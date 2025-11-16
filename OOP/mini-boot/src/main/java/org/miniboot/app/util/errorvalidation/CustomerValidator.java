package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.repo.PatientAndPrescription.CustomerRecordRepository;
import org.miniboot.app.http.HttpResponse;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Validator for Customer/Patient business rules and constraints
 * Centralizes customer validation logic to avoid code duplication
 */
public class CustomerValidator {

    /**
     * Validate required fields for customer
     */
    public static HttpResponse validateRequiredFields(Customer customer) {
        if (customer.getFirstname() == null || customer.getFirstname().trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST", "First name is required");
        }
        if (customer.getLastname() == null || customer.getLastname().trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST", "Last name is required");
        }
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST", "Phone is required");
        }
        return null; // Valid
    }

    /**
     * Validate customer business rules (DOB constraints)
     */
    public static HttpResponse validateBusinessRules(Customer customer) {
        if (customer.getDob() != null) {
            // Check future date
            if (customer.getDob().isAfter(LocalDate.now())) {
                return ValidationUtils.error(422, "VALIDATION_FAILED",
                        "Date of birth cannot be in the future");
            }
            // Check unrealistic age (>150 years)
            if (customer.getDob().isBefore(LocalDate.now().minusYears(150))) {
                return ValidationUtils.error(422, "VALIDATION_FAILED",
                        "Invalid date of birth");
            }
        }
        return null; // Valid
    }

    /**
     * Check phone number duplicate
     * For updates: excludes the customer's own ID
     */
    public static HttpResponse checkPhoneDuplicate(
            CustomerRecordRepository repository,
            String phone,
            Integer excludeCustomerId) {
        
        try {
            Optional<Customer> existing = repository.findByPhone(phone);
            if (existing.isPresent()) {
                // If excludeCustomerId is provided, check if it's the same customer
                if (excludeCustomerId != null && existing.get().getId() == excludeCustomerId) {
                    return null; // Same customer, not a duplicate
                }
                return ValidationUtils.error(409, "PHONE_CONFLICT",
                        "Phone number '" + phone + "' is already registered");
            }
        } catch (Exception e) {
            return DatabaseErrorHandler.handleDatabaseException(e);
        }
        return null; // No duplicate
    }

    /**
     * Check email duplicate
     * For updates: excludes the customer's own ID
     */
    public static HttpResponse checkEmailDuplicate(
            CustomerRecordRepository repository,
            String email,
            Integer excludeCustomerId) {
        
        // Only check if email is provided
        if (email == null || email.trim().isEmpty()) {
            return null; // Email is optional
        }

        try {
            Optional<Customer> existing = repository.findByEmail(email);
            if (existing.isPresent()) {
                // If excludeCustomerId is provided, check if it's the same customer
                if (excludeCustomerId != null && existing.get().getId() == excludeCustomerId) {
                    return null; // Same customer, not a duplicate
                }
                return ValidationUtils.error(409, "EMAIL_CONFLICT",
                        "Email '" + email + "' is already registered");
            }
        } catch (Exception e) {
            return DatabaseErrorHandler.handleDatabaseException(e);
        }
        return null; // No duplicate
    }

    /**
     * Full validation for customer (create scenario)
     * Combines all validation checks
     */
    public static HttpResponse validateForCreate(
            Customer customer,
            CustomerRecordRepository repository) {
        
        // Step 1: Required fields
        HttpResponse requiredError = validateRequiredFields(customer);
        if (requiredError != null) return requiredError;

        // Step 2: Business rules
        HttpResponse businessError = validateBusinessRules(customer);
        if (businessError != null) return businessError;

        // Step 3: Phone duplicate (no exclusion for create)
        HttpResponse phoneError = checkPhoneDuplicate(repository, customer.getPhone(), null);
        if (phoneError != null) return phoneError;

        // Step 4: Email duplicate (no exclusion for create)
        HttpResponse emailError = checkEmailDuplicate(repository, customer.getEmail(), null);
        if (emailError != null) return emailError;

        return null; // All validations passed
    }

    /**
     * Full validation for customer (update scenario)
     * Excludes own ID from duplicate checks
     */
    public static HttpResponse validateForUpdate(
            Customer customer,
            CustomerRecordRepository repository) {
        
        // Step 1: Required fields
        HttpResponse requiredError = validateRequiredFields(customer);
        if (requiredError != null) return requiredError;

        // Step 2: Business rules
        HttpResponse businessError = validateBusinessRules(customer);
        if (businessError != null) return businessError;

        // Step 3: Phone duplicate (exclude own ID)
        HttpResponse phoneError = checkPhoneDuplicate(repository, customer.getPhone(), customer.getId());
        if (phoneError != null) return phoneError;

        // Step 4: Email duplicate (exclude own ID)
        HttpResponse emailError = checkEmailDuplicate(repository, customer.getEmail(), customer.getId());
        if (emailError != null) return emailError;

        return null; // All validations passed
    }

    /**
     * Check if customer exists in database
     */
    public static HttpResponse checkExists(
            CustomerRecordRepository repository,
            int customerId) {
        
        try {
            boolean exists = repository.existsById(customerId);
            if (!exists) {
                return ValidationUtils.error(404, "NOT_FOUND",
                        "Customer with ID " + customerId + " not found");
            }
        } catch (Exception e) {
            return DatabaseErrorHandler.handleDatabaseException(e);
        }
        return null; // Exists
    }
}
