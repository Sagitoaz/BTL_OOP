package org.miniboot.app;

import org.miniboot.app.controllers.AppointmentController;
import org.miniboot.app.controllers.DoctorController;
import org.miniboot.app.controllers.EchoController;
import org.miniboot.app.controllers.AuthController;
import org.miniboot.app.controllers.HelloController;
import org.miniboot.app.controllers.PatientAndPrescription.CustomerRecordController;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.domain.repo.PatientAndPrescription.CustomerRecordRepository;
import org.miniboot.app.domain.repo.PatientAndPrescription.InMemoryPatientRecordRepository;
import org.miniboot.app.http.HttpServer;
import org.miniboot.app.router.Router;
import org.miniboot.app.router.middleware.AuthMiddlewareStub;
import org.miniboot.app.router.middleware.CorsMiddleware;
import org.miniboot.app.router.middleware.ErrorHandle;
import org.miniboot.app.router.middleware.LoggingMiddleware;

import java.util.List;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        // Đọc port từ AppConfig
        int port = Integer.parseInt(System.getProperty(AppConfig.PORT_KEY, AppConfig.HTTP_PORT));
        AppConfig.load();  // Đọc cấu hình từ AppConfig

        // Seed data

        // Tạo router và mount controllers
        Router router = new Router();
        router.use(new AuthMiddlewareStub());
        router.use(new CorsMiddleware());
        router.use(new LoggingMiddleware());
        router.use(new ErrorHandle());


        // Khởi động server
        HttpServer server = new HttpServer(port, router);
        server.start();



    }
}
