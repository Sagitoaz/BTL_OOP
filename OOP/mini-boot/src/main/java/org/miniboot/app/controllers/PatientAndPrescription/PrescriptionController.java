package org.miniboot.app.controllers.PatientAndPrescription;

import com.google.gson.Gson;
import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;
import org.miniboot.app.domain.repo.PatientAndPrescription.PrescriptionRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.GsonProvider;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PrescriptionController
{
    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionController(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }
    public static void mount(Router router, PrescriptionController pc) {
        router.post("/prescriptions", pc.createPrescription());
        router.get("/prescriptions", pc.getPrescription());
        router.put("/prescriptions", pc.updatePrescription());
    }

    public Function<HttpRequest, HttpResponse> createPrescription() {
        return (HttpRequest req) -> {

            try {
                System.out.println("🔍 Handling POST /prescriptions request");
                Gson gson = GsonProvider.getGson();
                String jsonBody = new String(req.body, StandardCharsets.UTF_8);
                Prescription newPrescription = gson.fromJson(jsonBody, Prescription.class);

                System.out.println("🔄 Attempting to create prescription: " + newPrescription.getId());
                System.out.println("📅 Date fields - created_at: " + newPrescription.getCreated_at() +
                                 ", updated_at: " + newPrescription.getUpdated_at() +
                                 ", signedAt: " + newPrescription.getSignedAt());

                Prescription savedPrescription = prescriptionRepository.save(newPrescription);
                if (savedPrescription != null && savedPrescription.getId() > 0) {
                    String jsonResponse = gson.toJson(savedPrescription);
                    System.out.println("✅ Prescription created successfully with ID: " + savedPrescription.getId());
                    return HttpResponse.of(201, "application/json", jsonResponse.getBytes(StandardCharsets.UTF_8));
                } else {
                    System.err.println("❌ Prescription creation failed - no prescription returned");
                    return HttpResponse.of(500, "text/plain; charset=utf-8",
                            "Internal Server Error: Failed to create prescription".getBytes(StandardCharsets.UTF_8));
                }
            }
            catch (RuntimeException e) {
                // Database errors từ repository
                System.err.println("❌ Database error creating prescription: " + e.getMessage());
                return HttpResponse.of(500, "text/plain; charset=utf-8",
                        ("Database Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                // JSON parsing hoặc lỗi khác
                System.err.println("❌ General error creating prescription: " + e.getMessage());
                e.printStackTrace();
                return HttpResponse.of(400, "text/plain; charset=utf-8",
                        HttpConstants.REASON_BAD_REQUEST.getBytes(StandardCharsets.UTF_8));
            }

        };
    }
    public Function<HttpRequest, HttpResponse> getPrescription() {
        return (HttpRequest req) -> {
            System.out.println("🔍 Handling GET /prescriptions request");
            Gson gson = GsonProvider.getGson();
            Optional<String> customerIdParam = extractFirst(req.query, "customer_id");
            Optional<String> appointmentIdParam = extractFirst(req.query, "appointment_id");
            if(customerIdParam.isPresent()) {
                try {
                    int id = Integer.parseInt(customerIdParam.get());
                    List<Prescription> prescriptions = prescriptionRepository.findByCustomerId(id);

                    if (!prescriptions.isEmpty()) {
                        String jsonResponse = gson.toJson(prescriptions);
                        return HttpResponse.of(200, "application/json", jsonResponse.getBytes(StandardCharsets.UTF_8));
                    } else {
                        return HttpResponse.of(404, "text/plain; charset=utf-8",
                                "Prescription not found".getBytes(StandardCharsets.UTF_8));
                    }
                } catch (NumberFormatException e) {
                    return HttpResponse.of(400, "text/plain; charset=utf-8",
                            "Invalid ID format".getBytes(StandardCharsets.UTF_8));
                }
            }
            else{
                if(appointmentIdParam.isPresent()) {
                    try {
                        int id = Integer.parseInt(appointmentIdParam.get());
                        List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(id);


                        if (!prescriptions.isEmpty()) {
                            String jsonResponse = gson.toJson(prescriptions);
                            return HttpResponse.of(200, "application/json", jsonResponse.getBytes(StandardCharsets.UTF_8));
                        } else {
                            return HttpResponse.of(404, "text/plain; charset=utf-8",
                                    "Prescription by appointment ID not found".getBytes(StandardCharsets.UTF_8));
                        }
                    } catch (NumberFormatException e) {
                        return HttpResponse.of(400, "text/plain; charset=utf-8",
                                "Invalid ID format".getBytes(StandardCharsets.UTF_8));
                    }
                }
                else{
                    List<Prescription> prescriptions = prescriptionRepository.findAll();
                    String jsonResponse = gson.toJson(prescriptions);
                    return HttpResponse.of(200, "application/json", jsonResponse.getBytes(StandardCharsets.UTF_8));

                }

            }
        };
    }
    public Function<HttpRequest, HttpResponse> updatePrescription() {
        return (HttpRequest req)->{
          try{
              Gson gson = GsonProvider.getGson();
              String jsonBody = new String(req.body, StandardCharsets.UTF_8);
              Prescription toChangePrescription = gson.fromJson(jsonBody, Prescription.class);
              Prescription savedPrescription = prescriptionRepository.save(toChangePrescription);
              if(savedPrescription != null && savedPrescription.getId() > 0) {
                  String jsonResponse = gson.toJson(savedPrescription);
                  return HttpResponse.of(200, "application/json", jsonResponse.getBytes(StandardCharsets.UTF_8));

              }
              else{
                  System.err.println("❌ Prescription update failed - no prescription returned");
                  return HttpResponse.of(500, "text/plain; charset=utf-8",
                          "Internal Server Error: Failed to update prescription".getBytes(StandardCharsets.UTF_8));
              }
          }
          catch (RuntimeException e) {
              // Database errors từ repository
              System.err.println("❌ Database error updating prescription: " + e.getMessage());
              return HttpResponse.of(500, "text/plain; charset=utf-8",
                      ("Database Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
          } catch (Exception e) {
              // JSON parsing hoặc lỗi khác
              System.err.println("❌ General error updating prescription: " + e.getMessage());
              e.printStackTrace();
              return HttpResponse.of(400, "text/plain; charset=utf-8",
                      HttpConstants.REASON_BAD_REQUEST.getBytes(StandardCharsets.UTF_8));
          }
        };
    }

    private Optional<Integer> extractInt(Map<String, List<String>> q, String key) {
        Optional<String> s = extractFirst(q, key);
        if (s.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(s.get()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    private Optional<String> extractFirst(Map<String, List<String>> q, String key) {
        if (q == null) return Optional.empty();
        List<String> vals = q.get(key);
        if (vals == null || vals.isEmpty()) return Optional.empty();
        String first = vals.get(0);
        return (first == null || first.isBlank()) ? Optional.empty() : Optional.of(first);
    }
}
