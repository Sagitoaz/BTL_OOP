package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Utils.ApiConfig;
import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.models.TimeSlot;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.models.TimeSlot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HttpDoctorService {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public HttpDoctorService() {
        this(ApiConfig.getBaseUrl());
    }

    public HttpDoctorService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = GsonProvider.getGson();
    }

    /**
     * GET /doctors - Lấy tất cả doctors
     */
    public List<Doctor> getAllDoctors() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/doctors"))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(),
                        new TypeToken<List<Doctor>>() {
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
     * GET /doctors/available-slots?doctorId={id}&date={date}
     * Lấy danh sách slot trống trong ngày
     */
    public List<TimeSlot> getAvailableSlots(int doctorId, String date) {
        try {
            String url = String.format("%s/doctors/available-slots?doctorId=%d&date=%s",
                    baseUrl, doctorId, date);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(),
                        new TypeToken<List<TimeSlot>>() {
                        }.getType());
            } else if (response.statusCode() == 400) {
                System.err.println("❌ Bad request: Missing doctorId or date");
                return List.of();
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
}
