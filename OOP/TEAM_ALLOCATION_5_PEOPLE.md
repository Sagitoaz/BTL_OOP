# 👥 PHÂN CÔNG CÔNG VIỆC - NHÓM 5 NGƯỜI
**Dự án: Quản lý Phòng Khám Mắt**  
**Thời hạn: 1 tuần (24/10/2025 - 30/10/2025)**  
**Chiến lược: Phân chia theo module độc lập để giảm phụ thuộc**

---

## 🎯 NGUYÊN TẮC PHÂN CHIA

1. **Độc lập cao**: Mỗi người làm việc trên module/layer riêng biệt
2. **Integration points rõ ràng**: Định nghĩa contract/interface từ đầu
3. **Parallel development**: Tất cả có thể làm đồng thời từ ngày 1
4. **Daily sync**: 15 phút mỗi sáng để sync progress và resolve blockers
5. **Code review**: Peer review trước khi merge vào dev branch

---

## 👤 PERSON 1: UI/UX DESIGNER & CSS SPECIALIST
**Role**: Frontend Styling & Visual Design  
**Độ ưu tiên**: 🔴 CRITICAL - Ảnh hưởng đến toàn bộ giao diện  
**Dependencies**: KHÔNG phụ thuộc ai, người khác phụ thuộc vào người này

### 📋 CÔNG VIỆC CHI TIẾT

#### **Ngày 1 (24/10) - Foundation**
```
Sáng (4h):
- [ ] Thiết kế Color Palette & Theme
  - Define colors: primary, secondary, success, danger, warning
  - Font families, sizes (heading, body, small)
  - Spacing system (4px, 8px, 16px, 24px, 32px)
  
- [ ] Tạo 6 file CSS cơ bản:
  - main.css (variables, reset, typography)
  - components.css (buttons, inputs, cards)
  - layout.css (containers, grid, flex)
  
Chiều (4h):
- [ ] Hoàn thiện CSS files:
  - tables.css (TableView styling)
  - forms.css (Form layouts, validation states)
  - navigation.css (Sidebar, menu, breadcrumb)
```

#### **Ngày 2 (25/10) - Apply to Auth Screens**
```
Sáng (4h):
- [ ] Apply CSS to Authentication screens:
  - Login.fxml → Remove inline styles, add styleClass
  - Signup.fxml → Apply forms.css
  - ResetPassword.fxml
  - ChangePassword.fxml
  
Chiều (4h):
- [ ] Tạo reusable components CSS:
  - .btn-primary, .btn-secondary, .btn-danger
  - .form-group, .form-control, .form-error
  - .card, .card-header, .card-body
```

#### **Ngày 3 (26/10) - Dashboard & Navigation**
```
Sáng (4h):
- [ ] Design & style Dashboard.fxml:
  - Top bar với user info, notifications
  - Left sidebar với menu items + icons
  - Content area với cards
  
Chiều (4h):
- [ ] Style navigation components:
  - Menu items (hover, active states)
  - Breadcrumbs
  - Tabs styling
```

#### **Ngày 4 (27/10) - Module Screens (Part 1)**
```
Sáng (4h):
- [ ] Apply CSS to Inventory module:
  - ProductCRUDView.fxml
  - StockMovementView.fxml
  - SearchInventoryView.fxml
  
Chiều (4h):
- [ ] Apply CSS to Schedule module:
  - AppointmentBooking.fxml
  - Calendar.fxml
  - DoctorSchedule.fxml
```

#### **Ngày 5 (28/10) - Module Screens (Part 2)**
```
Sáng (4h):
- [ ] Apply CSS to Payment module:
  - Invoice.fxml
  - Payment.fxml
  - PaymentHistory.fxml
  - Receipt.fxml
  
Chiều (4h):
- [ ] Apply CSS to Patient module:
  - CustomerHub.fxml
  - PrescriptionEditor.fxml
  - AddCustomerView.fxml
```

#### **Ngày 6 (29/10) - Employee Module & Polish**
```
Sáng (4h):
- [ ] Style Employee Management (new):
  - EmployeeManagement.fxml
  - EmployeeForm.fxml
  
Chiều (4h):
- [ ] Polish & refinements:
  - Hover effects
  - Transitions
  - Responsive adjustments
```

#### **Ngày 7 (30/10) - Final Polish**
```
Sáng (4h):
- [ ] Dark mode support (optional)
- [ ] Accessibility improvements (contrast, focus states)
- [ ] CSS documentation
  
Chiều (4h):
- [ ] Style guide document
- [ ] Component showcase
- [ ] Handover to team
```

### 📦 DELIVERABLES
- [ ] 6 CSS files hoàn chỉnh
- [ ] Tất cả FXML đã remove inline styles
- [ ] Style guide document
- [ ] Screenshots before/after

### 🔧 TOOLS & RESOURCES
- Scene Builder (để preview FXML + CSS)
- Color palette tool: https://coolors.co
- JavaFX CSS reference: https://openjfx.io/javadoc/17/javafx.graphics/javafx/scene/doc-files/cssref.html

---

## 👤 PERSON 2: BACKEND API DEVELOPER
**Role**: Mini-boot Server, API Endpoints, Database  
**Độ ưu tiên**: 🔴 CRITICAL - Frontend phụ thuộc vào API  
**Dependencies**: KHÔNG phụ thuộc ai

### 📋 CÔNG VIỆC CHI TIẾT

#### **Ngày 1 (24/10) - Setup & Testing**
```
Sáng (4h):
- [ ] Setup & Testing:
  - Tạo application.properties từ template
  - Test kết nối database
  - Test tất cả existing endpoints bằng Postman
  - Tạo Postman Collection với tất cả APIs
  
Chiều (4h):
- [ ] API Testing & Documentation:
  - Test từng module: Appointment, Payment, Inventory, Patient
  - Document request/response format
  - Note các issues cần fix
```

#### **Ngày 2 (25/10) - Error Handling**
```
Sáng (4h):
- [ ] Improve error responses:
  - Standardize error JSON format
  - Better error messages Vietnamese
  - HTTP status codes đúng (400, 401, 403, 404, 409, 422, 500, 503)
  
Chiều (4h):
- [ ] Add logging:
  - Request/Response logging
  - Error logging với stack trace
  - Performance logging (slow queries)
```

#### **Ngày 3 (26/10) - Missing Features Part 1**
```
Sáng (4h):
- [ ] Payment update API:
  - Implement PUT /payments/:id
  - Update payment status
  - Test với Postman
  
Chiều (4h):
- [ ] Customer search:
  - Search by phone: GET /customers?phone=xxx
  - Search by email: GET /customers?email=xxx
  - Search by name: GET /customers?name=xxx
```

#### **Ngày 4 (27/10) - Missing Features Part 2**
```
Sáng (4h):
- [ ] Doctor working hours API:
  - POST /doctors/:id/working-hours
  - GET /doctors/:id/working-hours
  - Update working hours schedule
  
Chiều (4h):
- [ ] Schedule validation:
  - Check doctor working hours before booking
  - Check patient appointment conflicts
  - Time slot validation logic
```

#### **Ngày 5 (28/10) - Database Optimization**
```
Sáng (4h):
- [ ] Add missing indexes:
  - Appointments (doctor_id, start_time)
  - Customers (phone, email)
  - Products (sku)
  - Payments (customer_id, issued_at)
  
Chiều (4h):
- [ ] Query optimization:
  - Analyze slow queries
  - Add connection pool tuning
  - Test performance improvements
```

#### **Ngày 6 (29/10) - Integration Testing & Deployment Prep**
```
Sáng (4h):
- [ ] Full CRUD testing:
  - Test tất cả endpoints end-to-end
  - Test error scenarios (network, timeout, invalid data)
  - Document test results
  
Chiều (4h):
- [ ] Deployment preparation:
  - Tạo Dockerfile cho mini-boot
  - Tạo docker-compose.yml (optional)
  - Test Docker build locally
  - Tạo .env.example với all required vars
```

#### **Ngày 7 (30/10) - Deploy Server & Documentation**
```
Sáng (4h):
- [ ] Deploy to cloud (Railway/Render):
  - Connect GitHub repo
  - Configure environment variables
  - Deploy và test production URL
  - Setup health check endpoint
  
Chiều (4h):
- [ ] API Documentation & Support:
  - Document tất cả endpoints
  - Postman Collection export
  - DEPLOYMENT.md guide
  - Support frontend team integrate production URL
```

### 📦 DELIVERABLES
- [ ] Postman Collection đầy đủ
- [ ] Tất cả APIs tested & documented
- [ ] Error handling improved
- [ ] Missing features implemented
- [ ] Database optimized
- [ ] **Docker setup** (Dockerfile + docker-compose.yml)
- [ ] **Server deployed to cloud** (Railway/Render)
- [ ] API Documentation + DEPLOYMENT.md

### 🔧 TOOLS & RESOURCES
- Postman: Test APIs
- DBeaver: Database management
- Docker Desktop: Containerization
- Railway/Render: Free cloud hosting

---

## 👤 PERSON 3: NAVIGATION & CORE FRAMEWORK DEVELOPER
**Role**: Scene Management, Dashboard, Routing  
**Độ ưu tiên**: 🔴 CRITICAL - Toàn bộ app phụ thuộc navigation  
**Dependencies**: Chờ Person 1 (CSS) cho styling, nhưng có thể code logic trước

### 📋 CÔNG VIỆC CHI TIẾT

#### **Ngày 1 (24/10) - SceneManager Utility**
```
Sáng (4h):
- [ ] Tạo SceneManager.java:
  public class SceneManager {
    private static Stage primaryStage;
    private static Map<String, Parent> cachedScenes;
    private static Stack<String> navigationHistory;
    
    public static void init(Stage stage);
    public static void switchScene(String fxmlPath);
    public static void openDialog(String fxmlPath);
    public static void goBack();
    public static void clearCache();
  }
  
Chiều (4h):
- [ ] Scene caching & preloading:
  - Implement cache logic
  - Preload common scenes
  - Memory management
```

#### **Ngày 2 (25/10) - Dashboard Structure**
```
Sáng (4h):
- [ ] Tạo Dashboard.fxml (structure only, no style):
  - BorderPane layout
  - Top: HBox (logo, user info, logout button)
  - Left: VBox (menu items)
  - Center: StackPane (content area)
  - Bottom: HBox (status bar)
  
Chiều (4h):
- [ ] Tạo DashboardController.java:
  - Menu navigation logic
  - Load content to center area
  - User session management
  - Logout functionality
```

#### **Ngày 3 (26/10) - Home View & Integration**
```
Sáng (4h):
- [ ] Tạo HomeView.fxml & Controller:
  - Statistics cards (appointments, patients, revenue)
  - Recent activities list
  - Quick actions buttons
  - Load data from APIs
  
Chiều (4h):
- [ ] Integrate Dashboard với existing screens:
  - Update LoginController: load Dashboard instead of hello-view
  - Test navigation flows
  - Breadcrumb implementation
```

#### **Ngày 4 (27/10) - Update All Controllers**
```
Sáng (4h):
- [ ] Replace manual navigation (Part 1):
  - LoginController
  - SignUpController
  - ForgotPasswordController
  - ChangePasswordController
  
Chiều (4h):
- [ ] Replace manual navigation (Part 2):
  - PaymentController
  - InvoiceController
  - CustomerHubController
```

#### **Ngày 5 (28/10) - Navigation Polish**
```
Sáng (4h):
- [ ] Advanced navigation features:
  - Navigation history (back/forward)
  - Breadcrumbs auto-update
  - Active menu item highlight
  
Chiều (4h):
- [ ] Session management:
  - Auto-logout khi idle
  - Session timeout warning
  - Remember last screen
```

#### **Ngày 6 (29/10) - Error & Loading Screens**
```
Sáng (4h):
- [ ] Tạo LoadingOverlay.fxml:
  - ProgressIndicator
  - Loading message
  - Cancel button (optional)
  
Chiều (4h):
- [ ] Tạo ErrorScreen.fxml:
  - 404 Not Found screen
  - 403 Forbidden screen
  - Generic error screen
```

#### **Ngày 7 (30/10) - Testing & Optimization**
```
Sáng (4h):
- [ ] Performance optimization:
  - Scene preloading strategy
  - Memory leak check
  - Smooth transitions
  
Chiều (4h):
- [ ] Integration testing:
  - Test all navigation paths
  - Test back button
  - Test session timeout
```

### 📦 DELIVERABLES
- [ ] SceneManager.java hoàn chỉnh
- [ ] Dashboard.fxml + Controller
- [ ] HomeView.fxml + Controller
- [ ] Tất cả controllers đã update navigation
- [ ] LoadingOverlay component

### 🔧 TOOLS & RESOURCES
- Scene Builder
- Git branches: feature/navigation

---

## 👤 PERSON 4: ERROR HANDLING & SERVICE LAYER DEVELOPER
**Role**: HTTP Error Handling, Service Layer, API Integration  
**Độ ưu tiên**: 🟡 HIGH - Improve code quality  
**Dependencies**: Chờ Person 2 (Backend API ready)

### 📋 CÔNG VIỆC CHI TIẾT

#### **Ngày 1 (24/10) - Error Framework**
```
Sáng (4h):
- [ ] Tạo ErrorHandler.java:
  public class ErrorHandler {
    public static String getErrorMessage(int statusCode);
    public static void handleHttpError(int code, String context);
    public static void showUserFriendlyError(int code, String msg);
    public static boolean shouldRetry(int statusCode);
  }
  
- [ ] Tạo HttpException.java:
  public class HttpException extends Exception {
    private int statusCode;
    private String errorMessage;
  }
  
Chiều (4h):
- [ ] Error message mapping:
  - Tạo properties file với error messages
  - Vietnamese translations
  - Context-specific messages
```

#### **Ngày 2 (25/10) - Update Service Layer (Part 1)**
```
Sáng (4h):
- [ ] Update HttpAppointmentService.java:
  - Replace simple error logging với ErrorHandler
  - Add try-catch blocks
  - User-friendly error messages
  - Retry logic cho 503/504
  
Chiều (4h):
- [ ] Update HttpPaymentService.java:
  - Same error handling pattern
  - Validate response before parsing
  - Handle null responses
```

#### **Ngày 3 (26/10) - Update Service Layer (Part 2)**
```
Sáng (4h):
- [ ] Update HttpPaymentItemService.java
- [ ] Update HttpPaymentStatusLogService.java
- [ ] Update HttpDoctorService.java
  
Chiều (4h):
- [ ] Update ApiProductService.java
- [ ] Update ApiStockMovementService.java
```

#### **Ngày 4 (27/10) - Update Service Layer (Part 3)**
```
Sáng (4h):
- [ ] Update CustomerRecordService.java
- [ ] Update PrescriptionService.java
- [ ] Tạo HttpEmployeeService.java (new)
  
Chiều (4h):
- [ ] Service utilities:
  - ApiClient improvements
  - Request timeout configuration
  - Connection pool management
```

#### **Ngày 5 (28/10) - Loading & Progress**
```
Sáng (4h):
- [ ] LoadingManager.java:
  - Show/hide loading overlay
  - Progress tracking
  - Cancel requests
  
Chiều (4h):
- [ ] Integrate loading vào BaseController:
  - executeAsync với loading indicator
  - Progress callbacks
  - Cancel functionality
```

#### **Ngày 6 (29/10) - Code Cleanup**
```
Sáng (4h):
- [ ] Remove debug code:
  - Remove System.out.println debug statements
  - Remove TODO comments đã xong
  - Remove unused imports
  
Chiều (4h):
- [ ] Code quality:
  - Add JavaDoc comments
  - Consistent naming conventions
  - Code formatting
```

#### **Ngày 7 (30/10) - Testing & Documentation**
```
Sáng (4h):
- [ ] Error scenario testing:
  - Test 400, 401, 403, 404, 409, 422, 429, 500, 503, 504
  - Test network failures
  - Test timeout scenarios
  
Chiều (4h):
- [ ] Service layer documentation:
  - Document all services
  - Usage examples
  - Error handling guide
```

### 📦 DELIVERABLES
- [ ] ErrorHandler framework
- [ ] 9 Services updated với proper error handling
- [ ] HttpEmployeeService.java
- [ ] LoadingManager
- [ ] Code quality report

### 🔧 TOOLS & RESOURCES
- IntelliJ IDEA code analysis
- SonarLint plugin
- Checkstyle

---

## 👤 PERSON 5: EMPLOYEE MODULE FULL-STACK DEVELOPER
**Role**: Employee Management - Backend API + Frontend UI (Toàn bộ)  
**Độ ưu tiên**: � CRITICAL - Module mới hoàn chỉnh  
**Dependencies**: Chờ Person 1 (CSS for styling) - có thể code logic trước

### 📋 CÔNG VIỆC CHI TIẾT

#### **Ngày 1 (24/10) - Planning & Backend API Setup**
```
Sáng (4h):
- [ ] Requirements analysis:
  - List tất cả fields của Employee (theo DB schema)
  - Define CRUD operations cần thiết
  - Design UI mockups (paper/Figma)
  
Chiều (4h):
- [ ] Backend - Mount UserController:
  - Vào ServerMain.java add: UserController.mount(router);
  - Test GET /users với Postman
  - Test GET /users/:id
  - Verify database connection
```

#### **Ngày 2 (25/10) - Complete Backend API**
```
Sáng (4h):
- [ ] Implement Employee API:
  - POST /users (create employee)
  - PUT /users/:id (update employee)
  - DELETE /users/:id (soft delete - set is_active=false)
  - Test tất cả với Postman
  
Chiều (4h):
- [ ] Employee business logic & validation:
  - Username unique check
  - Email format validation
  - Role-based queries: filter by role (doctor/nurse)
  - Search employees by name/email/phone
  - License_no required if role=doctor
```

#### **Ngày 3 (26/10) - Sequence Diagram & FXML Views**
```
Sáng (4h):
- [ ] Tạo Sequence Diagram:
  - EmployeeManagement.puml (trong UML/Sequence/Employee/)
  - Bao gồm: View list, Add, Edit, Delete flows
  - Tất cả error scenarios (400, 401, 403, 404, 409, 500, 503)
  
Chiều (4h):
- [ ] Tạo EmployeeManagement.fxml:
  - TableView: id, username, firstname, lastname, role, email, phone, active
  - Search TextField
  - Filter ComboBox (All/Doctor/Nurse, Active/Inactive)
  - Buttons: Add, Edit, Delete, Refresh
  - (Chưa style CSS, dùng default trước)
```

#### **Ngày 4 (27/10) - Controllers & Service**
```
Sáng (4h):
- [ ] Tạo HttpEmployeeService.java:
  - getAllEmployees()
  - getEmployeeById(int id)
  - createEmployee(Employee employee)
  - updateEmployee(Employee employee)
  - deleteEmployee(int id)
  - searchEmployees(String keyword)
  - Proper error handling
  
Chiều (4h):
- [ ] Tạo EmployeeManagementController.java:
  - extends BaseController
  - initialize() - load employees từ API
  - Search & filter logic (role, active status)
  - handleAdd() - open form dialog
  - handleEdit() - load selected employee
  - handleDelete() - confirmation dialog
  - handleRefresh()
```

#### **Ngày 5 (28/10) - Form & Validation**
```
Sáng (4h):
- [ ] Tạo EmployeeForm.fxml:
  - TextField: username, firstname, lastname, email, phone, license_no
  - PasswordField: password (chỉ khi add new)
  - ComboBox: role (Doctor/Nurse)
  - CheckBox: is_active
  - Buttons: Save, Cancel
  
Chiều (4h):
- [ ] Tạo EmployeeFormController.java:
  - Form validation:
    * Username required, unique
    * Email format validation
    * Phone format validation (10 digits)
    * Password strength (min 6 chars)
    * License_no required if role=Doctor
  - handleSave() - call HttpEmployeeService
  - Error handling với ErrorHandler
  - Success notification
```

#### **Ngày 6 (29/10) - Integration & Testing**
```
Sáng (4h):
- [ ] Integrate vào Dashboard:
  - Add menu item "Quản lý nhân sự" vào Dashboard
  - Role-based access (chỉ ADMIN thấy - check session)
  - Test navigation SceneManager
  - Apply CSS từ Person 1
  
Chiều (4h):
- [ ] Full Testing:
  - Test Add employee (Doctor vs Nurse validation)
  - Test Edit employee
  - Test Delete (soft delete check)
  - Test Search & Filter
  - Test tất cả error scenarios
  - Test với nhiều concurrent users
```

#### **Ngày 7 (30/10) - Build Desktop App & Final Polish**
```
Sáng (4h):
- [ ] Build JavaFX Desktop App:
  - Maven: mvn clean package
  - Test JAR: java -jar target/oop_ui.jar
  - jpackage: Create Windows .exe installer
  - Test installation process
  
Chiều (4h):
- [ ] Final Polish & Documentation:
  - Fix any bugs từ testing
  - Update frontend với production API URL
  - Employee module documentation
  - BUILD_GUIDE.md (how to build app)
  - Demo video/screenshots
```

### 📦 DELIVERABLES
- [ ] **Backend**: Employee API hoàn chỉnh (mounted & tested)
- [ ] **Frontend**: EmployeeManagement.fxml + Controller
- [ ] **Frontend**: EmployeeForm.fxml + Controller
- [ ] **Service**: HttpEmployeeService.java
- [ ] **UML**: EmployeeManagement.puml (Sequence Diagram)
- [ ] **Integration**: Module integrated vào Dashboard
- [ ] **Desktop App**: Windows .exe installer
- [ ] **Documentation**: Đầy đủ docs + BUILD_GUIDE.md

### 🔧 TOOLS & RESOURCES
- Postman: Test Employee API
- PlantUML extension (VS Code): Vẽ sequence diagram
- Scene Builder: Design FXML
- jpackage (JDK 17+): Build Windows installer
- PostgreSQL/Supabase: Database access

### ⚠️ IMPORTANT NOTES
- **Độc lập hoàn toàn**: Backend + Frontend cùng 1 người → Không có communication overhead
- **Database schema đã có**: Bảng Employees đã tồn tại trong DB
- **UserController đã có code**: Chỉ cần mount vào router
- **CSS**: Có thể code structure trước, apply CSS sau khi Person 1 xong
- **Testing**: Tự test end-to-end vì own cả stack

---

## 📅 TIMELINE OVERVIEW

```
        Day 1    Day 2    Day 3    Day 4    Day 5    Day 6    Day 7
Person1  CSS     CSS+     Dash     Modules  Modules  Employee Final
         Found   Auth     board    (Inv,    (Pay,    Module   Polish
                                   Sched)   Patient)

Person2  Setup   Employ   Error    Missing  Testing  Docker   API
         Test    API      Handle   Features         Deploy   Docs

Person3  Scene   Dash     Home     Update   Nav      Error    Test
         Mgr     board    View     Ctrls    Polish   Screens  Opt

Person4  Error   Update   Update   Update   Loading  Code     Test
         Frame   Svc1-2   Svc3-5   Svc6-9   Mgr      Cleanup  Doc

Person5  Plan    FXML     Ctrls    Ctrls    Integ    Fix      Fix
         Design  Views    Part1    Part2    Test     TODO1    TODO2
```

---

## 🔄 INTEGRATION SCHEDULE

### **Merge Points** (Sync vào dev branch):

#### **End of Day 2** (25/10 - 18:00):
- ✅ Person 1: main.css, components.css, layout.css ready
- ✅ Person 2: Employee API mounted và tested
- ✅ Person 3: SceneManager ready
- ✅ Person 4: ErrorHandler framework ready
- ✅ Person 5: Sequence diagram ready

**Action**: Team meeting - Demo progress, resolve conflicts

#### **End of Day 4** (27/10 - 18:00):
- ✅ Person 1: Auth screens + Inventory/Schedule modules styled
- ✅ Person 2: All missing endpoints implemented
- ✅ Person 3: Dashboard + HomeView integrated
- ✅ Person 4: 50% services updated
- ✅ Person 5: Employee FXML + Controllers ready

**Action**: Integration testing session

#### **End of Day 6** (29/10 - 18:00):
- ✅ Person 1: Employee module styled
- ✅ Person 2: Deployed to production
- ✅ Person 3: All navigation updated
- ✅ Person 4: All services updated
- ✅ Person 5: Employee module working + TODOs fixed

**Action**: Full system test, bug bash

#### **Day 7** (30/10):
- 🎯 **DEMO DAY** - Final polish, documentation, presentation prep

---

## 🤝 DAILY STANDUP (15 minutes mỗi sáng)

### Format:
1. **Yesterday**: Tôi đã làm gì?
2. **Today**: Tôi sẽ làm gì?
3. **Blockers**: Có vấn đề gì cần support?

### Example:
```
Person 1: 
- Yesterday: Hoàn thành main.css và components.css
- Today: Apply CSS cho Login.fxml và Signup.fxml
- Blockers: Không có

Person 2:
- Yesterday: Mount UserController, test GET endpoints
- Today: Implement POST và PUT cho Employee
- Blockers: Cần confirm validation rules với Person 5

Person 3:
- Yesterday: Tạo SceneManager với cache logic
- Today: Bắt đầu Dashboard.fxml structure
- Blockers: Chờ main.css từ Person 1 để test styling

Person 4:
- Yesterday: Tạo ErrorHandler framework
- Today: Update HttpAppointmentService
- Blockers: Không có

Person 5:
- Yesterday: Hoàn thành EmployeeManagement.puml
- Today: Bắt đầu EmployeeManagement.fxml
- Blockers: Cần confirm API endpoints với Person 2
```

---

## 🎯 DEPENDENCY MATRIX

```
          Person1  Person2  Person3  Person4  Person5
          (CSS)    (API)    (Nav)    (Error)  (Emp)
Person1   -        ✗        ✗        ✗        ✗
Person2   ✗        -        ✗        ✗        ✗
Person3   ▲        ✗        -        ✗        ✗
Person4   ✗        ▲        ✗        -        ✗
Person5   ▲        ▲        ✗        ✗        -

Legend:
✗ = Không phụ thuộc
▲ = Phụ thuộc nhẹ (có thể làm song song)
● = Phụ thuộc nặng (phải chờ)
```

**Analysis**:
- ✅ Person 1 & 2: **Hoàn toàn độc lập** - có thể bắt đầu ngay
- ✅ Person 3: Chờ CSS từ Person 1 cho styling, nhưng **logic code độc lập**
- ✅ Person 4: Chờ API từ Person 2 để test, nhưng **framework code độc lập**
- ✅ Person 5: Chờ API (Person 2) và CSS (Person 1), nhưng **planning & FXML structure độc lập**

---

## 📊 SUCCESS METRICS

### **Individual KPIs**:

**Person 1 (CSS)**:
- [ ] 100% FXML files có CSS
- [ ] Không còn inline styles
- [ ] Style guide document

**Person 2 (Backend)**:
- [ ] 100% API endpoints tested
- [ ] Production deployment success
- [ ] API documentation complete

**Person 3 (Navigation)**:
- [ ] Dashboard hoạt động
- [ ] 0 manual FXMLLoader code còn lại
- [ ] Navigation smooth, no lag

**Person 4 (Error Handling)**:
- [ ] 9/9 services updated
- [ ] 0 unhandled exceptions
- [ ] User-friendly error messages

**Person 5 (Employee Module)**:
- [ ] Employee CRUD works end-to-end
- [ ] Sequence diagram complete
- [ ] 80% TODOs fixed

### **Team KPIs**:
- [ ] **100% test coverage** for critical paths
- [ ] **< 2 seconds** API response time
- [ ] **0 critical bugs** in production
- [ ] **Demo ready** by Day 7

---

## 🚨 RISK MANAGEMENT

### **Potential Risks & Mitigation**:

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| CSS không tương thích với FXML | Medium | High | Person 1 test với Scene Builder liên tục |
| API không ready kịp | Low | High | Person 2 ưu tiên Employee API trước |
| Merge conflicts | High | Medium | Daily syncs, feature branches |
| Person bị bệnh/vắng | Medium | High | Cross-training, documentation |
| Bug phát sinh cuối tuần | Medium | High | Buffer time Day 7 |

### **Contingency Plans**:

**Nếu Person 1 chậm (CSS)**:
- Person 3, 5 code structure trước, style sau
- Use default JavaFX styling tạm thời

**Nếu Person 2 chậm (API)**:
- Person 4, 5 dùng mock data để test
- Deploy local server để test

**Nếu Person 3 chậm (Navigation)**:
- Giữ nguyên navigation cũ, refactor sau
- Priority Dashboard trước

---

## 🎉 CELEBRATION & RETROSPECTIVE

### **End of Week Celebration** (30/10 - Evening):
- 🍕 Team dinner/lunch
- 🏆 Award "Best Contributor"
- 📸 Team photo với app chạy
- 🎤 Share lessons learned

### **Retrospective Questions**:
1. What went well?
2. What could be improved?
3. What should we start/stop/continue?
4. Biggest challenge và cách overcome?

---

## 📞 COMMUNICATION CHANNELS

### **Slack/Discord Channels**:
- `#daily-standup` - Daily updates
- `#css-styling` - Person 1 Q&A
- `#backend-api` - Person 2 Q&A
- `#navigation` - Person 3 Q&A
- `#error-handling` - Person 4 Q&A
- `#employee-module` - Person 5 Q&A
- `#general` - Team chat
- `#blockers` - Urgent issues

### **Response Time SLA**:
- 🔴 Blocker: < 30 minutes
- 🟡 Question: < 2 hours
- 🟢 Discussion: < 4 hours

---

## ✅ FINAL CHECKLIST (Day 7 - Before Demo)

### **Code**:
- [ ] All code committed và pushed
- [ ] No console errors
- [ ] All TODOs addressed or documented
- [ ] Code reviewed by at least 1 peer

### **Functionality**:
- [ ] Login → Dashboard flow works
- [ ] All CRUD operations work
- [ ] Error messages display correctly
- [ ] Employee module works end-to-end

### **Visual**:
- [ ] CSS applied everywhere
- [ ] Consistent look & feel
- [ ] No layout issues
- [ ] Loading indicators work

### **Deployment**:
- [ ] Backend deployed và accessible
- [ ] Frontend connects to production API
- [ ] Database seeded với demo data
- [ ] Backup plan if deployment fails

### **Documentation**:
- [ ] README.md updated
- [ ] API documentation complete
- [ ] User guide ready
- [ ] Demo script prepared

### **Demo Prep**:
- [ ] Demo data seeded
- [ ] Demo flow rehearsed
- [ ] Screenshots/Video ready
- [ ] Presentation slides ready
- [ ] Backup plan (local demo nếu network fail)

---

## 🎯 RECOMMENDED WORKING HOURS

### **Core Hours** (Cùng online):
- **9:00 - 12:00**: Morning session (Daily standup 9:00-9:15)
- **14:00 - 18:00**: Afternoon session

### **Flexible Hours**:
- Early birds: 7:00-9:00
- Night owls: 20:00-22:00

### **No-Meeting Hours** (Deep work):
- 10:00-12:00
- 15:00-17:00

---

## 🏆 MOTIVATION & TEAM SPIRIT

### **Daily Wins**:
- Share 1 thing you're proud of mỗi ngày
- Celebrate small victories
- Help each other

### **Quotes**:
> "Alone we can do so little; together we can do so much." - Helen Keller

> "Coming together is a beginning, staying together is progress, and working together is success." - Henry Ford

> "The strength of the team is each individual member. The strength of each member is the team." - Phil Jackson

---

**🚀 LET'S BUILD SOMETHING AMAZING TOGETHER! 🚀**

---

*Last updated: 24/10/2025*  
*Version: 1.0*  
*Team: BTL_OOP - Eye Clinic Management System*
