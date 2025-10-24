package org.miniboot.app.domain.repo;

import org.miniboot.app.domain.models.User;
import org.miniboot.app.domain.models.Admin;
import org.miniboot.app.domain.models.Employee;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

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
 * - ID sử dụng kiểu int thay vì String để tương thích với OOP_UI
 * - Format file:
 *   + admins.txt: id|username|password|email|is_active
 *   + employees.txt: id|username|password|firstname|lastname|avatar|role|license_no|email|phone|is_active|created_at
 *   + customers.txt: id|username|password|firstname|lastname|phone|email|dob|gender|address|note|created_at
 */
public class UserRepository {
    // Sửa đường dẫn để tương thích với cấu trúc thư mục thực tế
    private static final String DATA_DIR = System.getProperty("user.dir") + "\\oop_ui\\src\\main\\resources\\Data\\";
    private static final String ADMINS_FILE = DATA_DIR + "admins.txt";
    private static final String EMPLOYEES_FILE = DATA_DIR + "employees.txt";
    private static final String CUSTOMERS_FILE = DATA_DIR + "customers.txt";

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
     * Find user by ID (search across all 3 tables)
     */
    public Optional<User> findById(int id) {
        // Tìm trong admins
        for (Admin admin : loadAdmins()) {
            if (admin.getId() == id) {
                return Optional.of(admin);
            }
        }

        // Tìm trong employees
        for (Employee employee : loadEmployees()) {
            if (employee.getId() == id) {
                return Optional.of(employee);
            }
        }

        // Tìm trong customers
        for (Customer customer : loadCustomers()) {
            if (customer.getId() == id) {
                return Optional.of(customer);
            }
        }

        return Optional.empty();
    }

    /**
     * Find user by username (search across all 3 tables)
     */
    public Optional<User> findByUsername(String username) {
        // Tìm trong admins
        for (Admin admin : loadAdmins()) {
            if (username.equals(admin.getUsername())) {
                return Optional.of(admin);
            }
        }

        // Tìm trong employees
        for (Employee employee : loadEmployees()) {
            if (username.equals(employee.getUsername())) {
                return Optional.of(employee);
            }
        }

        // Tìm trong customers
        for (Customer customer : loadCustomers()) {
            if (username.equals(customer.getUsername())) {
                return Optional.of(customer);
            }
        }

        return Optional.empty();
    }

    /**
     * Save user (determine which file based on user type)
     */
    public void save(User user) {
        if (user instanceof Admin) {
            saveAdmin((Admin) user);
        } else if (user instanceof Employee) {
            saveEmployee((Employee) user);
        } else if (user instanceof Customer) {
            saveCustomer((Customer) user);
        }
    }

    /**
     * Delete user by ID (search and delete from appropriate file)
     */
    public boolean deleteById(int id) {
        // Tìm và xóa từ admins
        List<Admin> admins = loadAdmins();
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getId() == id) {
                admins.remove(i);
                saveAllAdmins(admins);
                return true;
            }
        }

        // Tìm và xóa từ employees
        List<Employee> employees = loadEmployees();
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId() == id) {
                employees.remove(i);
                saveAllEmployees(employees);
                return true;
            }
        }

        // Tìm và xóa từ customers
        List<Customer> customers = loadCustomers();
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId() == id) {
                customers.remove(i);
                saveAllCustomers(customers);
                return true;
            }
        }

        return false;
    }

    /**
     * Load admins from admins.txt
     * Format: id|username|password|email|is_active
     */
    private List<Admin> loadAdmins() {
        List<Admin> admins = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(ADMINS_FILE))) {
                return admins;
            }

            List<String> lines = Files.readAllLines(Paths.get(ADMINS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    Admin admin = new Admin();
                    admin.setId(Integer.parseInt(parts[0]));
                    admin.setUsername(parts[1]);
                    admin.setPassword(parts[2]);
                    admin.setEmail(parts[3]);
                    admin.setActive(Boolean.parseBoolean(parts[4]));
                    admins.add(admin);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading admins: " + e.getMessage());
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
            if (!Files.exists(Paths.get(EMPLOYEES_FILE))) {
                return employees;
            }

            List<String> lines = Files.readAllLines(Paths.get(EMPLOYEES_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 12) {
                    Employee employee = new Employee();
                    employee.setId(Integer.parseInt(parts[0]));
                    employee.setUsername(parts[1]);
                    employee.setPassword(parts[2]);
                    employee.setFirstname(parts[3]);
                    employee.setLastname(parts[4]);
                    employee.setAvatar(parts[5]);
                    employee.setEmployeeRole(parts[6]);
                    employee.setLicenseNo(parts[7]);
                    employee.setEmail(parts[8]);
                    employee.setPhone(parts[9]);
                    employee.setActive(Boolean.parseBoolean(parts[10]));
                    if (!parts[11].isEmpty()) {
                        employee.setCreatedAt(LocalDateTime.parse(parts[11]));
                    }
                    employees.add(employee);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading employees: " + e.getMessage());
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
            if (!Files.exists(Paths.get(CUSTOMERS_FILE))) {
                return customers;
            }

            List<String> lines = Files.readAllLines(Paths.get(CUSTOMERS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 12) {
                    Customer customer = new Customer();
                    customer.setId(Integer.parseInt(parts[0]));
                    customer.setUsername(parts[1]);
                    customer.setPassword(parts[2]);
                    customer.setFirstname(parts[3]);
                    customer.setLastname(parts[4]);
                    customer.setPhone(parts[5]);
                    customer.setEmail(parts[6]);
                    if (!parts[7].isEmpty()) {
                        customer.setDob(LocalDate.parse(parts[7]));
                    }
                    customer.setGender(Customer.Gender.valueOf(parts[8]));
                    customer.setAddress(parts[9]);
                    customer.setNote(parts[10]);
                    if (!parts[11].isEmpty()) {
                        customer.setCreatedAt(LocalDateTime.parse(parts[11]));
                    }
                    customers.add(customer);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
        return customers;
    }

    private void saveAdmin(Admin admin) {
        List<Admin> admins = loadAdmins();

        // Update existing or add new
        boolean found = false;
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getId() == admin.getId()) {
                admins.set(i, admin);
                found = true;
                break;
            }
        }

        if (!found) {
            admins.add(admin);
        }

        saveAllAdmins(admins);
    }

    private void saveEmployee(Employee employee) {
        List<Employee> employees = loadEmployees();

        // Update existing or add new
        boolean found = false;
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId() == employee.getId()) {
                employees.set(i, employee);
                found = true;
                break;
            }
        }

        if (!found) {
            employees.add(employee);
        }

        saveAllEmployees(employees);
    }

    private void saveCustomer(Customer customer) {
        List<Customer> customers = loadCustomers();

        // Update existing or add new
        boolean found = false;
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId() == customer.getId()) {
                customers.set(i, customer);
                found = true;
                break;
            }
        }

        if (!found) {
            customers.add(customer);
        }

        saveAllCustomers(customers);
    }

    private void saveAllAdmins(List<Admin> admins) {
        try {
            Files.createDirectories(Paths.get(ADMINS_FILE).getParent());
            List<String> lines = new ArrayList<>();
            for (Admin admin : admins) {
                String line = String.format("%d|%s|%s|%s|%s",
                    admin.getId(),
                    admin.getUsername(),
                    admin.getPassword(),
                    admin.getEmail() != null ? admin.getEmail() : "",
                    admin.isActive()
                );
                lines.add(line);
            }
            Files.write(Paths.get(ADMINS_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving admins: " + e.getMessage());
        }
    }

    private void saveAllEmployees(List<Employee> employees) {
        try {
            Files.createDirectories(Paths.get(EMPLOYEES_FILE).getParent());
            List<String> lines = new ArrayList<>();
            for (Employee employee : employees) {
                String line = String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                    employee.getId(),
                    employee.getUsername(),
                    employee.getPassword(),
                    employee.getFirstname() != null ? employee.getFirstname() : "",
                    employee.getLastname() != null ? employee.getLastname() : "",
                    employee.getAvatar() != null ? employee.getAvatar() : "",
                    employee.getEmployeeRole() != null ? employee.getEmployeeRole() : "",
                    employee.getLicenseNo() != null ? employee.getLicenseNo() : "",
                    employee.getEmail() != null ? employee.getEmail() : "",
                    employee.getPhone() != null ? employee.getPhone() : "",
                    employee.isActive(),
                    employee.getCreatedAt() != null ? employee.getCreatedAt().toString() : ""
                );
                lines.add(line);
            }
            Files.write(Paths.get(EMPLOYEES_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving employees: " + e.getMessage());
        }
    }

    private void saveAllCustomers(List<Customer> customers) {
        try {
            Files.createDirectories(Paths.get(CUSTOMERS_FILE).getParent());
            List<String> lines = new ArrayList<>();
            for (Customer customer : customers) {
                String line = String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                    customer.getId(),
                    customer.getUsername(),
                    customer.getPassword(),
                    customer.getFirstname() != null ? customer.getFirstname() : "",
                    customer.getLastname() != null ? customer.getLastname() : "",
                    customer.getPhone() != null ? customer.getPhone() : "",
                    customer.getEmail() != null ? customer.getEmail() : "",
                    customer.getDob() != null ? customer.getDob().toString() : "",
                    customer.getGender() != null ? customer.getGender() : "",
                    customer.getAddress() != null ? customer.getAddress() : "",
                    customer.getNote() != null ? customer.getNote() : "",
                    customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : ""
                );
                lines.add(line);
            }
            Files.write(Paths.get(CUSTOMERS_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving customers: " + e.getMessage());
        }
    }
}
