package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.domain.models.CustomerRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRecordRepository {
    /**
     * Lưu bệnh nhân mới hoặc cập nhật bệnh nhân hiện có
     */
    CustomerRecord save(CustomerRecord customer);

    /**
     * Lưu danh sách bệnh nhân
     */
    void saveAll(List<CustomerRecord> customers);

    /**
     * Tìm bệnh nhân theo ID
     */
    Optional<CustomerRecord> findById(int id);

    /**
     * Lấy tất cả bệnh nhân
     */
    List<CustomerRecord> findAll();

    /**
     * Tìm bệnh nhân theo tên (tìm kiếm gần đúng)
     */
    List<CustomerRecord> findByName(String name);

    /**
     * Tìm bệnh nhân theo số điện thoại
     */
    Optional<CustomerRecord> findByPhoneNumber(String phoneNumber);

    /**
     * Tìm bệnh nhân theo email
     */
    Optional<CustomerRecord> findByEmail(String email);

    /**
     * Tìm bệnh nhân theo giới tính
     */
    List<CustomerRecord> findByGender(CustomerRecord.Gender gender);

    List<CustomerRecord> findByDateFrom(LocalDate dateFrom);

    List<CustomerRecord> findByDateTo(LocalDate dateTo);

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
