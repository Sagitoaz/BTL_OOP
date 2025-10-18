package org.example.oop.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.example.oop.Model.Inventory.Enum.Category;
import org.example.oop.Model.Inventory.Enum.InventoryStatus;
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
 * ✅ Hỗ trợ LocalDateTime và custom ENUMs
 * ✅ Hỗ trợ Category và InventoryStatus ENUMs cho Inventory module
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
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                return new GsonBuilder()
                                // LocalDateTime adapter
                                .registerTypeAdapter(LocalDateTime.class,
                                                (JsonDeserializer<LocalDateTime>) (json, type, context) -> LocalDateTime
                                                                .parse(json.getAsString(), formatter))
                                .registerTypeAdapter(LocalDateTime.class,
                                                (JsonSerializer<LocalDateTime>) (src, type, context) -> context
                                                                .serialize(src.format(formatter)))

                                // AppointmentType ENUM adapter
                                .registerTypeAdapter(AppointmentType.class,
                                                (JsonDeserializer<AppointmentType>) (json, type,
                                                                context) -> AppointmentType
                                                                                .fromValue(json.getAsString()))
                                .registerTypeAdapter(AppointmentType.class,
                                                (JsonSerializer<AppointmentType>) (src, type, context) -> context
                                                                .serialize(src.getValue()))

                                // AppointmentStatus ENUM adapter
                                .registerTypeAdapter(AppointmentStatus.class,
                                                (JsonDeserializer<AppointmentStatus>) (json, type,
                                                                context) -> AppointmentStatus
                                                                                .fromValue(json.getAsString()))
                                .registerTypeAdapter(AppointmentStatus.class,
                                                (JsonSerializer<AppointmentStatus>) (src, type, context) -> context
                                                                .serialize(src.getValue()))

                                // ✅ Category ENUM adapter (Inventory module)
                                .registerTypeAdapter(Category.class,
                                                (JsonDeserializer<Category>) (json, type, context) -> Category
                                                                .fromCode(json.getAsString()))
                                .registerTypeAdapter(Category.class,
                                                (JsonSerializer<Category>) (src, type, context) -> context
                                                                .serialize(src.getCode()))

                                // ✅ InventoryStatus ENUM adapter (Inventory module)
                                .registerTypeAdapter(InventoryStatus.class,
                                                (JsonDeserializer<InventoryStatus>) (json, type,
                                                                context) -> InventoryStatus
                                                                                .fromCode(json.getAsString()))
                                .registerTypeAdapter(InventoryStatus.class,
                                                (JsonSerializer<InventoryStatus>) (src, type, context) -> context
                                                                .serialize(src.getCode()))

                                .create();
        }
}
