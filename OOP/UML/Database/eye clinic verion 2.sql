// USER
Table Admins {
  id           int [pk, increment]
  username     varchar(50) [not null, unique]
  password     varchar(255) [not null]          // hash
  email        varchar(100) [unique]
  is_active    bool [default: true]
}

Table Employees {
  id           int [pk, increment]
  username     varchar(50) [not null, unique]
  password     varchar(255) [not null]          // hash
  firstname    nvarchar(50) [not null]
  lastname     nvarchar(100) [not null]
  avatar       varchar(255)     
  role         enum('doctor','nurse') [not null] // nurse = y tá/thu ngân/quản lý
  license_no   varchar(50)                       // điền khi role=doctor (validate ở backend)
  email        varchar(100) [unique]
  phone        varchar(20)
  is_active    bool [default: true]
  created_at   datetime [default: `CURRENT_TIMESTAMP`]
}

Table Customers {
  id         int [pk, increment]
  username     varchar(50) [not null, unique]
  password     varchar(255) [not null]
  firstname  nvarchar(50) [not null]
  lastname   nvarchar(100) [not null]
  phone      varchar(10)
  email      varchar(100) [unique]
  dob        date
  gender     enum()
  address    nvarchar(255)
  note       nvarchar(255)
  created_at datetime [default: `CURRENT_TIMESTAMP`]

  // Gợi ý index tìm kiếm quầy
  Note: 'INDEX phone, email để tra cứu nhanh tại quầy'
}

//APPOINTMENT
Table Appointments {
  id            int [pk, increment]
  customer_id   int [not null, ref: > Customers.id]
  doctor_id     int [not null, ref: > Employees.id] // MUST be role='doctor' (validate ở backend)

  appointment_type enum('visit','test','surgery', 'blocked') [not null, default: 'visit']
  notes         nvarchar(500)

  start_time    datetime [not null]
  end_time      datetime [not null]
  status        enum('scheduled','confirmed','checked_in','in_progress','completed','cancelled','no_show') [not null, default: 'scheduled']

  created_at    datetime [default: `CURRENT_TIMESTAMP`]
  updated_at    datetime

  Note: 'INDEX (doctor_id, start_time) để chống trùng lịch ở backend'
}

Table Spectacle_Prescriptions {
  id int [pk, increment]
  appointment_id int [not null, unique, ref: > Appointments.id]
  customer_id int [not null]
  created_at date
  updated_at date
  chief_complaint text
  refraction_notes text

  // OD (Right Eye) Prescription
  sph_od decimal(5,2) [not null, default: 0]
  cyl_od decimal(5,2) [default: 0]
  axis_od int [default: 0]
  va_od nvarchar(20)
  prism_od decimal(4,2) [default: 0]
  base_od enum('IN','OUT','UP','DOWN','NONE')
  add_od decimal(4,2) [default: 0]

  // OS (Left Eye) Prescription  
  sph_os decimal(5,2) [not null, default: 0]
  cyl_os decimal(5,2) [default: 0]
  axis_os int [default: 0]
  va_os nvarchar(20)
  prism_os decimal(4,2) [default: 0]
  base_os enum('IN','OUT','UP','DOWN','NONE')
  add_os decimal(4,2) [default: 0]

  // General Prescription Details
  pd decimal(4,1) [default: 0]
  material enum('CR_39','TRIVEX','POLYCARBONATE','HIGH_INDEX_1_60','HIGH_INDEX_1_67','HIGH_INDEX_1_74','OTHER')
  notes text

  // Lens Features
  has_anti_reflective_coating boolean [default: false]
  has_blue_light_filter boolean [default: false]
  has_uv_protection boolean [default: false]
  is_photochromic boolean [default: false]

  // Diagnosis & Plan
  diagnosis text
  plan text

  // Signature
  signed_at date
  signed_by int
  lens_type enum('SINGLE_VISION','BIFOCAL','PROGRESSIVE','CONTACT','OTHER') [default: 'OTHER']
}


//PAYMENT
  Table Payments {
    id               int [pk, increment]
    code             varchar(30) [unique]            // Số chứng từ/in biên lai
    customer_id      int //null khi chưa đk tài khoản, id của customer tự fill khi làm ở backend
    cashier_id       int [not null] // id của employee

    issued_at        datetime [default: `CURRENT_TIMESTAMP`]

    // Tổng tiền
    subtotal         int [not null, default: 0]
    discount         int [not null, default: 0]
    tax_total        int [not null, default: 0]
    rounding         int [not null, default: 0]
    grand_total      int [not null, default: 0]

    // Thanh toán 1 lần tại quầy
    payment_method   enum('CASH','BANK','CARD')
    amount_paid      int        // = grand_total khi thanh toán xong

    note             nvarchar(255)
    created_at       datetime [default: `CURRENT_TIMESTAMP`]

    Note: 'INDEX (status_code, issued_at), (customer_id, issued_at), (cashier_id)'
  }

  // ===== LOG LỊCH SỬ TRẠNG THÁI =====
  Table Payment_Status_Log {
    id            int [pk, increment]
    payment_id    int [not null, ref: > Payments.id]
    changed_at    datetime [default: `CURRENT_TIMESTAMP`]
    status        enum('UNPAID','PENDING','PAID','CANCELLED') [not null]
  }

  // chi tiết hàng trong hóa đơn
  Table Payment_Items {
    id            int [pk, increment]
    product_id    int [not null]
    payment_id    int [not null, ref: > Payments.id]
              // có thể NULL cho dòng thủ công
    description   nvarchar(200)
    qty           int [not null]
    unit_price    int [not null]
    total_line    int [not null]

    Note: 'INDEX (payment_id), (product_id)'
  }
// Sản phẩm
Table Products {
  id           int [pk, increment]
  sku          varchar(40) [not null, unique]    // mã hàng
  name         nvarchar(200) [not null]          // tên hiển thị
  category     enum('frame','lens','contact_lens','machine','consumable','service') [not null]
  unit         nvarchar(20)                      // chiếc, hộp, dịch vụ...
  price_cost   int                     // giá nhập
  price_retail int                     // giá bán lẻ mặc định
  is_active    bool [default: true]
  qty_on_hand int [not null, default: 0]
  batch_no    varchar(40)   // NULL nếu không quản theo lô
  expiry_date date
  serial_no   varchar(60)
  note         nvarchar(255)
  created_at   datetime [default: `CURRENT_TIMESTAMP`]
}

Table Stock_Movements {
  id           int [pk, increment]
  product_id   int [not null, ref: > Products.id]
  qty          int [not null] // >0 nhập, <0 xuất
  move_type    enum('purchase','sale','return_in','return_out','adjustment','consume','transfer') [not null]
  ref_table    varchar(40)    // ví dụ 'Payments','PurchaseOrders','InventoryTransfers'
  ref_id       int            // id chứng từ nguồn
  batch_no     varchar(40)
  expiry_date  date
  serial_no    varchar(60)
  moved_at     datetime [default: `CURRENT_TIMESTAMP`]
  moved_by     int [not null] //id người move
  Note: 'INDEX (product_id), (ref_table, ref_id)'
}