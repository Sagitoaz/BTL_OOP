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
import com.google.gson.annotations.SerializedName;
import org.miniboot.app.domain.models.Inventory.Enum.Category;

/**
 * GsonProvider - Tạo Gson instance với cấu hình sẵn
 * <p>
 * ✅ Tránh duplicate code khi tạo Gson
 * ✅ Centralized configuration cho tất cả services
 * ✅ Hỗ trợ LocalDate, LocalDateTime và Category ENUM
 * ❌ REMOVED: InventoryStatus (DB dùng boolean is_active)
 * ❌ COMMENTED: AppointmentType/Status (backend chưa có)
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
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

        return new GsonBuilder()
                // ✅ QUAN TRỌNG: Gson phải đọc cả snake_case và camelCase
                // Backend trả về: price_retail, qty_on_hand, price_cost, created_at...
                // Nhưng Java field names là: priceRetail, qtyOnHand, priceCost, createdAt...
                // Gson sẽ tự động map snake_case → camelCase
                .setFieldNamingStrategy(f -> {
                    // Ưu tiên đọc từ @SerializedName annotation (nếu có)
                    SerializedName serializedName = f.getAnnotation(SerializedName.class);
                    if (serializedName != null && !serializedName.value().isEmpty()) {
                        return serializedName.value(); // Trả về "price_retail", "qty_on_hand"...
                    }
                    // Fallback: giữ nguyên tên field
                    return f.getName();
                })

                // ✅ LocalDate adapter (cho expiry_date trong Product)
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, context) -> {
                            if (json.isJsonNull())
                                return null;
                            return LocalDate.parse(json.getAsString(), dateFormatter);
                        })
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, type, context) -> {
                            if (src == null)
                                return null;
                            return context.serialize(src.format(dateFormatter));
                        })

                // ✅ LocalDateTime adapter
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, context) -> {
                            if (json.isJsonNull())
                                return null;
                            return LocalDateTime.parse(json.getAsString(),
                                    dateTimeFormatter);
                        })
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, type, context) -> {
                            if (src == null)
                                return null;
                            return context.serialize(src.format(dateTimeFormatter));
                        })

                // ✅ Category ENUM adapter (Inventory module)
                .registerTypeAdapter(Category.class,
                        (JsonDeserializer<Category>) (json, type, context) -> Category
                                .fromCode(json.getAsString()))
                .registerTypeAdapter(Category.class,
                        (JsonSerializer<Category>) (src, type, context) -> context
                                .serialize(src.getCode()))

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
