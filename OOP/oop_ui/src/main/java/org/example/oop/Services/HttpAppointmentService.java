package org.example.oop.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.Appointment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * HttpAppointmentService - Gọi mini-boot qua HTTP API
 * 
 * ✅ Dùng cho kiến trúc Client-Server
 * ✅ An toàn hơn (có thể thêm authentication)
 * ✅ Hỗ trợ nhiều client (Web, Mobile, Desktop)
 * 
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
        this("http://localhost:8080");
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
     * GET /appointments?doctorId={id}&date={date}
     * Lấy appointments theo doctor và date
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
     * POST /appointments - Tạo appointment mới
     */
    public Appointment create(Appointment appointment) {
        try {
            String jsonBody = gson.toJson(appointment);
            System.out.println("📤 Sending JSON: " + jsonBody); // Debug
            
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
