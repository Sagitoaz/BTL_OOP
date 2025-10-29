package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
 * <p>
 * Yêu cầu: ServerMain phải đang chạy trên http://localhost:8080
 */
public class HttpAppointmentService {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * Constructor mặc định - kết nối localhost:8080
     */
    public HttpAppointmentService() {
        this("https://btl-oop-i9pi.onrender.com/");
    }

    /**
     * Constructor với custom URL
     */
    public HttpAppointmentService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = GsonProvider.getGson();
    }

    /**
     * GET /appointments - Lấy tất cả appointments
     */
    public List<Appointment> getAllAppointments() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/appointments"))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(),
                        new TypeToken<List<Appointment>>() {
                        }.getType());
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * GET /appointments?doctorId={id}&date={date}
     * Lấy appointments theo doctor và date
     */
    public List<Appointment> getByDoctorAndDate(int doctorId, LocalDate date) {
        try {
            String url = String.format("%s/appointments?doctorId=%d&date=%s",
                    baseUrl, doctorId, date.toString());
            
            System.out.println("🔍 DEBUG: Calling API: " + url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Appointment> appointments = gson.fromJson(response.body(), 
                        new TypeToken<List<Appointment>>(){}.getType());
                System.out.println("✅ DEBUG: Received " + appointments.size() + " appointments");
                // Debug first appointment if exists
                if (!appointments.isEmpty()) {
                    System.out.println("📅 DEBUG: First appointment date: " + appointments.get(0).getStartTime());
                }
                return appointments;
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * POST /appointments - Tạo appointment mới
     */
    public Appointment create(Appointment appointment) {
        try {
            String jsonBody = gson.toJson(appointment);
            System.out.println("Sending JSON: " + jsonBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/appointments"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return gson.fromJson(response.body(), Appointment.class);
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * GET /appointments?id={id} - Tìm appointment theo ID
     */
    public Optional<Appointment> findById(int id) {
        try {
            String url = String.format("%s/appointments?id=%d", baseUrl, id);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Appointment appointment = gson.fromJson(response.body(), Appointment.class);
                return Optional.ofNullable(appointment);
            } else if (response.statusCode() == 404) {
                return Optional.empty();
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return Optional.empty();
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
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
            StringBuilder url = new StringBuilder(baseUrl + "/appointments?");
            boolean hasParam = false;

            if (doctorId != null) {
                url.append("doctorId=").append(doctorId);
                hasParam = true;
            }

            if (customerId != null) {
                if (hasParam) url.append("&");
                url.append("customerId=").append(customerId);
                hasParam = true;
            }

            if (status != null && !status.equals("Tất cả")) {
                if (hasParam) url.append("&");
                url.append("status=").append(status);
                hasParam = true;
            }

            if (fromDate != null) {
                if (hasParam) url.append("&");
                url.append("fromDate=").append(fromDate.toString());
                hasParam = true;
            }

            if (toDate != null) {
                if (hasParam) url.append("&");
                url.append("toDate=").append(toDate.toString());
                hasParam = true;
            }

            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                if (hasParam) url.append("&");
                // URL encode search keyword
                url.append("search=").append(URLEncoder.encode(searchKeyword.trim(), "UTF-8"));
            }

            System.out.println("🔍 Calling API: " + url);

            // Execute request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url.toString()))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(),
                        new TypeToken<List<Appointment>>(){}.getType());
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return List.of();
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * GET /appointments?doctorId={id}&fromDate={from}&toDate={to}
     * Lấy appointments theo doctor và khoảng ngày
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
                return gson.fromJson(response.body(),
                        new TypeToken<List<Appointment>>(){}.getType());
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * UPDATE appointment
     * PUT /appointments
     */
    public Appointment update(Appointment appointment) {
        try {
            String jsonBody = gson.toJson(appointment);
            System.out.println("📤 Updating appointment: " + jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/appointments"))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Update successful");
                return gson.fromJson(response.body(), Appointment.class);
            } else {
                System.err.println("❌ Update failed: " + response.statusCode());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * DELETE appointment
     * DELETE /appointments?id={id}
     */
    public boolean delete(int id) {
        try {
            String url = String.format("%s/appointments?id=%d", baseUrl, id);
            System.out.println("📤 Deleting appointment: " + id);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Delete successful");
                return true;
            } else {
                System.err.println("❌ Delete failed: " + response.statusCode());
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
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
