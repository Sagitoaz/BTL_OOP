-- Script tạo bảng Products cho database Supabase
-- Chạy script này trong Supabase SQL Editor
-- Phải khớp 100% với schema đã cho

-- Xóa bảng cũ nếu có (cẩn thận với production!)
-- DROP TABLE IF EXISTS Products CASCADE;

CREATE TABLE IF NOT EXISTS Products (
    id SERIAL PRIMARY KEY,
    sku VARCHAR(40) UNIQUE NOT NULL,              -- Mã SKU sản phẩm
    name VARCHAR(200) NOT NULL,                   -- Tên sản phẩm
    category VARCHAR(30) NOT NULL,                -- frame, lens, contact_lens, machine, consumable, service
    unit VARCHAR(20),                             -- Đơn vị tính: chiếc, hộp, dịch vụ...
    price_cost INTEGER,                           -- Giá nhập (INT)
    price_retail INTEGER,                         -- Giá bán lẻ (INT)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,      -- Trạng thái
    qty_on_hand INTEGER NOT NULL DEFAULT 0,       -- Số lượng tồn kho
    batch_no VARCHAR(40),                         -- Số lô (NULL nếu không quản theo lô)
    expiry_date DATE,                             -- Hạn sử dụng
    serial_no VARCHAR(60),                        -- Số serial
    note VARCHAR(255),                            -- Ghi chú
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraint cho category
    CONSTRAINT ck_products_category CHECK (category IN ('frame','lens','contact_lens','machine','consumable','service'))
);

-- Tạo index cho tìm kiếm nhanh
CREATE INDEX IF NOT EXISTS idx_products_category ON Products(category);
CREATE INDEX IF NOT EXISTS idx_products_active ON Products(is_active);
CREATE INDEX IF NOT EXISTS idx_products_sku ON Products(sku);

-- Insert dữ liệu test
INSERT INTO Products (sku, name, category, unit, price_cost, price_retail, is_active, qty_on_hand, batch_no, expiry_date, serial_no, note) VALUES
('SKU001', 'Kính cận Essilor', 'frame', 'Cái', 2000000, 2500000, true, 10, 'BATCH001', '2027-12-31', 'SN001', 'Kính chất lượng cao'),
('SKU002', 'Thuốc nhỏ mắt Rohto', 'consumable', 'Chai', 60000, 85000, true, 50, 'BATCH002', '2026-06-30', NULL, 'Thuốc nhập khẩu Nhật'),
('SKU003', 'Lens cận 1 ngày', 'contact_lens', 'Hộp', 250000, 350000, true, 30, 'BATCH003', '2026-12-31', NULL, 'Lens dùng 1 lần'),
('SKU004', 'Dung dịch rửa lens', 'consumable', 'Chai', 80000, 120000, true, 25, 'BATCH004', '2027-03-31', NULL, 'Dung dịch 360ml'),
('SKU005', 'Gọng kính Titanium', 'frame', 'Cái', 1000000, 1500000, true, 15, 'BATCH005', NULL, 'SN002', 'Gọng siêu nhẹ');

-- Hiển thị kết quả
SELECT * FROM Products ORDER BY id;
