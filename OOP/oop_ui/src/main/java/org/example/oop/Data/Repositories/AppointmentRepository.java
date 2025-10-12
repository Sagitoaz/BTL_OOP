package org.example.oop.Data.Repositories;

import org.example.oop.Model.Schedule.Appointment;
import org.example.oop.Model.Schedule.AppointmentStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppointmentRepository {
    private static final String DATA_FILE = "data/ScheduleTestData/Appointments.txt";
    private ArrayList<Appointment> appointments;
    private int nextId;

    public AppointmentRepository() {
        this.appointments = new ArrayList<>();
        loadFromFile();
        this.nextId = appointments.isEmpty() ? 1 :
                appointments.stream().mapToInt(Appointment::getId).max().orElse(0) + 1;
    }

    public ArrayList<Appointment> findAll() {
        return appointments;
    }

    public Optional<Appointment> findById(int id) {
        return appointments.stream().filter(a -> a.getId() == id).findFirst();
    }

    public List<Appointment> findByDoctorId(int doctorId) {
        return appointments.stream().filter(a -> a.getDoctorId() == doctorId).collect(Collectors.toList());
    }

    public List<Appointment> findByPatientId(int patientId) {
        return appointments.stream().filter(a -> a.getCustomerId() == patientId).collect(Collectors.toList());
    }

    public List<Appointment> findByDoctorAndDate(int doctorId, LocalDate date) {
        return appointments.stream().filter(a -> a.getDoctorId() == doctorId)
                .filter(a -> a.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Appointment> findByDateRange(LocalDate from, LocalDate to) {
        return appointments.stream()
                .filter(a -> {
                    LocalDate apptDate = a.getStartTime().toLocalDate();
                    return !apptDate.isBefore(from) && !apptDate.isAfter(to);
                })
                .collect(Collectors.toList());
    }

    public List<Appointment> findByStatus(AppointmentStatus status) {
        return appointments.stream().filter(a -> a.getAppointmentStatus() == status).collect(Collectors.toList());
    }

    public void save(Appointment appointment) {
        appointment.validate();
        if (appointment.getId() == 0) {
            appointment.setId(nextId++);
            appointment.setCreatedAt(LocalDateTime.now());
        }
        appointment.setUpdatedAt(LocalDateTime.now());

        appointments.add(appointment);
        saveToFile();
    }

    public void update(Appointment appointment) {
        appointment.validate();
        appointment.setUpdatedAt(LocalDateTime.now());

        int index = findIndexById(appointment.getId());
        if (index >= 0) {
            appointments.set(index, appointment);
            saveToFile();
        } else {
            throw new IllegalArgumentException("Appointment not found: " + appointment.getId());
        }
    }

    public void delete(int id) {
        appointments.removeIf(a -> a.getId() == id);
        saveToFile();
    }

    private int findIndexById(int id) {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void loadFromFile() {
        try {
            if (Files.exists(Paths.get(DATA_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(DATA_FILE));
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        appointments.add(Appointment.fromDataString(line));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            Files.createDirectories(Paths.get(DATA_FILE).getParent());
            List<String> lines = appointments.stream().map(Appointment::toDataString).toList();

            Files.write(Paths.get(DATA_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }

    public ArrayList<Appointment> findConflicts() {
        ArrayList<Appointment> conflicts = new ArrayList<>();
        for (int i = 0; i < appointments.size(); i++) {
            for (int j = i + 1; j < appointments.size(); j++) {
                Appointment a1 = appointments.get(i);
                Appointment a2 = appointments.get(j);
                if (a1.getDoctorId() == a2.getDoctorId() && hasTimeOverlap(a1, a2)) {
                    if (!conflicts.contains(a1)) conflicts.add(a1);
                    if (!conflicts.contains(a2)) conflicts.add(a2);
                }
            }
        }
        return conflicts;
    }

    private boolean hasTimeOverlap(Appointment a1, Appointment a2) {
        return a1.getStartTime().isBefore(a2.getEndTime()) &&
                a2.getStartTime().isBefore(a1.getEndTime());
    }
}
