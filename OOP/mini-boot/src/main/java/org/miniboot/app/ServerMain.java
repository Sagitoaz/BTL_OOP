package org.miniboot.app;

import org.miniboot.app.controllers.AppointmentController;
import org.miniboot.app.controllers.AuthController;
import org.miniboot.app.controllers.DoctorController;
import org.miniboot.app.controllers.PatientAndPrescription.CustomerRecordController;
import org.miniboot.app.controllers.PatientAndPrescription.PrescriptionController;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.domain.repo.PatientAndPrescription.CustomerRecordRepository;
import org.miniboot.app.domain.repo.PatientAndPrescription.PostgreSQLCustomerRecordRepository;
import org.miniboot.app.domain.repo.PatientAndPrescription.PostgreSQLPrescription;
import org.miniboot.app.domain.repo.PatientAndPrescription.PrescriptionRepository;
import org.miniboot.app.domain.repo.PostgreSQLAppointmentRepository;
import org.miniboot.app.domain.repo.PostgreSQLDoctorRepository;
import org.miniboot.app.http.HttpServer;
import org.miniboot.app.router.Router;
import org.miniboot.app.router.middleware.AuthMiddlewareStub;
import org.miniboot.app.router.middleware.CorsMiddleware;
import org.miniboot.app.router.middleware.ErrorHandle;
import org.miniboot.app.router.middleware.LoggingMiddleware;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        // Đọc port từ AppConfig
        int port = Integer.parseInt(System.getProperty(AppConfig.PORT_KEY, AppConfig.HTTP_PORT));
        AppConfig.load();  // Đọc cấu hình từ AppConfig

        System.out.println("🚀 Starting mini-boot HTTP Server...");
        System.out.println("📊 Using PostgreSQL repositories (Supabase)");
        
        // Sử dụng PostgreSQL repositories thay vì InMemory
        DoctorRepository doctorRepo = new PostgreSQLDoctorRepository();
        AppointmentRepository apptRepo = new PostgreSQLAppointmentRepository();
        CustomerRecordRepository customerRecordRepo = new PostgreSQLCustomerRecordRepository();
        PrescriptionRepository prescriptionRepository = new PostgreSQLPrescription();
        
        System.out.println("✅ Repositories initialized");

        // Tạo controllers
        DoctorController dc = new DoctorController(doctorRepo);
        AppointmentController ac = new AppointmentController(apptRepo);
        CustomerRecordController crc = new CustomerRecordController(customerRecordRepo);
        PrescriptionController pc = new PrescriptionController(prescriptionRepository);

        // Tạo router và mount controllers
        Router router = new Router();
        router.use(new AuthMiddlewareStub());
        router.use(new CorsMiddleware());
        router.use(new LoggingMiddleware());
        router.use(new ErrorHandle());
        router.get("/doctors", dc.getDoctors());

        DoctorController.mount(router, dc);
        AppointmentController.mount(router, ac);
        PrescriptionController.mount(router, pc);
        // mount các controller
        AuthController.mount(router);
        CustomerRecordController.mount(router, crc);

        // Khởi động server
        HttpServer server = new HttpServer(port, router);
        System.out.println("🌐 Server starting on http://localhost:" + port);
        System.out.println("📋 Available endpoints:");
        System.out.println("   GET  /appointments");
        System.out.println("   POST /appointments");
        System.out.println("   GET  /doctors");
        System.out.println("   POST /auth/login");
        System.out.println("\n✅ Server is ready!");
        server.start();
    }
}
