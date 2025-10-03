package org.miniboot.app.controllers;

import org.miniboot.app.AppConfig;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.Json;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DoctorController {
    private final DoctorRepository doctorRepository;

    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public static void mount(Router router, DoctorController dc) {
        router.get("/doctors", dc.getDoctors());
        router.post("/doctors", dc.createDoctor());
    }

    //POST /doctors
    public Function<HttpRequest, HttpResponse> createDoctor() {
        return (HttpRequest req) -> {
            try {
                System.out.println(new String(req.body, StandardCharsets.UTF_8));
                Doctor doctor = Json.fromBytes(req.body, Doctor.class);
                doctorRepository.saveDoctor(doctor);
                return Json.created(doctor);
            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.of(400,
                        "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    // GET /doctors
    // -- không có ?id= trả về danh sách bác sĩ
    // -- có ?id= thì trả về 1 bác sĩ có id

    public Function<HttpRequest, HttpResponse> getDoctors() {
        return (HttpRequest req) -> {
            Optional<Integer> idOpt = extractId(req.query);
//            if (idOpt.isPresent()) {
//                int id = idOpt.get();
//                var doctorOpt = doctorRepository.findById(id);
//                if (doctorOpt.isPresent()) {
//                    return Json.ok(doctorOpt.get());
//                } else {
//                    return HttpResponse.of(
//                            404,
//                            "text/plain; charset=utf-8",
//                            AppConfig.RESPONSE_404.getBytes()
//                    );
//                }
//            } else {
//                return Json.ok(doctorRepository.findAll());
//            }
            return idOpt.map(id -> doctorRepository.findById(id)
                            .map(Json::ok)
                            .orElse(HttpResponse.of(
                                    404,
                                    "text/plain; charset=utf-8",
                                    AppConfig.RESPONSE_404.getBytes(StandardCharsets.UTF_8)
                            )))
                    .orElseGet(() -> Json.ok(doctorRepository.findAll()));
        };
    }

    //helper
    private Optional<Integer> extractId(Map<String, List<String>> queries) {
        // path /doctor?id=
        if (queries == null) return Optional.empty();
        List<String> ids = queries.get("id");
        if (ids == null || ids.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(ids.get(0)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}

