package org.miniboot.app.domain.repo.Employee;

import java.util.List;
import java.util.Optional;

import org.miniboot.app.domain.models.Employee;

public interface EmployeeRepository {
     List<Employee> findAll();

     Optional<Employee> findById(int id);

     String findPasswordByUsernameOrEmail(String username);

     Optional<Employee> findByUserName(String userName);

     Optional<Employee> findByEmail(String email);

     boolean deleteById(int id);

     Employee save(Employee product);

     List<Employee> findByRole(String role);

     boolean changePassword(String usernameOrEmail, String newPasswordHash);
}
