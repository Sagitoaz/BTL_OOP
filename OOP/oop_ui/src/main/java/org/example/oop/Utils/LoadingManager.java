package org.example.oop.Utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.effect.BoxBlur;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * LoadingManager - Quản lý loading states và overlays cho ứng dụng
 * 
 * Chức năng:
 * - Show/hide loading overlay trên bất kỳ container nào
 * - Progress tracking với message tùy chỉnh
 * - Cancel functionality cho long-running tasks
 * - Thread-safe với JavaFX UI thread
 * 
 * Usage:
 * 
 * <pre>
 * // Show loading
 * LoadingManager.show(rootPane, "Đang tải dữ liệu...");
 * 
 * // Hide loading
 * LoadingManager.hide(rootPane);
 * 
 * // With cancel button
 * LoadingManager.showWithCancel(rootPane, "Đang xử lý...", () -> {
 *     // Cancel action
 *     task.cancel();
 * });
 * </pre>
 */
public class LoadingManager {

    // CONSTANTS

    private static final String DEFAULT_MESSAGE = "Đang tải...";
    private static final String DEFAULT_CANCEL_TEXT = "Hủy";
    private static final double OVERLAY_OPACITY = 0.7;
    private static final double BLUR_RADIUS = 5.0;

    // STATE MANAGEMENT

    /**
     * Map để track loading overlays cho từng container
     * Key: StackPane container
     * Value: VBox loading overlay
     */
    private static final Map<StackPane, VBox> activeOverlays = new ConcurrentHashMap<>();

    /**
     * Map để track cancel actions
     * Key: StackPane container
     * Value: Runnable cancel action
     */
    private static final Map<StackPane, Runnable> cancelActions = new ConcurrentHashMap<>();

    // PUBLIC API - BASIC LOADING

    /**
     * Hiển thị loading overlay với message mặc định
     * 
     * @param container StackPane container để hiển thị overlay
     */
    public static void show(StackPane container) {
        show(container, DEFAULT_MESSAGE);
    }

    /**
     * Hiển thị loading overlay với custom message
     * 
     * @param container StackPane container để hiển thị overlay
     * @param message   Message hiển thị (Vietnamese)
     */
    public static void show(StackPane container, String message) {
        showWithCancel(container, message, null);
    }

    /**
     * Hiển thị loading overlay với cancel button
     * 
     * @param container StackPane container
     * @param message   Loading message
     * @param onCancel  Action thực hiện khi user click Cancel (null = no cancel
     *                  button)
     */
    public static void showWithCancel(StackPane container, String message, Runnable onCancel) {
        if (container == null) {
            System.err.println("⚠️ LoadingManager.show(): container is null");
            return;
        }

        Platform.runLater(() -> {
            // Nếu đã có overlay, remove trước
            if (activeOverlays.containsKey(container)) {
                hide(container);
            }

            // Tạo loading overlay
            VBox overlay = createLoadingOverlay(message, onCancel);

            // Store reference
            activeOverlays.put(container, overlay);
            if (onCancel != null) {
                cancelActions.put(container, onCancel);
            }

            // Add vào container
            container.getChildren().add(overlay);

            // Apply blur effect (optional - có thể tốn performance)
            // applyBlurEffect(container, true);
        });
    }

    /**
     * Ẩn loading overlay
     * 
     * @param container StackPane container
     */
    public static void hide(StackPane container) {
        if (container == null) {
            return;
        }

        Platform.runLater(() -> {
            VBox overlay = activeOverlays.remove(container);
            cancelActions.remove(container);

            if (overlay != null) {
                container.getChildren().remove(overlay);
                // applyBlurEffect(container, false);
            }
        });
    }

    /**
     * Update loading message
     * 
     * @param container  StackPane container
     * @param newMessage Message mới
     */
    public static void updateMessage(StackPane container, String newMessage) {
        if (container == null || newMessage == null) {
            return;
        }

        Platform.runLater(() -> {
            VBox overlay = activeOverlays.get(container);
            if (overlay != null) {
                // Find label trong overlay và update
                overlay.getChildren().stream()
                        .filter(node -> node instanceof Label)
                        .map(node -> (Label) node)
                        .findFirst()
                        .ifPresent(label -> label.setText(newMessage));
            }
        });
    }

    /**
     * Check xem container có đang loading không
     * 
     * @param container StackPane container
     * @return true nếu đang loading
     */
    public static boolean isLoading(StackPane container) {
        return activeOverlays.containsKey(container);
    }

    // PUBLIC API - ADVANCED LOADING WITH TASK

    /**
     * Execute task với loading indicator
     * 
     * @param <T>       Type của result
     * @param container StackPane container
     * @param message   Loading message
     * @param task      Task cần execute
     * @param onSuccess Callback khi success
     * @param onError   Callback khi error
     */
    public static <T> void executeWithLoading(
            StackPane container,
            String message,
            Task<T> task,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        // Show loading với cancel functionality
        showWithCancel(container, message, () -> {
            if (task != null && task.isRunning()) {
                task.cancel();
                System.out.println("🚫 Task cancelled by user");
            }
        });

        // Task completion handlers
        task.setOnSucceeded(event -> {
            hide(container);
            if (onSuccess != null) {
                try {
                    onSuccess.accept(task.getValue());
                } catch (Exception e) {
                    System.err.println("❌ Error in success callback: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        task.setOnFailed(event -> {
            hide(container);
            Throwable exception = task.getException();
            if (onError != null) {
                onError.accept(exception);
            } else {
                System.err.println("❌ Task failed: " +
                        (exception != null ? exception.getMessage() : "Unknown error"));
            }
        });

        task.setOnCancelled(event -> {
            hide(container);
            System.out.println("ℹ️ Task cancelled");
        });

        // Execute task trong background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // PRIVATE HELPERS - UI CREATION

    /**
     * Tạo loading overlay UI
     * 
     * @param message  Loading message
     * @param onCancel Cancel action (null = no cancel button)
     * @return VBox overlay
     */
    private static VBox createLoadingOverlay(String message, Runnable onCancel) {
        VBox overlay = new VBox(15);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, " + OVERLAY_OPACITY + ");");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // ProgressIndicator (spinner)
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(60, 60);
        spinner.setStyle("-fx-progress-color: #4CAF50;");

        // Loading message label
        Label messageLabel = new Label(message);
        messageLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10px;");

        overlay.getChildren().addAll(spinner, messageLabel);

        // Add cancel button nếu có onCancel action
        if (onCancel != null) {
            Button cancelButton = new Button(DEFAULT_CANCEL_TEXT);
            cancelButton.setStyle(
                    "-fx-background-color: #f44336; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 8px 20px; " +
                            "-fx-cursor: hand; " +
                            "-fx-background-radius: 4px;");

            // Hover effect
            cancelButton.setOnMouseEntered(
                    e -> cancelButton.setStyle(cancelButton.getStyle() + "-fx-background-color: #d32f2f;"));
            cancelButton.setOnMouseExited(
                    e -> cancelButton.setStyle(cancelButton.getStyle().replace("-fx-background-color: #d32f2f;", "")));

            cancelButton.setOnAction(e -> {
                if (onCancel != null) {
                    try {
                        onCancel.run();
                    } catch (Exception ex) {
                        System.err.println("❌ Error executing cancel action: " + ex.getMessage());
                    }
                }
            });

            overlay.getChildren().add(cancelButton);
        }

        return overlay;
    }

    /**
     * Apply blur effect vào container background (optional - performance cost)
     * 
     * @param container StackPane
     * @param enable    true để enable blur, false để remove
     */
    private static void applyBlurEffect(StackPane container, boolean enable) {
        if (enable) {
            BoxBlur blur = new BoxBlur(BLUR_RADIUS, BLUR_RADIUS, 3);
            // Apply to all children except overlay
            container.getChildren().stream()
                    .filter(node -> !(node instanceof VBox)) // Skip overlay
                    .forEach(node -> node.setEffect(blur));
        } else {
            container.getChildren().forEach(node -> node.setEffect(null));
        }
    }

    // UTILITY METHODS

    /**
     * Hide tất cả loading overlays (cleanup)
     */
    public static void hideAll() {
        activeOverlays.keySet().forEach(LoadingManager::hide);
    }

    /**
     * Get số lượng active loading overlays
     * 
     * @return Số lượng overlays đang active
     */
    public static int getActiveCount() {
        return activeOverlays.size();
    }

    /**
     * Clear tất cả references (dùng khi shutdown app)
     */
    public static void cleanup() {
        Platform.runLater(() -> {
            hideAll();
            activeOverlays.clear();
            cancelActions.clear();
        });
    }

    // CONVENIENCE METHODS

    /**
     * Show loading với Vietnamese message templates
     */
    public static class Messages {
        public static final String LOADING_DATA = "Đang tải dữ liệu...";
        public static final String SAVING_DATA = "Đang lưu dữ liệu...";
        public static final String DELETING_DATA = "Đang xóa dữ liệu...";
        public static final String PROCESSING = "Đang xử lý...";
        public static final String CONNECTING = "Đang kết nối máy chủ...";
        public static final String UPLOADING = "Đang tải lên...";
        public static final String DOWNLOADING = "Đang tải xuống...";
        public static final String SEARCHING = "Đang tìm kiếm...";
        public static final String VALIDATING = "Đang kiểm tra dữ liệu...";
        public static final String GENERATING_REPORT = "Đang tạo báo cáo...";
    }
}
