package org.example.oop.Tests;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.oop.Services.HttpAppointmentService;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.models.AppointmentType;

/**
 * TestHttpAccess - Test truy cập qua HTTP API
 * 
 * YÊU CẦU: Phải chạy ServerMain trước khi chạy test này!
 * 
 * Cách chạy ServerMain:
 * cd mini-boot
 * mvnw exec:java -Dexec.mainClass="org.miniboot.app.ServerMain"
 */
public class TestHttpAccess {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🧪 TEST HTTP ACCESS TO MINI-BOOT API");
        System.out.println("=".repeat(60));
        System.out.println();
        
        HttpAppointmentService service = new HttpAppointmentService();
        
        // Test 0: Kiểm tra server có chạy không
        System.out.println("--- TEST 0: Check Server Connection ---");
        if (!service.isServerAvailable()) {
            System.err.println("❌ ERROR: Server không chạy!");
            System.err.println("\n⚠️ Hãy chạy ServerMain trước:");
            System.err.println("   cd mini-boot");
            System.err.println("   .\\mvnw.cmd exec:java -Dexec.mainClass=\"org.miniboot.app.ServerMain\"");
            System.err.println();
            return;
        }
        System.out.println("✅ Server is running!");
        System.out.println();
        
        // Test 1: Get All
        testGetAll(service);
        
        // Test 2: Get By Doctor and Date
        testGetByDoctorAndDate(service);
        
        // Test 3: Create
        testCreate(service);
        
        // Test 4: Find By ID
        testFindById(service);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ ALL HTTP TESTS COMPLETED!");
        System.out.println("=".repeat(60));
    }
    
    private static void testGetAll(HttpAppointmentService service) {
        System.out.println("--- TEST 1: GET /appointments (Get All) ---");
        try {
            List<Appointment> all = service.getAllAppointments();
            System.out.println("✅ Found " + all.size() + " appointments");
            
            if (!all.isEmpty()) {
                Appointment first = all.get(0);
                System.out.println("   First appointment:");
                printAppointment(first);
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void testGetByDoctorAndDate(HttpAppointmentService service) {
        System.out.println("--- TEST 2: GET /appointments?doctorId=1&date=2025-10-17 ---");
        try {
            List<Appointment> filtered = service.getByDoctorAndDate(
                    1, 
                    java.time.LocalDate.of(2025, 10, 17)
            );
            System.out.println("✅ Found " + filtered.size() + " appointments");
            
            for (Appointment appt : filtered) {
                printAppointment(appt);
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void testCreate(HttpAppointmentService service) {
        System.out.println("--- TEST 3: POST /appointments (Create) ---");
        try {
            Appointment newAppt = new Appointment();
            newAppt.setCustomerId(1);
            newAppt.setDoctorId(1);
            // Dùng fromValue() thay vì valueOf()
            newAppt.setAppointmentType(AppointmentType.fromValue("visit"));
            newAppt.setNotes("Test từ HTTP API");
            newAppt.setStartTime(LocalDateTime.of(2025, 12, 25, 14, 0));
            newAppt.setEndTime(LocalDateTime.of(2025, 12, 25, 15, 0));
            newAppt.setStatus(AppointmentStatus.fromValue("scheduled"));
            
            Appointment created = service.create(newAppt);
            
            if (created != null) {
                System.out.println("✅ Appointment created via HTTP:");
                printAppointment(created);
            } else {
                System.err.println("❌ Failed to create appointment");
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void testFindById(HttpAppointmentService service) {
        System.out.println("--- TEST 4: GET /appointments?id=1 ---");
        try {
            Optional<Appointment> found = service.findById(1);
            
            if (found.isPresent()) {
                System.out.println("✅ Found appointment:");
                printAppointment(found.get());
            } else {
                System.out.println("⚠️ No appointment found with ID 1");
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void printAppointment(Appointment appt) {
        System.out.println("   ID: " + appt.getId() + 
                         " | Doctor: " + appt.getDoctorId() + 
                         " | Customer: " + appt.getCustomerId());
        System.out.println("   Type: " + appt.getAppointmentType() + 
                         " | Status: " + appt.getStatus());
        System.out.println("   Start: " + appt.getStartTime() + 
                         " | End: " + appt.getEndTime());
        if (appt.getNotes() != null && !appt.getNotes().isEmpty()) {
            System.out.println("   Notes: " + appt.getNotes());
        }
    }
}
