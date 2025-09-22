CREATE TABLE [Admins] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [username] varchar(50) UNIQUE NOT NULL,
  [password] varchar(255) NOT NULL,
  [email] varchar(100) UNIQUE,
  [is_active] bool DEFAULT (true)
)
GO

CREATE TABLE [Employees] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [username] varchar(50) UNIQUE NOT NULL,
  [password] varchar(255) NOT NULL,
  [firstname] nvarchar(50) NOT NULL,
  [lastname] nvarchar(100) NOT NULL,
  [role] enum(doctor,nurse) NOT NULL,
  [license_no] varchar(50),
  [email] varchar(100) UNIQUE,
  [phone] varchar(20),
  [is_active] bool DEFAULT (true),
  [created_at] datetime DEFAULT (CURRENT_TIMESTAMP)
)
GO

CREATE TABLE [Customers] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [firstname] nvarchar(50) NOT NULL,
  [lastname] nvarchar(100) NOT NULL,
  [phone] varchar(10),
  [email] varchar(100) UNIQUE,
  [dob] date,
  [address] nvarchar(255),
  [note] nvarchar(255),
  [created_at] datetime DEFAULT (CURRENT_TIMESTAMP)
)
GO

CREATE TABLE [Appointments] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [customer_id] int NOT NULL,
  [doctor_id] int NOT NULL,
  [appointment_type] enum(visit,test,surgery) NOT NULL DEFAULT 'visit',
  [notes] nvarchar(500),
  [start_time] datetime NOT NULL,
  [end_time] datetime NOT NULL,
  [status] enum(scheduled,confirmed,checked_in,in_progress,completed,cancelled,no_show) NOT NULL DEFAULT 'scheduled',
  [created_at] datetime DEFAULT (CURRENT_TIMESTAMP),
  [updated_at] datetime
)
GO

CREATE TABLE [Spectacle_Prescriptions] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [appointment_id] int UNIQUE NOT NULL,
  [date_issued] date NOT NULL,
  [status] enum(active,expired,void) DEFAULT 'active',
  [sph_od] decimal(5,2) NOT NULL,
  [cyl_od] decimal(5,2),
  [axis_od] int,
  [va_od] nvarchar(20),
  [sph_os] decimal(5,2) NOT NULL,
  [cyl_os] decimal(5,2),
  [axis_os] int,
  [va_os] nvarchar(20),
  [add_power] decimal(4,2),
  [pd] decimal(4,1),
  [lens_type] enum(single_vision,bifocal,progressive,contact,other) DEFAULT 'single_vision',
  [material] nvarchar(50),
  [features] nvarchar(255),
  [recheck_after_months] int,
  [notes] text,
  [signed_at] datetime
)
GO

CREATE TABLE [Payments] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [code] varchar(30) UNIQUE,
  [customer_id] int,
  [cashier_id] int NOT NULL,
  [issued_at] datetime DEFAULT (CURRENT_TIMESTAMP),
  [subtotal] int NOT NULL DEFAULT (0),
  [discount] int NOT NULL DEFAULT (0),
  [tax_total] int NOT NULL DEFAULT (0),
  [rounding] int NOT NULL DEFAULT (0),
  [grand_total] int NOT NULL DEFAULT (0),
  [payment_method] enum(cash,bank),
  [amount_paid] decimal(12,2),
  [note] nvarchar(255),
  [created_at] datetime DEFAULT (CURRENT_TIMESTAMP)
)
GO

CREATE TABLE [Payment_Status_Log] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [payment_id] int NOT NULL,
  [changed_at] datetime DEFAULT (CURRENT_TIMESTAMP),
  [status] enum(unpaid,paid) NOT NULL
)
GO

CREATE TABLE [Payment_Items] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [payment_id] int NOT NULL,
  [description] nvarchar(200),
  [qty] int NOT NULL,
  [unit_price] int NOT NULL,
  [total_line] int NOT NULL
)
GO

CREATE TABLE [Products] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [sku] varchar(40) UNIQUE NOT NULL,
  [name] nvarchar(200) NOT NULL,
  [category] enum(frame,lens,contact_lens,machine,consumable,service) NOT NULL,
  [unit] nvarchar(20),
  [price_cost] int,
  [price_retail] int,
  [is_active] bool DEFAULT (true),
  [note] nvarchar(255),
  [created_at] datetime DEFAULT (CURRENT_TIMESTAMP)
)
GO

CREATE TABLE [Stock_Movements] (
  [id] int PRIMARY KEY IDENTITY(1, 1),
  [product_id] int NOT NULL,
  [qty] int NOT NULL,
  [move_type] enum(purchase,sale,return_in,return_out,adjustment,consume,transfer) NOT NULL,
  [ref_table] varchar(40),
  [ref_id] int,
  [batch_no] varchar(40),
  [expiry_date] date,
  [serial_no] varchar(60),
  [moved_at] datetime DEFAULT (CURRENT_TIMESTAMP),
  [moved_by] int NOT NULL
)
GO

CREATE TABLE [Inventory_Balances] (
  [product_id] int NOT NULL,
  [batch_no] varchar(40),
  [expiry_date] date,
  [serial_no] varchar(60),
  [qty_on_hand] int NOT NULL DEFAULT (0),
  [updated_at] datetime DEFAULT (CURRENT_TIMESTAMP)
)
GO

CREATE INDEX [Inventory_Balances_index_0] ON [Inventory_Balances] ("product_id")
GO

CREATE UNIQUE INDEX [Inventory_Balances_index_1] ON [Inventory_Balances] ("product_id", "batch_no", "expiry_date", "serial_no")
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = 'INDEX phone, email để tra cứu nhanh tại quầy',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Customers';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = 'INDEX (doctor_id, start_time) để chống trùng lịch ở backend',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Appointments';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = 'INDEX (status_code, issued_at), (customer_id, issued_at), (cashier_id)',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Payments';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = 'INDEX (payment_id), (product_id)',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Payment_Items';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = 'INDEX (product_id), (ref_table, ref_id)',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'Stock_Movements';
GO

ALTER TABLE [Appointments] ADD FOREIGN KEY ([customer_id]) REFERENCES [Customers] ([id])
GO

ALTER TABLE [Appointments] ADD FOREIGN KEY ([doctor_id]) REFERENCES [Employees] ([id])
GO

ALTER TABLE [Spectacle_Prescriptions] ADD FOREIGN KEY ([appointment_id]) REFERENCES [Appointments] ([id])
GO

ALTER TABLE [Payment_Status_Log] ADD FOREIGN KEY ([payment_id]) REFERENCES [Payments] ([id])
GO

ALTER TABLE [Payment_Items] ADD FOREIGN KEY ([payment_id]) REFERENCES [Payments] ([id])
GO

ALTER TABLE [Stock_Movements] ADD FOREIGN KEY ([product_id]) REFERENCES [Products] ([id])
GO

ALTER TABLE [Inventory_Balances] ADD FOREIGN KEY ([product_id]) REFERENCES [Products] ([id])
GO
