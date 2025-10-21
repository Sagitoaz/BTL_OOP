package org.example.oop.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.models.AppointmentType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * GsonProvider - Tạo Gson instance với cấu hình sẵn
 * 
 * ✅ Tránh duplicate code khi tạo Gson
 * ✅ Centralized configuration cho tất cả services
 * ✅ Hỗ trợ LocalDateTime, LocalDate, LocalTime và custom ENUMs
 */
public class GsonProvider {
    
    private static Gson instance;
    
    /**
     * Lấy Gson instance (Singleton pattern)
     */
    public static Gson getGson() {
        if (instance == null) {
            instance = createGson();
        }
        return instance;
    }
    
    /**
     * Tạo Gson mới với custom adapters
     */
    public static Gson createGson() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME; // ✅ THÊM

        return new GsonBuilder()
                // LocalDateTime adapter
                .registerTypeAdapter(LocalDateTime.class, 
                        (JsonDeserializer<LocalDateTime>) (json, type, context) -> 
                                LocalDateTime.parse(json.getAsString(), dateTimeFormatter))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, type, context) -> 
                                context.serialize(src.format(dateTimeFormatter)))

                // LocalDate adapter
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, context) ->
                                LocalDate.parse(json.getAsString(), dateFormatter))
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, type, context) ->
                                context.serialize(src.format(dateFormatter)))

                // ✅ LocalTime adapter - FIX CHO TIMESLOT
                .registerTypeAdapter(LocalTime.class,
                        (JsonDeserializer<LocalTime>) (json, type, context) ->
                                LocalTime.parse(json.getAsString(), timeFormatter))
                .registerTypeAdapter(LocalTime.class,
                        (JsonSerializer<LocalTime>) (src, type, context) ->
                                context.serialize(src.format(timeFormatter)))

                // AppointmentType ENUM adapter
                .registerTypeAdapter(AppointmentType.class,
                        (JsonDeserializer<AppointmentType>) (json, type, context) ->
                                AppointmentType.fromValue(json.getAsString()))
                .registerTypeAdapter(AppointmentType.class,
                        (JsonSerializer<AppointmentType>) (src, type, context) ->
                                context.serialize(src.getValue()))
                
                // AppointmentStatus ENUM adapter
                .registerTypeAdapter(AppointmentStatus.class,
                        (JsonDeserializer<AppointmentStatus>) (json, type, context) ->
                                AppointmentStatus.fromValue(json.getAsString()))
                .registerTypeAdapter(AppointmentStatus.class,
                        (JsonSerializer<AppointmentStatus>) (src, type, context) ->
                                context.serialize(src.getValue()))
                
                .create();
    }
}
