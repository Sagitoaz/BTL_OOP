-- =========================
-- ADMINS
-- =========================
CREATE TABLE [Admins] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [username] VARCHAR(50) UNIQUE NOT NULL,
  [password] VARCHAR(255) NOT NULL,
  [email] VARCHAR(100) UNIQUE,
  [is_active] BIT NOT NULL DEFAULT (1)
);
GO

-- =========================
-- EMPLOYEES
-- =========================
CREATE TABLE [Employees] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [username] VARCHAR(50) UNIQUE NOT NULL,
  [password] VARCHAR(255) NOT NULL,
  [firstname] NVARCHAR(50) NOT NULL,
  [lastname] NVARCHAR(100) NOT NULL,
  [role] VARCHAR(20) NOT NULL,
  [license_no] VARCHAR(50),
  [email] VARCHAR(100) UNIQUE,
  [phone] VARCHAR(20),
  [is_active] BIT NOT NULL DEFAULT (1),
  [created_at] DATETIME2(0) DEFAULT (GETDATE()),
  CONSTRAINT CK_Employees_Role
    CHECK ([role] IN ('doctor','nurse'))
);
GO

-- =========================
-- CUSTOMERS
-- =========================
CREATE TABLE [Customers] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [firstname] NVARCHAR(50) NOT NULL,
  [lastname] NVARCHAR(100) NOT NULL,
  [phone] VARCHAR(10),
  [email] VARCHAR(100) UNIQUE,
  [dob] DATE,
  [address] NVARCHAR(255),
  [note] NVARCHAR(255),
  [created_at] DATETIME2(0) DEFAULT (GETDATE())
);
GO

-- =========================
-- APPOINTMENTS
-- =========================
CREATE TABLE [Appointments] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [customer_id] INT NOT NULL,
  [doctor_id] INT NOT NULL,
  [appointment_type] VARCHAR(20) NOT NULL DEFAULT 'visit',
  [notes] NVARCHAR(500),
  [start_time] DATETIME2(0) NOT NULL,
  [end_time] DATETIME2(0) NOT NULL,
  [status] VARCHAR(20) NOT NULL DEFAULT 'scheduled',
  [created_at] DATETIME2(0) DEFAULT (GETDATE()),
  [updated_at] DATETIME2(0),
  CONSTRAINT CK_Appointments_Type
    CHECK ([appointment_type] IN ('visit','test','surgery')),
  CONSTRAINT CK_Appointments_Status
    CHECK ([status] IN ('scheduled','confirmed','checked_in','in_progress','completed','cancelled','no_show'))
);
GO

-- =========================
-- SPECTACLE_PRESCRIPTIONS
-- =========================
CREATE TABLE [Spectacle_Prescriptions] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [appointment_id] INT UNIQUE NOT NULL,
  [date_issued] DATE NOT NULL,
  [status] VARCHAR(20) NOT NULL DEFAULT 'active',
  [sph_od] DECIMAL(5,2) NOT NULL,
  [cyl_od] DECIMAL(5,2),
  [axis_od] INT,
  [va_od] NVARCHAR(20),
  [sph_os] DECIMAL(5,2) NOT NULL,
  [cyl_os] DECIMAL(5,2),
  [axis_os] INT,
  [va_os] NVARCHAR(20),
  [add_power] DECIMAL(4,2),
  [pd] DECIMAL(4,1),
  [lens_type] VARCHAR(30) NOT NULL DEFAULT 'single_vision',
  [material] NVARCHAR(50),
  [features] NVARCHAR(255),
  [recheck_after_months] INT,
  [notes] NVARCHAR(MAX),
  [signed_at] DATETIME2(0),
  CONSTRAINT CK_Prescriptions_Status
    CHECK ([status] IN ('active','expired','void')),
  CONSTRAINT CK_Prescriptions_LensType
    CHECK ([lens_type] IN ('single_vision','bifocal','progressive','contact','other'))
);
GO

-- =========================
-- PAYMENTS
-- =========================
CREATE TABLE [Payments] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [code] VARCHAR(30) UNIQUE,
  [customer_id] INT,
  [cashier_id] INT NOT NULL,
  [issued_at] DATETIME2(0) DEFAULT (GETDATE()),
  [subtotal] INT NOT NULL DEFAULT (0),
  [discount] INT NOT NULL DEFAULT (0),
  [tax_total] INT NOT NULL DEFAULT (0),
  [rounding] INT NOT NULL DEFAULT (0),
  [grand_total] INT NOT NULL DEFAULT (0),
  [payment_method] VARCHAR(20),
  [amount_paid] DECIMAL(12,2),
  [note] NVARCHAR(255),
  [created_at] DATETIME2(0) DEFAULT (GETDATE()),
  CONSTRAINT CK_Payments_Method
    CHECK ([payment_method] IN ('cash','bank'))
);
GO

-- =========================
-- PAYMENT STATUS LOG
-- =========================
CREATE TABLE [Payment_Status_Log] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [payment_id] INT NOT NULL,
  [changed_at] DATETIME2(0) DEFAULT (GETDATE()),
  [status] VARCHAR(20) NOT NULL,
  CONSTRAINT CK_PaymentStatusLog_Status
    CHECK ([status] IN ('unpaid','pending','paid'))
);
GO

-- =========================
-- PAYMENT ITEMS
-- =========================
CREATE TABLE [Payment_Items] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [payment_id] INT NOT NULL,
  [description] NVARCHAR(200),
  [qty] INT NOT NULL,
  [unit_price] INT NOT NULL,
  [total_line] INT NOT NULL
);
GO

-- =========================
-- PRODUCTS
-- =========================
CREATE TABLE [Products] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [sku] VARCHAR(40) UNIQUE NOT NULL,
  [name] NVARCHAR(200) NOT NULL,
  [category] VARCHAR(30) NOT NULL,
  [unit] NVARCHAR(20),
  [price_cost] INT,
  [price_retail] INT,
  [is_active] BIT NOT NULL DEFAULT (1),
  [note] NVARCHAR(255),
  [created_at] DATETIME2(0) DEFAULT (GETDATE()),
  CONSTRAINT CK_Products_Category
    CHECK ([category] IN ('frame','lens','contact_lens','machine','consumable','service'))
);
GO

-- =========================
-- STOCK MOVEMENTS
-- =========================
CREATE TABLE [Stock_Movements] (
  [id] INT PRIMARY KEY IDENTITY(1, 1),
  [product_id] INT NOT NULL,
  [qty] INT NOT NULL,
  [move_type] VARCHAR(20) NOT NULL,
  [ref_table] VARCHAR(40),
  [ref_id] INT,
  [batch_no] VARCHAR(40),
  [expiry_date] DATE,
  [serial_no] VARCHAR(60),
  [moved_at] DATETIME2(0) DEFAULT (GETDATE()),
  [moved_by] INT NOT NULL,
  CONSTRAINT CK_StockMovements_MoveType
    CHECK ([move_type] IN ('purchase','sale','return_in','return_out','adjustment','consume','transfer'))
);
GO

-- =========================
-- INVENTORY BALANCES
-- =========================
CREATE TABLE [Inventory_Balances] (
  [product_id] INT NOT NULL,
  [batch_no] VARCHAR(40),
  [expiry_date] DATE,
  [serial_no] VARCHAR(60),
  [qty_on_hand] INT NOT NULL DEFAULT (0),
  [updated_at] DATETIME2(0) DEFAULT (GETDATE())
);
GO

CREATE INDEX [Inventory_Balances_index_0]
  ON [Inventory_Balances] (product_id);

CREATE UNIQUE INDEX [Inventory_Balances_index_1]
  ON [Inventory_Balances] (product_id, batch_no, expiry_date, serial_no);
GO

-- =========================
-- EXTENDED PROPERTIES (giữ nguyên ý bạn)
-- =========================
EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = N'INDEX phone, email để tra cứu nhanh tại quầy',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Customers';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = N'INDEX (doctor_id, start_time) để chống trùng lịch ở backend',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Appointments';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = N'INDEX (customer_id, issued_at), (cashier_id)',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Payments';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = N'INDEX (payment_id), (product_id)',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Payment_Items';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = N'INDEX (product_id), (ref_table, ref_id)',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Stock_Movements';
GO

-- =========================
-- FOREIGN KEYS (giữ đúng quan hệ ban đầu)
-- =========================
ALTER TABLE [Appointments]
  ADD FOREIGN KEY ([customer_id]) REFERENCES [Customers]([id]);
GO

ALTER TABLE [Appointments]
  ADD FOREIGN KEY ([doctor_id]) REFERENCES [Employees]([id]);
GO

ALTER TABLE [Spectacle_Prescriptions]
  ADD FOREIGN KEY ([appointment_id]) REFERENCES [Appointments]([id]);
GO

ALTER TABLE [Payment_Status_Log]
  ADD FOREIGN KEY ([payment_id]) REFERENCES [Payments]([id]);
GO

ALTER TABLE [Payment_Items]
  ADD FOREIGN KEY ([payment_id]) REFERENCES [Payments]([id]);
GO

ALTER TABLE [Stock_Movements]
  ADD FOREIGN KEY ([product_id]) REFERENCES [Products]([id]);
GO

ALTER TABLE [Inventory_Balances]
  ADD FOREIGN KEY ([product_id]) REFERENCES [Products]([id]);
GO
