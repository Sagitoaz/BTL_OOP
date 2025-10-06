package org.miniboot.app;

import org.miniboot.app.controllers.AppointmentController;
import org.miniboot.app.controllers.DoctorController;
import org.miniboot.app.controllers.EchoController;
import org.miniboot.app.controllers.AuthController;
import org.miniboot.app.controllers.HelloController;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.domain.repo.InMemoryAppointmentRepository;
import org.miniboot.app.domain.repo.InMemoryDoctorRepository;
import org.miniboot.app.http.HttpServer;
import org.miniboot.app.router.PathPattern;
import org.miniboot.app.router.Router;
import org.miniboot.app.router.middleware.AuthMiddlewareStub;
import org.miniboot.app.router.middleware.CorsMiddleware;
import org.miniboot.app.router.middleware.ErrorHandle;
import org.miniboot.app.router.middleware.LoggingMiddleware;

import java.util.Map;

import java.util.List;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        // Đọc port từ AppConfig
        int port = Integer.parseInt(System.getProperty(AppConfig.PORT_KEY, AppConfig.HTTP_PORT));
        AppConfig.load();  // Đọc cấu hình từ AppConfig

        // Seed data
        DoctorRepository doctorRepo = new InMemoryDoctorRepository();
        doctorRepo.saveAll(List.of(
                new Doctor(1, "Hậu", "Trần Văn", "123456"),
                new Doctor(2, "Dúng", "Dương Chí", "1236780"),
                new Doctor(3, "Toàn", "Phạm Minh", "71439"),
                new Doctor(4, "Hếu", "Phan Minh", "1637428"),
                new Doctor(5, "Trung", "Nuyễn Thành", "23647")
        ));

        AppointmentRepository apptRepo = new InMemoryAppointmentRepository();
        apptRepo.save(
                new Appointment(0, 1, "Dương Chí Dúng", "09:00", "2025-12-30")
        );
        apptRepo.save(
                new Appointment(0, 1, "Nguyễn Văn A", "10:00", "2025-12-30")
        );
        apptRepo.save(
                new Appointment(0, 2, "Trần Thị B", "09:30", "2025-12-30")
        );
        apptRepo.save(
                new Appointment(0, 2, "Lê Văn C", "11:00", "2025-12-31")
        );

        // Tạo controllers
        DoctorController dc = new DoctorController(doctorRepo);
        AppointmentController ac = new AppointmentController(apptRepo);

        // Tạo router và mount controllers
        Router router = new Router();
        router.use(new AuthMiddlewareStub());
        router.use(new CorsMiddleware());
        router.use(new LoggingMiddleware());
        router.use(new ErrorHandle());
        router.get("/doctors", dc.getDoctors());

        DoctorController.mount(router, dc);
        AppointmentController.mount(router, ac);
        HelloController.mount(router);  // HelloController sử dụng mount để thêm routes
        EchoController.mount(router);   // EchoController cũng vậy
        // mount các controller
        AuthController.mount(router);

        // Khởi động server
        HttpServer server = new HttpServer(port, router);
        server.start();



    }
}
