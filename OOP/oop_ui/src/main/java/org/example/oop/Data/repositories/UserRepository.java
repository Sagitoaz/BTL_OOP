package org.example.oop.Data.Repositories;

import org.example.oop.Data.models.*;
import org.example.oop.Data.storage.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UserRepository - Lớp lưu/đọc dữ liệu người dùng từ file.
 *
 * CẬP NHẬT theo database mới:
 * - User giờ là interface với 3 implementation: Admin, Employee, Customer
 * - Mỗi loại user có file riêng: admins.txt, employees.txt, customers.txt
 * - Format: TYPE|id|username|password|email|...other_fields
 */
public class UserRepository implements DataRepository<User> {
    private static final String ADMIN_FILE = "admins.txt";
    private static final String EMPLOYEE_FILE = "employees.txt";
    private static final String CUSTOMER_FILE = "customers.txt";
    private final FileManager fileManager;

    public UserRepository() {
        this.fileManager = new FileManager();
    }

    @Override
    public User save(User user) {
        try {
            if (exists(user.getId())) {
                update(user);
            } else {
                String filename = getFilenameForUser(user);
                String data = serializeUser(user);
                fileManager.appendLine(filename, data);
            }
            return user;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        return findAll().stream()
                .filter(u -> u.getId() == id)
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        List<User> allUsers = new ArrayList<>();

        // Load admins
        try {
            List<String> adminLines = fileManager.readLines(ADMIN_FILE);
            for (String line : adminLines) {
                if (isValidLine(line)) {
                    User admin = deserializeAdmin(line);
                    if (admin != null) allUsers.add(admin);
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }

        // Load employees
        try {
            List<String> empLines = fileManager.readLines(EMPLOYEE_FILE);
            for (String line : empLines) {
                if (isValidLine(line)) {
                    User emp = deserializeEmployee(line);
                    if (emp != null) allUsers.add(emp);
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }

        // Load customers
        try {
            List<String> custLines = fileManager.readLines(CUSTOMER_FILE);
            for (String line : custLines) {
                if (isValidLine(line)) {
                    User cust = deserializeCustomer(line);
                    if (cust != null) allUsers.add(cust);
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }

        return allUsers;
    }

    @Override
    public void update(User user) {
        try {
            // Xóa user cũ và thêm mới
            delete(user.getId());

            String filename = getFilenameForUser(user);
            String data = serializeUser(user);
            fileManager.appendLine(filename, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            // Xóa từ tất cả các file
            deleteFromFile(ADMIN_FILE, id);
            deleteFromFile(EMPLOYEE_FILE, id);
            deleteFromFile(CUSTOMER_FILE, id);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public boolean exists(int id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        return findAll().size();
    }

    // Custom queries
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public List<User> findByRole(UserRole role) {
        return findAll().stream()
                .filter(u -> u.getUserRole() == role)
                .collect(Collectors.toList());
    }

    // Helper methods
    private boolean isValidLine(String line) {
        return line != null && !line.trim().isEmpty() && !line.trim().startsWith("#");
    }

    private String getFilenameForUser(User user) {
        if (user instanceof Admin) return ADMIN_FILE;
        if (user instanceof Employee) return EMPLOYEE_FILE;
        if (user instanceof Customer) return CUSTOMER_FILE;
        throw new IllegalArgumentException("Unknown user type: " + user.getClass());
    }

    private String serializeUser(User user) {
        if (user instanceof Admin) {
            Admin admin = (Admin) user;
            // Format: id|username|password|email|is_active (KHÔNG có "ADMIN|" ở đầu)
            return String.format("%d|%s|%s|%s|%b",
                admin.getId(), admin.getUsername(), admin.getPassword(),
                admin.getEmail(), admin.isActive());
        } else if (user instanceof Employee) {
            Employee emp = (Employee) user;
            // Format: id|username|password|firstname|lastname|avatar|role|license_no|email|phone|is_active|created_at
            return String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s|%b|%s",
                emp.getId(), emp.getUsername(), emp.getPassword(),
                emp.getFirstname(), emp.getLastname(),
                emp.getAvatar() != null ? emp.getAvatar() : "",
                emp.getRole().getValue(),
                emp.getLicenseNo() != null ? emp.getLicenseNo() : "",
                emp.getEmail(),
                emp.getPhone() != null ? emp.getPhone() : "",
                emp.isActive(),
                emp.getCreatedAt() != null ? emp.getCreatedAt().toString() : "");
        } else if (user instanceof Customer) {
            Customer cust = (Customer) user;
            // Format: id|username|password|firstname|lastname|phone|email|dob|gender|address|note|created_at
            return String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                cust.getId(),
                cust.getUsername(),
                cust.getPassword(),
                cust.getFirstname(),
                cust.getLastname(),
                cust.getPhone() != null ? cust.getPhone() : "",
                cust.getEmail() != null ? cust.getEmail() : "",
                cust.getDob() != null ? cust.getDob().toString() : "",
                cust.getGender() != null ? cust.getGender() : "",
                cust.getAddress() != null ? cust.getAddress() : "",
                cust.getNote() != null ? cust.getNote() : "",
                cust.getCreatedAt() != null ? cust.getCreatedAt().toString() : "");
        }
        throw new IllegalArgumentException("Unknown user type");
    }

    private Admin deserializeAdmin(String line) {
        try {
            String[] parts = line.split("\\|", -1);
            // Format: id|username|password|email|is_active (5 trường, index 0-4)
            if (parts.length < 5) return null;

            int id = Integer.parseInt(parts[0]);  // Đổi từ parts[1] thành parts[0]
            String username = parts[1];           // Đổi từ parts[2] thành parts[1]
            String password = parts[2];           // Đổi từ parts[3] thành parts[2]
            String email = parts[3];              // Đổi từ parts[4] thành parts[3]
            boolean active = Boolean.parseBoolean(parts[4]); // Đổi từ parts[5] thành parts[4]

            return new Admin(id, username, password, email, active);
        } catch (Exception e) {
            System.err.println("Error deserializing admin: " + e.getMessage());
            return null;
        }
    }

    private Employee deserializeEmployee(String line) {
        try {
            String[] parts = line.split("\\|", -1);
            // Format: id|username|password|firstname|lastname|avatar|role|license_no|email|phone|is_active|created_at
            if (parts.length < 12) return null;

            int id = Integer.parseInt(parts[0]);     // Đổi từ parts[1]
            String username = parts[1];              // Đổi từ parts[2]
            String password = parts[2];              // Đổi từ parts[3]
            String firstname = parts[3];             // Đổi từ parts[4]
            String lastname = parts[4];              // Đổi từ parts[5]
            String avatar = parts[5].isEmpty() ? null : parts[5];    // Đổi từ parts[6]
            String roleStr = parts[6];               // Đổi từ parts[7]
            String licenseNo = parts[7].isEmpty() ? null : parts[7]; // Đổi từ parts[8]
            String email = parts[8];                 // Đổi từ parts[9]
            String phone = parts[9].isEmpty() ? null : parts[9];     // Đổi từ parts[10]
            boolean active = Boolean.parseBoolean(parts[10]);        // Đổi từ parts[11]

            Employee emp = new Employee();
            emp.setId(id);
            emp.setUsername(username);
            emp.setPassword(password);
            emp.setFirstname(firstname);
            emp.setLastname(lastname);
            emp.setAvatar(avatar);
            emp.setRole(EmployeeRole.fromValue(roleStr));
            emp.setLicenseNo(licenseNo);
            emp.setEmail(email);
            emp.setPhone(phone);
            emp.setActive(active);

            return emp;
        } catch (Exception e) {
            System.err.println("Error deserializing employee: " + e.getMessage());
            return null;
        }
    }

    private Customer deserializeCustomer(String line) {
        try {
            String[] parts = line.split("\\|", -1);
            // Format: id|username|password|firstname|lastname|phone|email|dob|gender|address|note|created_at
            if (parts.length < 12) return null;

            int id = Integer.parseInt(parts[0]);     // Đổi từ parts[1]
            String username = parts[1];              // Đổi từ parts[2]
            String password = parts[2];              // Đổi từ parts[3]
            String firstname = parts[3];             // Đổi từ parts[4]
            String lastname = parts[4];              // Đổi từ parts[5]
            String phone = parts[5].isEmpty() ? null : parts[5];     // Đổi từ parts[6]
            String email = parts[6].isEmpty() ? null : parts[6];     // Đổi từ parts[7]

            Customer cust = new Customer();
            cust.setId(id);
            cust.setUsername(username);
            cust.setPassword(password);
            cust.setFirstname(firstname);
            cust.setLastname(lastname);
            cust.setPhone(phone);
            cust.setEmail(email);

            // Parse optional fields
            if (parts.length > 7 && !parts[7].isEmpty()) {
                cust.setDob(java.time.LocalDate.parse(parts[7]));
            }
            if (parts.length > 8 && !parts[8].isEmpty()) {
                cust.setGender(Gender.fromString(parts[8]));
            }
            if (parts.length > 9 && !parts[9].isEmpty()) {
                cust.setAddress(parts[9]);
            }
            if (parts.length > 10 && !parts[10].isEmpty()) {
                cust.setNote(parts[10]);
            }

            return cust;
        } catch (Exception e) {
            System.err.println("Error deserializing customer: " + e.getMessage());
            return null;
        }
    }

    private void deleteFromFile(String filename, int id) throws IOException {
        try {
            List<String> lines = fileManager.readLines(filename);
            List<String> filtered = lines.stream()
                    .filter(line -> {
                        if (!isValidLine(line)) return true;
                        String[] parts = line.split("\\|");
                        if (parts.length < 1) return true;
                        try {
                            int lineId = Integer.parseInt(parts[0]); // SỬA: Đổi từ parts[1] thành parts[0]
                            return lineId != id;
                        } catch (Exception e) {
                            return true;
                        }
                    })
                    .collect(Collectors.toList());
            fileManager.writeLines(filename, filtered);
        } catch (IOException e) {
            // File might not exist
        }
    }
}