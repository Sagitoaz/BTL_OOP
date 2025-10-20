package org.miniboot.app.Service.mappers.CustomerAndPrescription;

import org.miniboot.app.domain.models.Customer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerMapper {
    public static Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setUsername(rs.getString("username"));
        customer.setPassword(rs.getString("password"));
        customer.setFirstname(rs.getString("firstname"));
        customer.setLastname(rs.getString("lastname"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));
        customer.setDob(rs.getDate("dob").toLocalDate());
        customer.setGender(Customer.Gender.valueOf(rs.getString("gender")));
        customer.setAddress(rs.getString("address"));
        customer.setNote(rs.getString("note"));
        customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return customer;
    }

}
