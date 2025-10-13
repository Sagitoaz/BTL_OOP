package org.miniboot.app.domain.repo;

import org.miniboot.app.domain.models.User;
import org.miniboot.app.domain.models.Admin;
import org.miniboot.app.domain.models.Employee;
import org.miniboot.app.domain.models.Customer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository for managing user data
 *
 * Đã cập nhật theo database mới:
 * - Đọc từ 3 file riêng biệt: admins.txt, employees.txt, customers.txt
 * - User giờ là interface, trả về Admin/Employee/Customer cụ thể
 * - Format file:
 *   + admins.txt: id|username|password|email|is_active
 *   + employees.txt: id|username|password|firstname|lastname|avatar|role|license_no|email|phone|is_active|created_at
 *   + customers.txt: id|username|password|firstname|lastname|phone|email|dob|gender|address|note|created_at
 */
public class UserRepository {
    private static final String ADMINS_FILE = "oop_ui/src/main/resources/Data/admins.txt";
    private static final String EMPLOYEES_FILE = "oop_ui/src/main/resources/Data/employees.txt";
    private static final String CUSTOMERS_FILE = "oop_ui/src/main/resources/Data/customers.txt";

    /**
     * Find all users from all 3 tables
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        users.addAll(loadAdmins());
        users.addAll(loadEmployees());
        users.addAll(loadCustomers());
        return users;
    }

    /**
     * Load admins from admins.txt
     * Format: id|username|password|email|is_active
     */
    private List<Admin> loadAdmins() {
        List<Admin> admins = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(ADMINS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 5) {
                    Admin admin = new Admin();
                    admin.setId(parts[0]);
                    admin.setUsername(parts[1]);
                    admin.setPassword(parts[2]);
                    admin.setEmail(parts[3].isEmpty() ? null : parts[3]);
                    admin.setActive(Boolean.parseBoolean(parts[4]));
                    admins.add(admin);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading admins file: " + e.getMessage());
        }
        return admins;
    }

    /**
     * Load employees from employees.txt
     * Format: id|username|password|firstname|lastname|avatar|role|license_no|email|phone|is_active|created_at
     */
    private List<Employee> loadEmployees() {
        List<Employee> employees = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(EMPLOYEES_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 12) {
                    Employee employee = new Employee();
                    employee.setId(parts[0]);
                    employee.setUsername(parts[1]);
                    employee.setPassword(parts[2]);
                    employee.setFirstname(parts[3]);
                    employee.setLastname(parts[4]);
                    employee.setAvatar(parts[5].isEmpty() ? null : parts[5]);
                    employee.setEmployeeRole(parts[6]);
                    employee.setLicenseNo(parts[7].isEmpty() ? null : parts[7]);
                    employee.setEmail(parts[8].isEmpty() ? null : parts[8]);
                    employee.setPhone(parts[9].isEmpty() ? null : parts[9]);
                    employee.setActive(Boolean.parseBoolean(parts[10]));
                    employee.setCreatedAt(parts[11].isEmpty() ? null : LocalDateTime.parse(parts[11]));
                    employees.add(employee);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading employees file: " + e.getMessage());
        }
        return employees;
    }

    /**
     * Load customers from customers.txt
     * Format: id|username|password|firstname|lastname|phone|email|dob|gender|address|note|created_at
     */
    private List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(CUSTOMERS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 12) {
                    Customer customer = new Customer();
                    customer.setId(parts[0]);
                    customer.setUsername(parts[1]);
                    customer.setPassword(parts[2]);
                    customer.setFirstname(parts[3]);
                    customer.setLastname(parts[4]);
                    customer.setPhone(parts[5].isEmpty() ? null : parts[5]);
                    customer.setEmail(parts[6].isEmpty() ? null : parts[6]);
                    customer.setDob(parts[7].isEmpty() ? null : LocalDate.parse(parts[7]));
                    customer.setGender(parts[8].isEmpty() ? null : parts[8]);
                    customer.setAddress(parts[9].isEmpty() ? null : parts[9]);
                    customer.setNote(parts[10].isEmpty() ? null : parts[10]);
                    customer.setCreatedAt(parts[11].isEmpty() ? null : LocalDateTime.parse(parts[11]));
                    customers.add(customer);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading customers file: " + e.getMessage());
        }
        return customers;
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(String id) {
        return findAll().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Save user (create or update)
     * Xác định loại user dựa trên instance và lưu vào file tương ứng
     */
    public User save(User user) {
        if (user instanceof Admin) {
            return saveAdmin((Admin) user);
        } else if (user instanceof Employee) {
            return saveEmployee((Employee) user);
        } else if (user instanceof Customer) {
            return saveCustomer((Customer) user);
        }
        throw new IllegalArgumentException("Unknown user type: " + user.getClass().getName());
    }

    private Admin saveAdmin(Admin admin) {
        List<Admin> admins = loadAdmins();
        admins.removeIf(a -> a.getId().equals(admin.getId()));
        admins.add(admin);
        writeAdminsToFile(admins);
        return admin;
    }

    private Employee saveEmployee(Employee employee) {
        List<Employee> employees = loadEmployees();
        employees.removeIf(e -> e.getId().equals(employee.getId()));
        employees.add(employee);
        writeEmployeesToFile(employees);
        return employee;
    }

    private Customer saveCustomer(Customer customer) {
        List<Customer> customers = loadCustomers();
        customers.removeIf(c -> c.getId().equals(customer.getId()));
        customers.add(customer);
        writeCustomersToFile(customers);
        return customer;
    }

    /**
     * Delete user by ID
     */
    public boolean deleteById(String id) {
        boolean deleted = false;

        List<Admin> admins = loadAdmins();
        if (admins.removeIf(a -> a.getId().equals(id))) {
            writeAdminsToFile(admins);
            deleted = true;
        }

        List<Employee> employees = loadEmployees();
        if (employees.removeIf(e -> e.getId().equals(id))) {
            writeEmployeesToFile(employees);
            deleted = true;
        }

        List<Customer> customers = loadCustomers();
        if (customers.removeIf(c -> c.getId().equals(id))) {
            writeCustomersToFile(customers);
            deleted = true;
        }

        return deleted;
    }

    /**
     * Write admins to file
     * Format: id|username|password|email|is_active
     */
    private void writeAdminsToFile(List<Admin> admins) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|username|password|email|is_active");
        for (Admin admin : admins) {
            String line = String.join("|",
                    admin.getId(),
                    admin.getUsername(),
                    admin.getPassword(),
                    admin.getEmail() != null ? admin.getEmail() : "",
                    String.valueOf(admin.isActive())
            );
            lines.add(line);
        }
        try {
            Files.write(Paths.get(ADMINS_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error writing admins file: " + e.getMessage());
        }
    }

    /**
     * Write employees to file
     * Format: id|username|password|firstname|lastname|avatar|role|license_no|email|phone|is_active|created_at
     */
    private void writeEmployeesToFile(List<Employee> employees) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|username|password|firstname|lastname|avatar|role|license_no|email|phone|is_active|created_at");
        for (Employee emp : employees) {
            String line = String.join("|",
                    emp.getId(),
                    emp.getUsername(),
                    emp.getPassword(),
                    emp.getFirstname(),
                    emp.getLastname(),
                    emp.getAvatar() != null ? emp.getAvatar() : "",
                    emp.getEmployeeRole(),
                    emp.getLicenseNo() != null ? emp.getLicenseNo() : "",
                    emp.getEmail() != null ? emp.getEmail() : "",
                    emp.getPhone() != null ? emp.getPhone() : "",
                    String.valueOf(emp.isActive()),
                    emp.getCreatedAt() != null ? emp.getCreatedAt().toString() : ""
            );
            lines.add(line);
        }
        try {
            Files.write(Paths.get(EMPLOYEES_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error writing employees file: " + e.getMessage());
        }
    }

    /**
     * Write customers to file
     * Format: id|username|password|firstname|lastname|phone|email|dob|gender|address|note|created_at
     */
    private void writeCustomersToFile(List<Customer> customers) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|username|password|firstname|lastname|phone|email|dob|gender|address|note|created_at");
        for (Customer cust : customers) {
            String line = String.join("|",
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
                    cust.getCreatedAt() != null ? cust.getCreatedAt().toString() : ""
            );
            lines.add(line);
        }
        try {
            Files.write(Paths.get(CUSTOMERS_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error writing customers file: " + e.getMessage());
        }
    }
}
