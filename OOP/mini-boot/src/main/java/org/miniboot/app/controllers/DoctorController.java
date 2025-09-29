package org.miniboot.app.controllers;

import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.router.Handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DoctorController {
    private final DoctorRepository doctorRepository;

    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    //get//doctor
    public Handler listDoctor() {
        return null;
    }

    //get/doctor/{id}
    public Handler getDoctorById(String doctorId) {
        return null;
    }

    //helper
    private Optional<Integer> extractId(HttpRequest request) {
        // path /doctor/{id}
        String path = request.path;
        if (path != null && path.startsWith("/doctors/")) {
            String tail = path.substring("/doctors/".length()).trim();
            if (!tail.isEmpty() && allDigits(tail)) {
                try {
                    return Optional.of(Integer.parseInt(tail));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        // đọc từ query ?id=
        Map<String, List<String>> q = request.query;
        if (q != null && !q.isEmpty()) {
            List<String> ids = q.get("id");
            if (ids != null && !ids.isEmpty()) {
                try {
                    return Optional.of(Integer.parseInt(ids.get(0)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return Optional.empty();
    }

    private boolean allDigits(String str) {
        for (int i = 0; i < str.length(); i++)
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        return true;
    }

}

