package org.miniboot.app.domain;

import org.miniboot.app.controllers.AppointmentController;
import org.miniboot.app.controllers.DoctorController;
import org.miniboot.app.controllers.HelloController;
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
import org.miniboot.app.util.HttpStub;

import java.util.*;

public class DomainMain {
    public static void main(String[] args) throws Exception {
        DoctorRepository doctorRepository = new InMemoryDoctorRepository();

        doctorRepository.saveAll(List.of(
                new Doctor(1, "Hậu", "Trần Văn", "123456"),
                new Doctor(2, "Dúng", "Dương Chí", "1236780"),
                new Doctor(3, "Toàn", "Phạm Minh", "71439"),
                new Doctor(4, "Hếu", "Phan Minh", "1637428"),
                new Doctor(5, "Trung", "Nuyễn Thành", "23647")
        ));

        DoctorController dc = new DoctorController(doctorRepository);

        Router router = new Router();
        router.use(new AuthMiddlewareStub());
        router.use(new CorsMiddleware());
        router.use(new LoggingMiddleware());
        router.use(new ErrorHandle());

        
        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        appointmentRepository.save(
                new Appointment(0, 1, "Dương Chí Dúng", "09:00", "2025-12-30")
        );
        appointmentRepository.save(
                new Appointment(0, 1, "Nguyễn Văn A", "10:00", "2025-12-30")
        );
        appointmentRepository.save(
                new Appointment(0, 2, "Trần Thị B", "09:30", "2025-12-30")
        );
        appointmentRepository.save(
                new Appointment(0, 2, "Lê Văn C", "11:00", "2025-12-31")
        );

        AppointmentController ac = new AppointmentController(appointmentRepository);

        //router.get("/appointments", ac.getAppointments());
        HttpStub stub = new HttpStub(router);
        HelloController.mount(router);
        DoctorController.mount(router, dc);
        //doctor
        var restList = stub.get("/hello");
        System.out.println("LIST => " + restList.status() + " " + restList.body());

        var restById = stub.get("/doctors?id=5");
        System.out.println("BY ID => " + restById.status() + " " + restById.body());
//
//        //GET ALL
//        var v1 = stub.get("/appointments");
//        System.out.println("LIST => " + v1.status() + " " + v1.body());
//
//        // get by id =2
//        var v2 = stub.get("/appointments?id=3");
//        System.out.println("BY ID => " + v2.status() + " " + v2.body());
//
//        // get filter doctorid + date
//        var v3 = stub.get("/appointments?doctorId=2&date=2025-12-30");
//        System.out.println("BY ID => " + v3.status() + " " + v3.body());
    }
}
