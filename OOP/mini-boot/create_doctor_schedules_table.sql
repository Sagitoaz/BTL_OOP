-- ===========================
-- ENUM cho thứ trong tuần
-- ===========================
CREATE TYPE day_of_week_enum AS ENUM (
    'MONDAY',
    'TUESDAY', 
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY'
);

-- ===========================
-- Bảng lịch làm việc định kỳ
-- ===========================
CREATE TABLE IF NOT EXISTS doctor_schedules (
    id SERIAL PRIMARY KEY,
    doctor_id INT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    day_of_week day_of_week_enum NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Chỉ cho phép start_time < end_time
    CONSTRAINT check_time_range CHECK (start_time < end_time)
);

-- =========================================
-- Trigger đảm bảo doctor_id là role = 'doctor'
-- =========================================
CREATE OR REPLACE FUNCTION ensure_doctor_role()
RETURNS trigger AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM employees e
        WHERE e.id = NEW.doctor_id
          AND e.role = 'doctor'
    ) THEN
        RAISE EXCEPTION 'Employee % is not a doctor, cannot create schedule', NEW.doctor_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_doctor_schedules_check_doctor
BEFORE INSERT OR UPDATE ON doctor_schedules
FOR EACH ROW
EXECUTE FUNCTION ensure_doctor_role();

-- =========================================
-- Trigger tự cập nhật updated_at khi UPDATE
-- =========================================
CREATE OR REPLACE FUNCTION set_updated_at_timestamp()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_doctor_schedules_set_updated_at
BEFORE UPDATE ON doctor_schedules
FOR EACH ROW
EXECUTE FUNCTION set_updated_at_timestamp();

-- ===========================
-- Index phục vụ query
-- ===========================
CREATE INDEX IF NOT EXISTS idx_doctor_schedules_doctor_id 
    ON doctor_schedules(doctor_id);

CREATE INDEX IF NOT EXISTS idx_doctor_schedules_day 
    ON doctor_schedules(day_of_week);

CREATE INDEX IF NOT EXISTS idx_doctor_schedules_doctor_day 
    ON doctor_schedules(doctor_id, day_of_week);

-- ===========================
-- Comment cho bảng & cột
-- ===========================
COMMENT ON TABLE doctor_schedules IS 'Lịch làm việc định kỳ của bác sĩ theo ngày trong tuần';
COMMENT ON COLUMN doctor_schedules.doctor_id IS 'ID của bác sĩ (foreign key to employees.id với role=doctor)';
COMMENT ON COLUMN doctor_schedules.day_of_week IS 'Ngày trong tuần (MONDAY, TUESDAY, ...)';
COMMENT ON COLUMN doctor_schedules.start_time IS 'Giờ bắt đầu ca làm việc';
COMMENT ON COLUMN doctor_schedules.end_time IS 'Giờ kết thúc ca làm việc';
COMMENT ON COLUMN doctor_schedules.is_active IS 'Trạng thái hoạt động (true = đang hoạt động, false = tạm ngưng)';

-- ===========================
-- Dữ liệu mẫu
-- Lưu ý: phải có employees.id = 201, 202 và role = 'doctor' trước
-- ===========================

-- Bác sĩ ID 201: Làm việc T2-T6, 8:00-17:00
INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_active) VALUES
(1, 'MONDAY',    '08:00:00', '17:00:00', true),
(1, 'TUESDAY',   '08:00:00', '17:00:00', true),
(1, 'WEDNESDAY', '08:00:00', '17:00:00', true),
(1, 'THURSDAY',  '08:00:00', '17:00:00', true),
(1, 'FRIDAY',    '08:00:00', '17:00:00', true);

-- Bác sĩ ID 202: Làm việc T2, T4, T6, 9:00-16:00
INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_active) VALUES
(3, 'MONDAY',    '09:00:00', '16:00:00', true),
(3, 'WEDNESDAY', '09:00:00', '16:00:00', true),
(3, 'FRIDAY',    '09:00:00', '16:00:00', true);
