package org.miniboot.app.auth;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager
 *
 * Lớp chịu trách nhiệm quản lý các phiên (sessions) người dùng trong bộ nhớ (in-memory).
 * - Mục đích: cấp, lưu, kiểm tra và hủy session khi người dùng đăng nhập/đăng xuất.
 * - Lưu ý: hiện tại là in-memory (ConcurrentHashMap). Với môi trường production nhiều instance
 *   cần chuyển sang store phân tán (Redis, memcached, DB) để chia sẻ session giữa các instance.
 *
 * Thiết kế chính:
 * - Singleton: đảm bảo chỉ có một instance quản lý session trong JVM.
 * - Mỗi session được lưu dưới dạng Session object với thông tin cơ bản (userId, username, role, timestamps).
 * - Có task background (daemon thread) dọn các session đã hết hạn.
 */
public class SessionManager {
    // Singleton instance
    private static SessionManager instance;

    // Bản đồ lưu các session hiện đang active: sessionId -> Session
    // Sử dụng ConcurrentHashMap để an toàn khi truy cập đồng thời từ nhiều thread
    private final Map<String, Session> activeSessions;

    // Thời gian timeout mặc định cho session, tính bằng phút
    // Nếu user không có hoạt động trong khoảng này thì session được coi là expired
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    private SessionManager() {
        this.activeSessions = new ConcurrentHashMap<>();
        // Khởi động task dọn session hết hạn (chạy nền, daemon)
        startSessionCleanupTask();
    }

    // Lấy instance singleton (double-checked locking để an toàn multi-thread)
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Create new session
     *
     * Quy trình:
     * 1. Hủy các session cũ của user (nếu muốn chỉ cho 1 session/1 user tại 1 thời điểm)
     * 2. Tạo sessionId mới (UUID)
     * 3. Lưu Session vào activeSessions
     *
     * @param userId id người dùng
     * @param username username (dùng để log/hiển thị)
     * @param role role của user (ví dụ: ADMIN, STAFF...)
     * @return sessionId mới tạo
     */
    public String createSession(String userId, String username, String role) {
        // Nếu muốn đảm bảo một user chỉ có một phiên duy nhất, gọi invalidateUserSessions
        invalidateUserSessions(userId);

        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId, userId, username, role);
        activeSessions.put(sessionId, session);

        // Log đơn giản (stdout). Trong production nên dùng logger và tránh in dữ liệu nhạy cảm.
        System.out.println("✓ Session created: " + username + " (" + role + ")");
        return sessionId;
    }

    /**
     * Get session
     *
     * Lấy session theo sessionId. Nếu session tồn tại và chưa hết hạn thì cập nhật lastActivity
     * và trả về Optional.of(session). Nếu không tồn tại hoặc đã hết hạn, trả về Optional.empty().
     *
     * @param sessionId id của session
     * @return Optional chứa Session nếu hợp lệ
     */
    public Optional<Session> getSession(String sessionId) {
        Session session = activeSessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            // Cập nhật thời điểm hoạt động cuối cùng khi user có request
            session.updateLastActivity();
            return Optional.of(session);
        }
        return Optional.empty();
    }

    /**
     * Invalidate session
     *
     * Xoá session khỏi activeSessions khi user logout hoặc admin force logout.
     *
     * @param sessionId id của session cần hủy
     */
    public void invalidateSession(String sessionId) {
        Session session = activeSessions.remove(sessionId);
        if (session != null) {
            System.out.println("✓ Session invalidated: " + session.getUsername());
        }
    }

    /**
     * Invalidate all sessions for user
     *
     * Huỷ tất cả session thuộc về userId. Dùng khi muốn đảm bảo user chỉ có một session hoặc
     * khi thực hiện tác vụ bảo mật (ví dụ: đổi mật khẩu, khóa tài khoản).
     *
     * @param userId id người dùng
     */
    public void invalidateUserSessions(String userId) {
        // removeIf an toàn với ConcurrentHashMap.entrySet()
        activeSessions.entrySet().removeIf(entry ->
                entry.getValue().getUserId().equals(userId)
        );
    }

    /**
     * Check if session is valid
     *
     * @param sessionId id của session
     * @return true nếu session tồn tại và chưa hết hạn
     */
    public boolean isSessionValid(String sessionId) {
        return getSession(sessionId).isPresent();
    }

    /**
     * Get active session count
     *
     * Trả về số lượng session hiện đang được lưu (bao gồm cả những session sắp hết hạn chưa kịp dọn).
     *
     * @return số lượng session active
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Cleanup expired sessions
     *
     * Tạo một daemon thread chạy định kỳ (mỗi phút) để dọn các session đã expired.
     * Đây là cơ chế bảo đảm memory không bị đầy bởi session cũ.
     */
    private void startSessionCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // Every minute
                    cleanupExpiredSessions();
                } catch (InterruptedException e) {
                    // Nếu thread bị interrupt, thoát vòng lặp để thread kết thúc
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true); // Daemon để JVM có thể kết thúc nếu chỉ có thread này đang chạy
        cleanupThread.start();
    }

    // Kiểm tra và xoá các session đã expired
    private void cleanupExpiredSessions() {
        activeSessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Session class
     *
     * Lưu trữ thông tin session:
     * - sessionId: id duy nhất
     * - userId: id người dùng liên kết
     * - username, role: để tiện log và kiểm tra quyền
     * - createdAt: thời điểm tạo session
     * - lastActivity: thời điểm hoạt động cuối cùng (dùng để tính timeout)
     *
     * Phương thức chính:
     * - updateLastActivity(): cập nhật timestamp khi có hoạt động
     * - isExpired(): kiểm tra dựa trên lastActivity + SESSION_TIMEOUT_MINUTES
     */
    public static class Session {
        private final String sessionId;
        private final String userId;
        private final String username;
        private final String role;
        private final LocalDateTime createdAt;
        private LocalDateTime lastActivity;

        public Session(String sessionId, String userId, String username, String role) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.createdAt = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
        }

        // Cập nhật thời điểm hoạt động cuối
        public void updateLastActivity() {
            this.lastActivity = LocalDateTime.now();
        }

        // Kiểm tra session có expired hay không
        public boolean isExpired() {
            // So sánh lastActivity + timeout với thời điểm hiện tại
            return LocalDateTime.now().isAfter(
                    lastActivity.plusMinutes(SESSION_TIMEOUT_MINUTES)
            );
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastActivity() { return lastActivity; }
    }
}