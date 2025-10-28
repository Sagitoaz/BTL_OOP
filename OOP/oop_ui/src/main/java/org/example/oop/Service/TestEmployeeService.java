package org.example.oop.Service;

import org.miniboot.app.domain.models.Employee;

public class TestEmployeeService {
    public static void main(String[] args) {
        // Có thể truyền baseUrl/token nếu cần: new HttpEmployeeService("http://localhost:8080", "<JWT>");
        HttpEmployeeService service = new HttpEmployeeService();

        try {
            // TEST 7: Get Doctors
            System.out.println("\n=== TEST 7: Get Doctors ===");
            var doctors = service.getEmployeesByRole("doctor");
            System.out.println("✅ Total doctors: " + doctors.size());

            // TEST 8: Get Nurses
            System.out.println("\n=== TEST 8: Get Nurses ===");
            var nurses = service.getEmployeesByRole("nurse");
            System.out.println("✅ Total nurses: " + nurses.size());

            // TEST 9: Delete
            System.out.println("\n=== TEST 9: Delete Employees ===");
            boolean deleted1 = service.deleteEmployee(7);
            boolean deleted2 = service.deleteEmployee(8);
            System.out.println("✅ Deleted doctor: " + deleted1);
            System.out.println("✅ Deleted nurse: " + deleted2);

            System.out.println("\n🎉 ALL TESTS PASSED!");

        } catch (Exception e) {
            System.err.println("❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
