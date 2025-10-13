package org.miniboot.app;

import org.miniboot.app.controllers.AppointmentController;
import org.miniboot.app.controllers.DoctorController;
import org.miniboot.app.controllers.EchoController;
import org.miniboot.app.controllers.AuthController;
import org.miniboot.app.controllers.HelloController;
import org.miniboot.app.controllers.UserController;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.domain.repo.InMemoryAppointmentRepository;
import org.miniboot.app.domain.repo.InMemoryDoctorRepository;
import org.miniboot.app.http.HttpServer;
import org.miniboot.app.router.Router;
import org.miniboot.app.router.middleware.AuthMiddlewareStub;
import org.miniboot.app.router.middleware.CorsMiddleware;
import org.miniboot.app.router.middleware.ErrorHandle;
import org.miniboot.app.router.middleware.LoggingMiddleware;

import java.time.LocalDateTime;
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
        // Cập nhật theo constructor mới: id, customerId, doctorId, appointmentType, notes, startTime, endTime, status, createdAt, updatedAt
        apptRepo.save(
                new Appointment(1, 1, 1, "visit", "Khám mắt định kỳ",
                    LocalDateTime.of(2025, 12, 30, 9, 0),
                    LocalDateTime.of(2025, 12, 30, 9, 30),
                    "scheduled", LocalDateTime.now(), LocalDateTime.now())
        );
        apptRepo.save(
                new Appointment(2, 2, 1, "visit", "Khám mắt lần đầu",
                    LocalDateTime.of(2025, 12, 30, 10, 0),
                    LocalDateTime.of(2025, 12, 30, 10, 30),
                    "scheduled", LocalDateTime.now(), LocalDateTime.now())
        );
        apptRepo.save(
                new Appointment(3, 3, 2, "test", "Kiểm tra thị lực",
                    LocalDateTime.of(2025, 12, 30, 9, 30),
                    LocalDateTime.of(2025, 12, 30, 10, 0),
                    "scheduled", LocalDateTime.now(), LocalDateTime.now())
        );
        apptRepo.save(
                new Appointment(4, 4, 2, "visit", "Tái khám",
                    LocalDateTime.of(2025, 12, 31, 11, 0),
                    LocalDateTime.of(2025, 12, 31, 11, 30),
                    "scheduled", LocalDateTime.now(), LocalDateTime.now())
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
        UserController.mount(router);

        // Khởi động server
        HttpServer server = new HttpServer(port, router);
        server.start();
    }
}
