package org.miniboot.app.controllers;

import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;

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

    // GET /doctors
    // -- không có ?id= trả về danh sách bác sĩ
    // -- có ?id= thì trả về 1 bác sĩ có id

    public Function<HttpRequest, HttpResponse> getDoctors() {
        return (HttpRequest req) -> {
            Optional<Integer> idOpt = extractId(req.query);
            if (idOpt.isPresent()) {
                return doctorRepository.findById(idOpt.get())
                        .map(doctor -> HttpResponse.json(200, toJson(doctor)))
                        .orElse(HttpResponse.of(
                                404,
                                "text/plain; charset=utf-8",
                                "Doctor not found".getBytes(StandardCharsets.UTF_8)
                        ));
            }
            return HttpResponse.json(200, toJsonList(doctorRepository.findAll()));
        };
    }

    //helper
    private Optional<Integer> extractId(Map<String, List<String>> queries) {
        // path /doctor/{id}
        if (queries == null) return Optional.empty();
        List<String> ids = queries.get("id");
        if (ids == null || ids.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(ids.get(0)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String toJson(Object object) {
        return "\"" + object.toString().replace("\"", "\\\"") + "\"";
    }

    private String toJsonList(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toJson(list.get(i)));
            if (i < list.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}

