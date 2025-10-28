package org.example.oop.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miniboot.app.domain.models.Employee;
import org.miniboot.app.util.GsonProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HttpEmployeeService {

     private final String baseUrl;
     private final String bearerToken; // null nếu không dùng JWT
     private static final Gson gson = GsonProvider.getGson();
     private final HttpClient httpClient = HttpClient.newHttpClient();
     private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

     public HttpEmployeeService() {
          this("http://localhost:8080", null);
     }

     public HttpEmployeeService(String baseUrl) {
          this(baseUrl, null);
     }

     public HttpEmployeeService(String baseUrl, String bearerToken) {
          this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
          this.bearerToken = bearerToken;
     }

     private HttpRequest.Builder reqBuilder(String path) {
          HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(DEFAULT_TIMEOUT);
          if (bearerToken != null && !bearerToken.isBlank()) {
               b.header("Authorization", "Bearer " + bearerToken);
          }
          return b;
     }

     private static String extractMessage(String json, String fallback) {
          try {
               var obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
               if (obj.has("message"))
                    return obj.get("message").getAsString();
               if (obj.has("error"))
                    return obj.get("error").getAsString();
          } catch (Exception ignore) {
          }
          return fallback;
     }

     public List<Employee> getAllEmployee() throws Exception {
          HttpRequest req = reqBuilder("/employees")
                    .GET()
                    .header("Accept", "application/json")
                    .build();

          HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
          if (res.statusCode() == 200) {
               var listType = new TypeToken<List<Employee>>() {
               }.getType();
               return gson.fromJson(res.body(), listType);
          }
          throw new Exception("Failed to fetch employees: HTTP " + res.statusCode() + " body=" + res.body());
     }

     public Employee getEmployeeById(int id) throws Exception {
          HttpRequest req = reqBuilder("/employees?id=" + id)
                    .GET()
                    .header("Accept", "application/json")
                    .build();

          HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
          if (res.statusCode() == 200) {
               return gson.fromJson(res.body(), Employee.class);
          } else if (res.statusCode() == 404) {
               throw new Exception("Không tìm thấy nhân viên");
          }
          throw new Exception("Failed to fetch employee: HTTP " + res.statusCode() + " body=" + res.body());
     }

     public Employee createEmployee(Employee employee) throws Exception {
          if (employee.getUsername() == null || employee.getUsername().isBlank())
               throw new IllegalArgumentException("username là bắt buộc");
          if (employee.getPassword() == null || employee.getPassword().isBlank())
               throw new IllegalArgumentException("password là bắt buộc");
          if (employee.getFirstname() == null || employee.getFirstname().isBlank()
                    || employee.getLastname() == null || employee.getLastname().isBlank())
               throw new IllegalArgumentException("firstname/lastname là bắt buộc");
          if (employee.getRole() == null)
               throw new IllegalArgumentException("role là bắt buộc");

          String roleLower = employee.getRole().toString().toLowerCase();
          if ("doctor".equals(roleLower) && (employee.getLicenseNo() == null || employee.getLicenseNo().isBlank()))
               throw new IllegalArgumentException("licenseNo là bắt buộc đối với bác sĩ");

          var body = new java.util.LinkedHashMap<String, Object>();
          body.put("username", employee.getUsername());
          body.put("password", employee.getPassword());
          body.put("firstname", employee.getFirstname());
          body.put("lastname", employee.getLastname());
          body.put("role", roleLower);
          if (employee.getLicenseNo() != null)
               body.put("licenseNo", employee.getLicenseNo());
          if (employee.getEmail() != null)
               body.put("email", employee.getEmail());
          if (employee.getPhone() != null)
               body.put("phone", employee.getPhone());
          if (employee.getAvatar() != null)
               body.put("avatar", employee.getAvatar());
          body.put("active", employee.isActive());

          String jsonBody = gson.toJson(body);

          HttpRequest req = reqBuilder("/employees")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

          HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
          int code = res.statusCode();
          if (code == 200 || code == 201) {
               return gson.fromJson(res.body(), Employee.class);
          } else if (code == 409) {
               throw new Exception(extractMessage(res.body(), "Username hoặc email đã tồn tại"));
          } else if (code == 400) {
               throw new Exception(extractMessage(res.body(), "Dữ liệu không hợp lệ"));
          }
          throw new Exception("Failed to create employee: HTTP " + code + " body=" + res.body());
     }

     public Employee updateEmployee(Employee employee) throws Exception {
          Map<String, Object> body = new HashMap<>();
          body.put("id", employee.getId());
          body.put("firstname", employee.getFirstname());
          body.put("lastname", employee.getLastname());
          if (employee.getRole() != null)
               body.put("role", employee.getRole().toString().toLowerCase());
          body.put("licenseNo", employee.getLicenseNo());
          body.put("email", employee.getEmail());
          body.put("phone", employee.getPhone());
          body.put("avatar", employee.getAvatar());
          body.put("active", employee.isActive());

          String jsonBody = gson.toJson(body);

          HttpRequest req = reqBuilder("/employees")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

          HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
          int code = res.statusCode();
          if (code == 200) {
               return gson.fromJson(res.body(), Employee.class);
          } else if (code == 404) {
               throw new Exception("Không tìm thấy nhân viên");
          } else if (code == 409) {
               throw new Exception(extractMessage(res.body(), "Username hoặc email đã tồn tại"));
          } else if (code == 400) {
               throw new Exception(extractMessage(res.body(), "Dữ liệu không hợp lệ"));
          }
          throw new Exception("Failed to update employee: HTTP " + code + " body=" + res.body());
     }

     public boolean deleteEmployee(int id) throws Exception {
          // Giữ theo BE hiện tại: id trên query
          HttpRequest req = reqBuilder("/employees?id=" + id)
                    .DELETE()
                    .header("Accept", "application/json")
                    .build();

          HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
          int code = res.statusCode();
          if (code == 200 || code == 204)
               return true;
          if (code == 404)
               throw new Exception("Không tìm thấy nhân viên");
          throw new Exception("Failed to delete employee: HTTP " + code + " body=" + res.body());
     }

     public List<Employee> searchEmployees(String keyword) throws Exception {
          String q = java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
          HttpRequest req = reqBuilder("/employees/search?keyword=" + q)
                    .GET()
                    .header("Accept", "application/json")
                    .build();

          HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
          if (res.statusCode() == 200) {
               var listType = new TypeToken<List<Employee>>() {
               }.getType();
               return gson.fromJson(res.body(), listType);
          }
          throw new Exception("HTTP Error " + res.statusCode() + " body=" + res.body());
     }

     public List<Employee> getEmployeesByRole(String role) throws Exception {
          String roleLower = role == null ? "" : role.toLowerCase();
          String q = java.net.URLEncoder.encode(roleLower, java.nio.charset.StandardCharsets.UTF_8);
          HttpRequest req = reqBuilder("/employees/role?role=" + q)
                    .GET()
                    .header("Accept", "application/json")
                    .build();

          HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
          if (res.statusCode() == 200) {
               var listType = new TypeToken<List<Employee>>() {
               }.getType();
               return gson.fromJson(res.body(), listType);
          } else if (res.statusCode() == 400) {
               throw new Exception(
                         extractMessage(res.body(), "Vai trò không hợp lệ. Chỉ chấp nhận 'doctor' hoặc 'nurse'"));
          }
          throw new Exception("HTTP Error " + res.statusCode() + " body=" + res.body());
     }
}
