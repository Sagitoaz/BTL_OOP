# 🏥 Eye Clinic Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A comprehensive medical clinic management system built with Java and JavaFX**

[Features](#-features) • [Architecture](#-architecture) • [Installation](#-installation) • [Usage](#-usage) • [Documentation](#-documentation)

</div>

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Architecture](#-architecture)
- [Technologies](#-technologies)
- [Project Structure](#-project-structure)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Usage](#-usage)
- [Documentation](#-documentation)
- [Contributing](#-contributing)
- [License](#-license)
- [Contact](#-contact)

---

## 🎯 About

**Eye Clinic Management System** is a comprehensive desktop application designed to streamline the operations of eye clinics and optometry practices. Built as an Object-Oriented Programming (OOP) project, this system provides a complete solution for managing patients, appointments, prescriptions, inventory, and financial operations.

The application follows modern software architecture principles with a clear separation between the presentation layer (JavaFX UI) and business logic layer (Mini-Boot backend), ensuring maintainability, scalability, and testability.

### 🎓 Project Information

- **Course**: Object-Oriented Programming (OOP)
- **Institution**: Post and Telecommunications Institute of Technology (PTIT)
- **Instructor**: Nguyen Manh Son
- **Academic Year**: 2024-2025
- **Team Members**: 
  - Dương Trí Dũng
  - Trần Văn Hậu
  - Phan Minh Hiếu
  - Nguyễn Minh Toàn
  - Nguyễn Thành Trung

---

## ✨ Features

### 👤 User Management
- 🔐 **Multi-role Authentication System**
  - Admin, Doctor, Nurse, Customer
  - Secure login with JWT token authentication
  - Password encryption using SHA256
- 👥 **Account Management**
  - User registration and profile management
  - Password reset and change functionality
  - Role-based access control (RBAC)

### 🏥 Patient & Examination Management
- 📝 **Patient Records**
  - Comprehensive patient information management
  - Medical history tracking
  - Patient search and filtering
- 📅 **Appointment System**
  - Schedule patient appointments
  - Doctor availability management
  - Time slot booking
  - Appointment notifications
- 🔬 **Examination Process**
  - Record examination results
  - Prescription management
  - Treatment history
  - Medical reports generation

### 💊 Inventory Management
- 📦 **Stock Management**
  - Medicine and equipment tracking
  - Low stock alerts
  - Expiry date monitoring
- ➕ **Inventory Operations**
  - Add, edit, and delete inventory items
  - Stock level adjustments
  - Supplier management
- 📊 **Reports**
  - Inventory status reports
  - Usage statistics
  - Export to Excel

### 💰 Financial Management
- 🧾 **Invoice Generation**
  - Automated invoice creation
  - Service and medication billing
  - Tax calculations
- 💳 **Payment Processing**
  - Multiple payment methods
  - Payment history tracking
  - Receipt generation (PDF)
- 📈 **Financial Reports**
  - Revenue reports
  - Payment analytics
  - Export to Excel and PDF

### 📊 Dashboard & Analytics
- 📉 **Real-time Statistics**
  - Patient statistics
  - Appointment metrics
  - Revenue tracking
- 📅 **Schedule Management**
  - Doctor schedules
  - Working hours management
  - Holiday and leave tracking

---

## 🏗️ Architecture

The system follows a **modular multi-tier architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│      (JavaFX UI - oop_ui module)        │
│  - Controllers                          │
│  - FXML Views                           │
│  - CSS Styling                          │
└──────────────┬──────────────────────────┘
               │
               ↓ REST API / Service Layer
┌──────────────────────────────────────────┐
│         Business Logic Layer             │
│      (Mini-Boot - mini-boot module)      │
│  - Services                              │
│  - Business Rules                        │
│  - Authentication & Authorization        │
└──────────────┬───────────────────────────┘
               │
               ↓ Data Access Layer
┌──────────────────────────────────────────┐
│          Database Layer                  │
│         (PostgreSQL / MS SQL)            │
│  - Data Models                           │
│  - Repositories                          │
└──────────────────────────────────────────┘
```

### Design Patterns Used
- **MVC (Model-View-Controller)**: Separation of UI and business logic
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic encapsulation
- **Singleton Pattern**: Database connection management
- **Factory Pattern**: Object creation
- **Observer Pattern**: Event handling in JavaFX

---

## 🛠️ Technologies

### Frontend (oop_ui)
- **JavaFX 21.0.2** - Modern desktop UI framework
- **FXML** - UI markup language
- **CSS** - Custom styling and themes
- **ControlsFX 11.2.1** - Extended JavaFX controls

### Backend (mini-boot)
- **Java 21** - Latest LTS version
- **Custom Mini-Boot Framework** - Lightweight backend framework
- **JWT (java-jwt 4.4.0)** - Authentication tokens
- **BCrypt** - Password hashing
- **HikariCP 5.1.0** - High-performance connection pool

### Database
- **PostgreSQL** - Primary database (Supabase)
- **MS SQL Server** - Alternative database support

### Build & Dependencies
- **Maven 3.8+** - Dependency management and build tool
- **Gson 2.10.1** - JSON serialization
- **Jackson 2.17.2** - JSON processing
- **Apache POI 5.2.5** - Excel export functionality
- **iText 7.2.5** - PDF generation

### Additional Libraries
- **Jakarta Mail 2.0.1** - Email functionality
- **Log4j 2.20.0** - Logging framework

---

## 📁 Project Structure

```
BTL_OOP/OOP/
├── 📄 pom.xml                          # Parent Maven configuration
├── 📄 README.md                        # This file
│
├── 📁 mini-boot/                       # Backend module
│   ├── 📄 pom.xml                      # Backend dependencies
│   ├── 📄 Dockerfile                   # Docker configuration
│   ├── 📄 docker-compose.yml           # Docker Compose setup
│   ├── 📄 application.properties.example
│   └── 📁 src/main/java/org/miniboot/
│       └── app/                        # Backend application code
│
├── 📁 oop_ui/                          # Frontend module (JavaFX)
│   ├── 📄 pom.xml                      # Frontend dependencies
│   ├── 📁 src/main/
│   │   ├── 📁 java/org/example/oop/
│   │   │   ├── 📁 Control/             # Controllers (MVC)
│   │   │   ├── 📁 Model/               # Data models
│   │   │   ├── 📁 Service/             # Service layer
│   │   │   ├── 📁 Utils/               # Utility classes
│   │   │   ├── 📁 config/              # Configuration
│   │   │   └── 📄 Main.java            # Application entry point
│   │   │
│   │   └── 📁 resources/
│   │       ├── 📁 FXML/                # UI layouts
│   │       │   ├── Login.fxml
│   │       │   ├── Dashboard/
│   │       │   ├── Employee/
│   │       │   ├── Inventory/
│   │       │   └── PatientAndPrescription/
│   │       │
│   │       ├── 📁 css/                 # Stylesheets
│   │       │   ├── global-styles.css
│   │       │   ├── dashboard.css
│   │       │   ├── forms.css
│   │       │   └── tables.css
│   │       │
│   │       ├── 📁 Image/               # Images and icons
│   │       └── 📄 error_messages.properties
│   │
│   └── 📁 data/                        # Test data
│       └── ScheduleTestData/
│
└── 📁 UML/                             # Documentation
    ├── 📁 Activity/                    # Activity diagrams
    ├── 📁 Class/                       # Class diagrams
    ├── 📁 Database/                    # Database schema
    ├── 📁 Sequence/                    # Sequence diagrams
    │   ├── Account/
    │   ├── Examination/
    │   ├── Inventory/
    │   └── Payment/
    └── 📁 Usecase Diagram/             # Use case diagrams
```

---

## 🚀 Installation

### Prerequisites

Before you begin, ensure you have the following installed:

- ☕ **Java Development Kit (JDK) 21** or higher
  ```bash
  java -version  # Should show version 21 or higher
  ```

- 📦 **Apache Maven 3.8+**
  ```bash
  mvn -version
  ```

- 🗄️ **PostgreSQL 15+** (or MS SQL Server)
  - Or use a cloud database service like Supabase

- 🔧 **Git** (optional, for cloning)

### Step-by-Step Installation

#### 1️⃣ Clone the Repository

```bash
git clone https://github.com/Sagitoaz/BTL_OOP.git
```

Or download and extract the ZIP file.

#### 2️⃣ Database Setup

**Option A: Using Supabase (Recommended)**

1. Create a free account at [Supabase](https://supabase.com)
2. Create a new project
3. Navigate to SQL Editor and execute the schema from `UML/Database/database.dbml`
4. Copy your database connection string

**Option B: Using Local PostgreSQL**

1. Install PostgreSQL
2. Create a new database:
   ```sql
   CREATE DATABASE eye_clinic_db;
   ```
3. Run the SQL schema provided in the UML/Database directory

#### 3️⃣ Configure Database Connection

1. Navigate to `mini-boot/` directory
2. Copy the example configuration:
   ```bash
   cp application.properties.example application.properties
   ```
3. Edit `application.properties` with your database credentials:
   ```properties
   db.url=jdbc:postgresql://your-host:5432/your-database
   db.username=your-username
   db.password=your-password
   jwt.secret=your-secret-key
   ```

#### 4️⃣ Build the Project

From the root directory:

```bash
mvn clean install
```

This will:
- Download all dependencies
- Compile both modules (mini-boot and oop_ui)
- Run tests
- Package the application

#### 5️⃣ Run the Application

**Option A: Using Maven**

```bash
cd oop_ui
mvn javafx:run
```

**Option B: Using Executable JAR**

```bash
cd oop_ui/target
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar oop_ui-1.0-SNAPSHOT.jar
```

---

## ⚙️ Configuration

### Application Settings

Edit `mini-boot/application.properties`:

```properties
# Database Configuration
db.url=jdbc:postgresql://localhost:5432/eye_clinic_db
db.username=postgres
db.password=your_password
db.driver=org.postgresql.Driver

# Connection Pool Settings
db.pool.maximumPoolSize=10
db.pool.minimumIdle=5
db.pool.connectionTimeout=30000

# JWT Configuration
jwt.secret=your-secret-key-change-this-in-production
jwt.expiration=86400000  # 24 hours in milliseconds

# Email Configuration (for password reset)
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.username=your-email@gmail.com
mail.password=your-app-password
```

### Environment Variables (Production)

For production deployment, use environment variables instead of hardcoding:

```bash
export DB_URL=your-database-url
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export JWT_SECRET=your-secret-key
```

---

## 📖 Usage

### Default Login Credentials

After initial setup, use these default credentials:

**Admin Account:**
- Username: `admin_account`
- Password: `admin_password`

**Doctor Account:**
- Username: `doctor1`
- Password: `doctor123`

> ⚠️ **Security Note:** Change these default passwords immediately after first login!

### User Roles & Permissions

| Role         | Permissions                                               |
|--------------|-----------------------------------------------------------|
| **Admin**    | Full system access, user management, system configuration |
| **Doctor**   | Patient examination, prescription, view appointments      |
| **Nurse**    | Patient management, examination assistance                |
| **Customer** | Patient examination, prescription, view appointments      |

### Common Workflows

#### 1. Schedule an Appointment (Receptionist)
1. Login as Receptionist
2. Navigate to **Appointments** → **New Appointment**
3. Select patient (or register new patient)
4. Choose doctor and available time slot
5. Confirm booking

#### 2. Conduct Examination (Doctor)
1. Login as Doctor
2. View **Today's Appointments**
3. Select patient
4. Record examination findings
5. Create prescription
6. Complete examination

#### 3. Process Payment (Cashier)
1. Login as Cashier
2. Navigate to **Payments** → **Generate Invoice**
3. Select patient and services
4. Calculate total amount
5. Record payment method
6. Print receipt

#### 4. Manage Inventory (Inventory Staff)
1. Login as Inventory Staff
2. Navigate to **Inventory**
3. Add/Update items
4. Check low stock alerts
5. Generate inventory reports

---

## 📚 Documentation

### UML Diagrams

The project includes comprehensive UML documentation:

#### Use Case Diagrams
- Patient workflows (Account, Appointment, Examination, Payment)
- Staff workflows (Doctor, Nurse, Receptionist, Cashier, Inventory Staff)
- Admin workflows (User Management, System Configuration)

#### Class Diagrams
- Complete system class structure
- Relationships and dependencies
- Design patterns implementation

#### Sequence Diagrams
Located in `UML/Sequence/`:
- **Account Management**: Login, Signup, Password Reset, Change Password
- **Examination**: Appointment, Schedule, Patient Updates
- **Inventory**: Add, Edit, Delete, Search, View
- **Payment**: Invoice Generation, Payment Process, View Payments

#### Activity Diagrams
- System workflows and business processes
- Located in `UML/Activity/`

#### Database Schema
- Complete database design
- Entity relationships
- Available in `UML/Database/`
- View online: [dbdiagram.io](https://dbdiagram.io)

---

## 🐳 Docker Deployment (Optional)

The project includes Docker support for easy deployment:

### Build and Run with Docker

```bash
cd mini-boot
docker-compose up -d
```

This will start:
- The Mini-Boot backend service
- PostgreSQL database (if configured)

### Docker Configuration

Edit `mini-boot/docker-compose.yml` to customize:
- Port mappings
- Environment variables
- Database credentials

---

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Test Data

Sample test data is available in:
- `oop_ui/data/ScheduleTestData/`

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

### How to Contribute

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m "Add some amazing feature"
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Code Style Guidelines

- Follow Java naming conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Include JavaDoc for public methods
- Keep methods focused and concise
- Write unit tests for new features

### Reporting Issues

Found a bug? Have a suggestion? Please open an issue with:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Screenshots (if applicable)

---

## 🔧 Troubleshooting

### Common Issues

**Issue: JavaFX runtime components are missing**
```
Error: JavaFX runtime components are missing
```
**Solution:** Ensure JavaFX is properly configured in your IDE or add to module path

**Issue: Database connection failed**
```
Error: Unable to connect to database
```
**Solution:** 
- Check database credentials in `application.properties`
- Ensure database server is running
- Verify network connectivity

**Issue: Port already in use**
```
Error: Port 8080 is already in use
```
**Solution:** Change the server port in configuration or stop the conflicting service

**Issue: Maven build fails**
```
Error: Failed to execute goal
```
**Solution:** 
- Run `mvn clean` first
- Check internet connection for dependency downloads
- Verify Java version: `java -version`

---

## 📄 License

This project is licensed under the **License** - see the [LICENSE](LICENSE) file for details.

```
License

Copyright (c) 2025 Ngũ Hổ Tướng team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 👥 Authors & Acknowledgments

### Development Team

- Dương Trí Dũng
- Trần Văn Hậu
- Phan Minh Hiếu
- Nguyễn Minh Toàn
- Nguyễn Thành Trung

### Special Thanks

- **Nguyễn Mạnh Sơn** - Course Instructor & Project Advisor
- **Post and Telecomunication Institue of Technology** - Academic Support
- OpenJFX Team - JavaFX Framework
- Apache Software Foundation - Maven & Libraries
- All contributors and testers

---

<div align="center">

**Made with ❤️ by Ngũ Hổ Tướng**

**⭐ Star this repo if you find it useful! ⭐**

[Back to Top](#-eye-clinic-management-system)

</div>

