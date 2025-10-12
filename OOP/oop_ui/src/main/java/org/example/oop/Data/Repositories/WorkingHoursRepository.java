package org.example.oop.Data.Repositories;

import org.example.oop.Model.Schedule.WorkingHours;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkingHoursRepository {
    private static final String DATA_FILE = "data/ScheduleTestData/WorkingHours.txt";
    private ArrayList<WorkingHours> workingHours;

    public ArrayList<WorkingHours> findAll() {
        return workingHours;
    }

    public List<WorkingHours> findByDoctorId(int doctorId) {
        return workingHours.stream()
                .filter(wh -> wh.getDoctorId() == doctorId)
                .collect(Collectors.toList());
    }

    public Optional<WorkingHours> findByDoctorIdAndDay(int doctorId, DayOfWeek day) {
        return workingHours.stream()
                .filter(wh -> wh.getDoctorId() == doctorId && wh.getDayOfWeek() == day)
                .findFirst();
    }

    public void save(WorkingHours workingHour) {
        workingHour.validate();

        workingHours.removeIf(wh ->
                wh.getDoctorId() == workingHour.getDoctorId() &&
                        wh.getDayOfWeek() == workingHour.getDayOfWeek()
        );

        workingHours.add(workingHour);
        saveToFile();
    }

    public void delete(int doctorId, DayOfWeek day) {
        workingHours.removeIf(wh ->
                wh.getDoctorId() == doctorId && wh.getDayOfWeek() == day
        );
        saveToFile();
    }

    private void loadFromFile() {
        try {
            if (Files.exists(Paths.get(DATA_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(DATA_FILE));
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        workingHours.add(WorkingHours.fromDataString(line));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading working hours: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            Files.createDirectories(Paths.get(DATA_FILE).getParent());
            List<String> lines = workingHours.stream()
                    .map(WorkingHours::toDataString)
                    .collect(Collectors.toList());
            Files.write(Paths.get(DATA_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving working hours: " + e.getMessage());
        }
    }
}
