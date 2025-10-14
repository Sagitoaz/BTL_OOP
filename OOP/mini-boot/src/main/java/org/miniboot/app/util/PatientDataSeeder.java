package org.miniboot.app.util;

import org.miniboot.app.domain.models.CustomerRecord;
import java.time.LocalDate;
import java.util.List;

/**
 * Utility class để tạo dữ liệu mẫu cho PatientRecord với firstNamePatient và lastNamePatient
 */
public class PatientDataSeeder {

    public static List<CustomerRecord> createSamplePatients() {
        return List.of(
            new CustomerRecord(0, "Minh", "Nguyễn Văn",
                LocalDate.of(1985, 3, 15), CustomerRecord.Gender.NAM,
                "123 Đường ABC, Quận 1, TP.HCM", "0912345678", "minh.nguyen@email.com",
                "Bệnh nhân thường xuyên"),

            new CustomerRecord(0, "Lan", "Trần Thị",
                LocalDate.of(1990, 7, 22), CustomerRecord.Gender.NỮ,
                "456 Đường XYZ, Quận 3, TP.HCM", "0987654321", "lan.tran@email.com",
                "Có tiền sử dị ứng thuốc"),

            new CustomerRecord(0, "Hoàng", "Lê Văn",
                LocalDate.of(1978, 12, 8), CustomerRecord.Gender.NAM,
                "789 Đường DEF, Quận 7, TP.HCM", "0901234567", "hoang.le@email.com",
                null),

            new CustomerRecord(0, "Mai", "Phạm Thị",
                LocalDate.of(1995, 4, 30), CustomerRecord.Gender.NỮ,
                "321 Đường GHI, Quận 5, TP.HCM", "0976543210", "mai.pham@email.com",
                "Bệnh nhân VIP"),

            new CustomerRecord(0, "Tuấn", "Võ Minh",
                LocalDate.of(1988, 9, 12), CustomerRecord.Gender.NAM,
                "654 Đường JKL, Quận 2, TP.HCM", "0934567890", "tuan.vo@email.com",
                "Khám định kỳ 6 tháng"),

            new CustomerRecord(0, "Hương", "Đặng Thị",
                LocalDate.of(1992, 1, 18), CustomerRecord.Gender.NỮ,
                "987 Đường MNO, Quận 4, TP.HCM", "0965432109", "huong.dang@email.com",
                null)
        );
    }
}
