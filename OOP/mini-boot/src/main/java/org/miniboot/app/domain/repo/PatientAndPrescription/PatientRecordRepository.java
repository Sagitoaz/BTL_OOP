package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.domain.models.PatientRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRecordRepository {
    /**
     * Lưu bệnh nhân mới hoặc cập nhật bệnh nhân hiện có
     */
    PatientRecord save(PatientRecord patient);

    /**
     * Lưu danh sách bệnh nhân
     */
    void saveAll(List<PatientRecord> patients);

    /**
     * Tìm bệnh nhân theo ID
     */
    Optional<PatientRecord> findById(int id);

    /**
     * Lấy tất cả bệnh nhân
     */
    List<PatientRecord> findAll();

    /**
     * Tìm bệnh nhân theo tên (tìm kiếm gần đúng)
     */
    List<PatientRecord> findByName(String name);

    /**
     * Tìm bệnh nhân theo số điện thoại
     */
    Optional<PatientRecord> findByPhoneNumber(String phoneNumber);

    /**
     * Tìm bệnh nhân theo email
     */
    Optional<PatientRecord> findByEmail(String email);

    /**
     * Tìm bệnh nhân theo giới tính
     */
    List<PatientRecord> findByGender(PatientRecord.Gender gender);

    List<PatientRecord> findByDateFrom(LocalDate dateFrom);

    List<PatientRecord> findByDateTo(LocalDate dateTo);

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
