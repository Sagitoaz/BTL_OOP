package org.example.oop.Model.Inventory;

/**
 * Model quản lý thông tin nhà cung cấp
 */
public class Supplier {
    private int id;
    private String code; // SUP-001
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String status; // ACTIVE, INACTIVE
    private String paymentTerms; // NET30, NET60
    private String notes;

    // Constructors
    public Supplier() {
    }

    public Supplier(int id, String code, String name, String contactPerson,
            String email, String phone, String address,
            String status, String paymentTerms, String notes) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
        this.paymentTerms = paymentTerms;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    /**
     * Validate supplier data
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty())
            return false;
        if (code == null || code.trim().isEmpty())
            return false;
        if (email != null && !email.contains("@"))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
