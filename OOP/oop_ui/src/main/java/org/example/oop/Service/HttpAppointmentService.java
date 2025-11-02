package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Utils.ApiConfig;
import org.example.oop.Utils.ErrorHandler;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.util.GsonProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * HttpAppointmentService - Gọi mini-boot qua HTTP API
 * <p>
 * ✅ Dùng cho kiến trúc Client-Server
 * ✅ An toàn hơn (có thể thêm authentication)
 * ✅ Hỗ trợ nhiều client (Web, Mobile, Desktop)
 * ✅ Updated Day 7: Sử dụng ApiConfig endpoint management
 * <p>
 * Yêu cầu: ServerMain phải đang chạy
 */
public class HttpAppointmentService {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * Constructor mặc định - sử dụng ApiConfig baseUrl
     */
    public HttpAppointmentService() {
        this(ApiConfig.getBaseUrl());
    }

    /**
     * Constructor với custom URL (for testing)
     */
    public HttpAppointmentService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = GsonProvider.getGson();
    }

    /**
     * GET /appointments - Lấy tất cả appointments
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public List<Appointment> getAllAppointments() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + ApiConfig.appointmentsEndpoint()))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Validate response trước khi parse
                if (!ErrorHandler.validateResponse(response.body(), "Tải danh sách lịch hẹn")) {
                    return List.of();
                }

                try {
                    return gson.fromJson(response.body(),
                            new TypeToken<List<Appointment>>() {
                            }.getType());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse appointments data");
                    return List.of();
                }
            } else {
                // Hiển thị thông báo lỗi user-friendly
                ErrorHandler.showUserFriendlyError(
                        response.statusCode(),
                        "Không thể tải danh sách lịch hẹn");
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            // Xử lý lỗi kết nối
            ErrorHandler.handleConnectionError(e, "Tải danh sách lịch hẹn");
            return List.of();
        }
    }

    /**
     * GET /appointments?doctorId={id}&date={date}
     * Lấy appointments theo doctor và date
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public List<Appointment> getByDoctorAndDate(int doctorId, LocalDate date) {
        try {
            String url = String.format("%s/appointments?doctorId=%d&date=%s",
                    baseUrl, doctorId, date.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tải lịch hẹn theo bác sĩ và ngày")) {
                    return List.of();
                }

                try {
                    return gson.fromJson(response.body(),
                            new TypeToken<List<Appointment>>() {
                            }.getType());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse appointments by doctor and date");
                    return List.of();
                }
            } else {
                ErrorHandler.showUserFriendlyError(
                        response.statusCode(),
                        "Không thể tải lịch hẹn của bác sĩ");
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tải lịch hẹn theo bác sĩ và ngày");
            return List.of();
        }
    }

    /**
     * POST /appointments - Tạo appointment mới
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public Appointment create(Appointment appointment) {
        try {
            String jsonBody = gson.toJson(appointment);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + ApiConfig.appointmentsEndpoint()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tạo lịch hẹn mới")) {
                    return null;
                }

                try {
                    return gson.fromJson(response.body(), Appointment.class);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse created appointment");
                    return null;
                }
            } else {
                ErrorHandler.showUserFriendlyError(
                        response.statusCode(),
                        "Không thể tạo lịch hẹn mới");
                return null;
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tạo lịch hẹn mới");
            return null;
        }
    }

    /**
     * GET /appointments?id={id} - Tìm appointment theo ID
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public Optional<Appointment> findById(int id) {
        try {
            String url = String.format("%s%s?id=%d", baseUrl, ApiConfig.appointmentsEndpoint(), id);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tìm lịch hẹn theo ID")) {
                    return Optional.empty();
                }

                try {
                    Appointment appointment = gson.fromJson(response.body(), Appointment.class);
                    return Optional.ofNullable(appointment);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse appointment by ID");
                    return Optional.empty();
                }
            } else if (response.statusCode() == 404) {
                return Optional.empty();
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể tìm lịch hẹn");
                return Optional.empty();
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tìm lịch hẹn theo ID");
            return Optional.empty();
        }
    }

    /**
     * GET /appointments với filters (support multiple filters)
     * Tất cả params đều optional
     */
    public List<Appointment> getAppointmentsFiltered(
            Integer doctorId,
            Integer customerId,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            String searchKeyword) {
        try {
            // Build URL với query string
            StringBuilder url = new StringBuilder(baseUrl + ApiConfig.appointmentsEndpoint() + "?");
            boolean hasParam = false;

            if (doctorId != null) {
                url.append("doctorId=").append(doctorId);
                hasParam = true;
            }

            if (customerId != null) {
                if (hasParam)
                    url.append("&");
                url.append("customerId=").append(customerId);
                hasParam = true;
            }

            if (status != null && !status.equals("Tất cả")) {
                if (hasParam)
                    url.append("&");
                url.append("status=").append(status);
                hasParam = true;
            }

            if (fromDate != null) {
                if (hasParam)
                    url.append("&");
                url.append("fromDate=").append(fromDate.toString());
                hasParam = true;
            }

            if (toDate != null) {
                if (hasParam)
                    url.append("&");
                url.append("toDate=").append(toDate.toString());
                hasParam = true;
            }

            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                if (hasParam)
                    url.append("&");
                // URL encode search keyword
                url.append("search=").append(URLEncoder.encode(searchKeyword.trim(), "UTF-8"));
            }

            // Execute request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url.toString()))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tìm kiếm lịch hẹn với filters")) {
                    return List.of();
                }

                try {
                    return gson.fromJson(response.body(),
                            new TypeToken<List<Appointment>>() {
                            }.getType());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse filtered appointments");
                    return List.of();
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể tìm kiếm lịch hẹn");
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tìm kiếm lịch hẹn với filters");
            return List.of();
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Filtered appointments");
            return List.of();
        }
    }

    /**
     * GET /appointments?doctorId={id}&fromDate={from}&toDate={to}
     * Lấy appointments theo doctor và khoảng ngày
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public List<Appointment> getByDoctorAndDateRange(int doctorId, LocalDate fromDate, LocalDate toDate) {
        try {
            String url = String.format("%s/appointments?doctorId=%d&fromDate=%s&toDate=%s",
                    baseUrl, doctorId, fromDate.toString(), toDate.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tải lịch hẹn theo khoảng ngày")) {
                    return List.of();
                }

                try {
                    return gson.fromJson(response.body(),
                            new TypeToken<List<Appointment>>() {
                            }.getType());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse appointments by date range");
                    return List.of();
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể tải lịch hẹn theo khoảng ngày");
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tải lịch hẹn theo khoảng ngày");
            return List.of();
        }
    }

    /**
     * UPDATE appointment
     * PUT /appointments
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public Appointment update(Appointment appointment) {
        try {
            String jsonBody = gson.toJson(appointment);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + ApiConfig.appointmentsEndpoint()))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Cập nhật lịch hẹn")) {
                    return null;
                }

                try {
                    return gson.fromJson(response.body(), Appointment.class);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse updated appointment");
                    return null;
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể cập nhật lịch hẹn");
                return null;
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Cập nhật lịch hẹn");
            return null;
        }
    }

    /**
     * DELETE appointment
     * DELETE /appointments?id={id}
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public boolean delete(int id) {
        try {
            String url = String.format("%s/appointments?id=%d", baseUrl, id);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return true;
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể xóa lịch hẹn");
                return false;
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Xóa lịch hẹn");
            return false;
        }
    }

    /**
     * Kiểm tra kết nối server
     */
    public boolean isServerAvailable() {
        try {
            // Thay đổi từ /echo sang /appointments vì đã xóa EchoController
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/appointments"))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }
}
