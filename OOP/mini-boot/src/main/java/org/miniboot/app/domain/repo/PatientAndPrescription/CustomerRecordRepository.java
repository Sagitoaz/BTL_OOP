package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.domain.models.Customer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRecordRepository {
    /**
     * Lưu bệnh nhân mới hoặc cập nhật bệnh nhân hiện có
     */
    Customer save(Customer customer);

    /**
     * Lưu danh sách bệnh nhân
     */
    void saveAll(List<Customer> customers);

    /**
     * Tìm bệnh nhân theo ID
     */
    Optional<Customer> findById(int id);

    /**
     * Lấy tất cả bệnh nhân
     */
    List<Customer> findAll();

    /**
     * Tìm bệnh nhân theo tên (tìm kiếm gần đúng)
     */
    List<Customer> findByName(String name);

    /**
     * Tìm bệnh nhân theo số điện thoại
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    /**
     * Tìm bệnh nhân theo email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Tìm bệnh nhân theo giới tính
     */
    List<Customer> findByGender(Customer.Gender gender);

    List<Customer> findByDateFrom(LocalDate dateFrom);

    List<Customer> findByDateTo(LocalDate dateTo);



    /**
     * Xóa bệnh nhân theo ID
     */
    boolean deleteById(int id);

    /**
     * Kiểm tra bệnh nhân có tồn tại theo ID
     */
    boolean existsById(int id);

    /**
     * Đếm tổng số bệnh nhân
     */
    long count();

    /**
     * Tìm kiếm bệnh nhân theo từ khóa (tìm trong tên, email, số điện thoại)
     */

}
