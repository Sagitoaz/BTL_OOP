package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Utils.ApiConfig;
import org.miniboot.app.domain.models.DoctorSchedule;
import org.miniboot.app.util.GsonProvider;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * HTTP Service cho Doctor Schedule (Lịch làm việc của bác sĩ)
 */
public class HttpDoctorScheduleService {
    
    private static final Gson gson = GsonProvider.getGson();
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    
    private final String baseUrl;
    private final String bearerToken;
    private final HttpClient httpClient;
    
    public HttpDoctorScheduleService() {
        this(ApiConfig.getBaseUrl(), null);
    }
    
    public HttpDoctorScheduleService(String baseUrl, String bearerToken) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.bearerToken = bearerToken;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(DEFAULT_TIMEOUT)
                .build();
    }
    
    /**
     * Helper: Tạo request builder với auth header
     */
    private HttpRequest.Builder reqBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(DEFAULT_TIMEOUT)
                .version(HttpClient.Version.HTTP_1_1);
        
        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        
        return builder;
    }
    
    /**
     * GET /doctor-schedules?doctorId=X
     * Lấy tất cả lịch làm việc của bác sĩ
     */
    public List<DoctorSchedule> getDoctorSchedules(int doctorId) throws Exception {
        String url = "/doctor-schedules?doctorId=" + doctorId;
        
        HttpRequest request = reqBuilder(url)
                .GET()
                .header("Accept", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<DoctorSchedule>>(){}.getType());
        } else {
            throw new Exception("Failed to get doctor schedules: HTTP " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * GET /doctor-schedules?doctorId=X&day=MONDAY
     * Lấy lịch làm việc của bác sĩ theo ngày cụ thể
     */
    public List<DoctorSchedule> getDoctorSchedulesByDay(int doctorId, String dayOfWeek) throws Exception {
        String url = "/doctor-schedules?doctorId=" + doctorId + "&day=" + URLEncoder.encode(dayOfWeek, StandardCharsets.UTF_8);
        
        HttpRequest request = reqBuilder(url)
                .GET()
                .header("Accept", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<DoctorSchedule>>(){}.getType());
        } else {
            throw new Exception("Failed to get doctor schedules by day: HTTP " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * POST /doctor-schedules
     * Tạo lịch làm việc mới
     */
    public DoctorSchedule createSchedule(DoctorSchedule schedule) throws Exception {
        String jsonBody = gson.toJson(schedule);
        
        HttpRequest request = reqBuilder("/doctor-schedules")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return gson.fromJson(response.body(), DoctorSchedule.class);
        } else {
            String errorMsg = extractErrorMessage(response.body());
            throw new Exception("Failed to create schedule: " + errorMsg);
        }
    }
    
    /**
     * PUT /doctor-schedules?id=X
     * Cập nhật lịch làm việc
     */
    public DoctorSchedule updateSchedule(int scheduleId, DoctorSchedule schedule) throws Exception {
        String jsonBody = gson.toJson(schedule);
        String url = "/doctor-schedules?id=" + scheduleId;
        
        HttpRequest request = reqBuilder(url)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), DoctorSchedule.class);
        } else {
            String errorMsg = extractErrorMessage(response.body());
            throw new Exception("Failed to update schedule: " + errorMsg);
        }
    }
    
    /**
     * DELETE /doctor-schedules?id=X
     * Xóa lịch làm việc
     */
    public void deleteSchedule(int scheduleId) throws Exception {
        String url = "/doctor-schedules?id=" + scheduleId;
        
        HttpRequest request = reqBuilder(url)
                .DELETE()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            String errorMsg = extractErrorMessage(response.body());
            throw new Exception("Failed to delete schedule: " + errorMsg);
        }
    }
    
    /**
     * ✅ DELETE /doctor-schedules/batch?doctorId=X
     * XÓA TẤT CẢ lịch làm việc của bác sĩ trong 1 request (Batch Delete)
     * Tối ưu hiệu suất: 1 request thay vì N requests
     */
    public int deleteAllSchedulesByDoctor(int doctorId) throws Exception {
        String url = "/doctor-schedules/batch?doctorId=" + doctorId;
        
        HttpRequest request = reqBuilder(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            // Parse response to get deleted count
            JsonObject result = gson.fromJson(response.body(), JsonObject.class);
            int deletedCount = result.has("deletedCount") ? result.get("deletedCount").getAsInt() : 0;
            System.out.println("✅ Batch deleted " + deletedCount + " schedules for doctor #" + doctorId);
            return deletedCount;
        } else {
            String errorMsg = extractErrorMessage(response.body());
            throw new Exception("Failed to batch delete schedules: " + errorMsg);
        }
    }
    
    /**
     * ✅ POST /doctor-schedules/batch
     * TẠO NHIỀU lịch làm việc cùng lúc trong 1 request (Batch Insert)
     * Tối ưu hiệu suất: 1 request thay vì N requests
     */
    public List<DoctorSchedule> createSchedulesBatch(List<DoctorSchedule> schedules) throws Exception {
        if (schedules == null || schedules.isEmpty()) {
            throw new IllegalArgumentException("Schedules list cannot be null or empty");
        }
        
        String jsonBody = gson.toJson(schedules);
        
        HttpRequest request = reqBuilder("/doctor-schedules/batch")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            // Parse response to get created schedules
            JsonObject result = gson.fromJson(response.body(), JsonObject.class);
            if (result.has("schedules")) {
                List<DoctorSchedule> created = gson.fromJson(
                    result.get("schedules"), 
                    new TypeToken<List<DoctorSchedule>>(){}.getType()
                );
                System.out.println("✅ Batch created " + created.size() + " schedules");
                return created;
            }
            return schedules; // Fallback if no schedules in response
        } else {
            String errorMsg = extractErrorMessage(response.body());
            throw new Exception("Failed to batch create schedules: " + errorMsg);
        }
    }
    
    /**
     * Helper: Extract error message from JSON response
     */
    private String extractErrorMessage(String json) {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            if (obj.has("message")) {
                return obj.get("message").getAsString();
            }
            if (obj.has("error")) {
                return obj.get("error").getAsString();
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return json;
    }
}
