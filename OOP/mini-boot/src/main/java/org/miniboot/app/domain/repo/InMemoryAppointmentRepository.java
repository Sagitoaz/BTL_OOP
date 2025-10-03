package org.miniboot.app.domain.repo;

import org.miniboot.app.domain.models.Appointment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryAppointmentRepository implements AppointmentRepository {
    private final Map<Integer, Appointment> data = new ConcurrentHashMap<>();

    private final AtomicInteger counter = new AtomicInteger(0);


    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Optional<Appointment> findById(Integer id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Appointment save(Appointment appointment) {
        // nếu id chưa gán thì tự tăng
        if (appointment.getId() == 0) appointment.setId(counter.incrementAndGet());
        data.put(appointment.getId(), appointment);
        return appointment;
    }

    @Override
    public List<Appointment> findByDoctorIdAndDate(Integer doctorId, String date) {
        List<Appointment> res = new ArrayList<>();
        for (Appointment a : data.values()) {
            if (a.getDoctorId() == doctorId && date.equals(a.getDate())) {
                res.add(a);
            }
        }
        // Tùy chọn: sắp xếp theo startTime cho đẹp
        res.sort(Comparator.comparing(Appointment::getStartTime));
        return res;
    }

    //tiện ích cho việc trùng slot
    public boolean existsSlot(Integer id, String date, String startTime) {
        for (Appointment a : data.values()) {
            if (a.getId() == id && a.getDate().equals(date) && a.getStartTime().equals(startTime)) {
                return true;
            }
        }
        return false;
    }
}
