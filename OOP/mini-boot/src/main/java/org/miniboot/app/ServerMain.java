package org.miniboot.app;

import org.miniboot.app.controllers.AppointmentController;
import org.miniboot.app.controllers.AuthController;
import org.miniboot.app.controllers.DoctorController;
import org.miniboot.app.controllers.Inventory.InventoryController;
import org.miniboot.app.controllers.payment.PaymentController;
import org.miniboot.app.controllers.payment.PaymentItemController;
import org.miniboot.app.controllers.payment.PaymentStatusLogController;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.domain.repo.Inventory.PostgreSQLProductRepository;
import org.miniboot.app.domain.repo.Inventory.ProductRepository;
import org.miniboot.app.domain.repo.Payment.*;
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
        AppConfig.load(); // Đọc cấu hình từ AppConfig

        System.out.println("🚀 Starting mini-boot HTTP Server...");
        System.out.println("📊 Using PostgreSQL repositories (Supabase)");

        // Sử dụng PostgreSQL repositories thay vì InMemory
        DoctorRepository doctorRepo = new PostgreSQLDoctorRepository();
        AppointmentRepository apptRepo = new PostgreSQLAppointmentRepository();
        // 🔽 ADD: Payment repositories// 🔽 ADD: PaymentItem repository
        PaymentItemRepository paymentItemRepo = new PostgreSQLPaymentItemRepository();

        PaymentRepository paymentRepo = new PostgreSQLPaymentRepository();
        PaymentStatusLogRepository paymentStatusRepo = new PostgreSQLPaymentStatusLogRepository();


        System.out.println("✅ Repositories initialized");

        // Tạo controllers
        DoctorController dc = new DoctorController(doctorRepo, apptRepo);
        AppointmentController ac = new AppointmentController(apptRepo);
        // 🔽 ADD: Payment controllers
        PaymentController pc = new PaymentController(paymentRepo, paymentStatusRepo);
        PaymentStatusLogController pslc = new PaymentStatusLogController(paymentStatusRepo);
        //// 🔽 ADD: PaymentItem controller
        PaymentItemController pic = new PaymentItemController(paymentItemRepo);


        // Inventory
        ProductRepository productRepo = new PostgreSQLProductRepository();
        InventoryController ic = new InventoryController(productRepo);

        // Tạo router và mount controllers
        Router router = new Router();
        router.use(new AuthMiddlewareStub());
        router.use(new CorsMiddleware());
        router.use(new LoggingMiddleware());
        router.use(new ErrorHandle());
        router.get("/doctors", dc.getDoctors());

        DoctorController.mount(router, dc);
        AppointmentController.mount(router, ac);
        InventoryController.mount(router, ic);
        // 🔽 ADD: Mount Payment routes
        PaymentController.mount(router, pc);
        PaymentStatusLogController.mount(router, pslc);
        // 🔽 ADD: Mount PaymentItem routes
        PaymentItemController.mount(router, pic);


        // mount các controller
        AuthController.mount(router);

        // Khởi động server
        HttpServer server = new HttpServer(port, router);
        System.out.println("🌐 Server starting on http://localhost:" + port);
        System.out.println("📋 Available endpoints:");
        System.out.println("   GET  /appointments");
        System.out.println("   POST /appointments");
        System.out.println("   GET  /doctors");
        System.out.println("   POST /auth/login");
        System.out.println("\n✅ Server is ready!");
        System.out.println("   GET  /products");
        System.out.println("   POST /products");
        System.out.println("   PUT  /products");
        System.out.println("   DELETE /products?id=...");
        // 🔽 ADD: Payment endpoints in the list
        System.out.println("   GET  /payments");
        System.out.println("   POST /payments");
        System.out.println("   PUT  /payments");
        System.out.println("   GET  /payment-status?paymentId=...");
        System.out.println("   POST /payment-status");
        // 🔽 ADD: PaymentItem endpoints in the list
        System.out.println("   GET    /payment-items");
        System.out.println("   POST   /payment-items");
        System.out.println("   PUT    /payment-items");
        System.out.println("   PUT    /payment-items/replace");
        System.out.println("   DELETE /payment-items?id=... | ?paymentId=...");

        server.start();
    }
}
