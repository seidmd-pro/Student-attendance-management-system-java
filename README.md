# Student Attendance Management System

A modern, role-based desktop application built with **Java Swing + FlatLaf** for managing student attendance across departments, courses, and classes.

---

## Features

- **Multi-role access** — Admin, Teacher, and Student portals
- **Attendance tracking** — Mark, edit, and view attendance by session
- **Reports & analytics** — Generate attendance summaries with export (CSV, Excel, PDF, Print)
- **Low attendance alerts** — Automatic notifications to students below threshold
- **Modern UI** — Gradient login, icon-based dashboard, dark mode toggle
- **Email notifications** — SMTP-based email alerts for low attendance

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | Java Swing + FlatLaf 3.4 |
| Build Tool | Apache Maven |
| Database | MySQL 8.0 |
| Password Hashing | BCrypt (jBCrypt) |
| Excel Export | Apache POI |
| PDF Export | iText 5 |
| Email | JavaMail |

---

## Prerequisites

### 1. Java JDK 17+
Download from https://adoptium.net  
Verify: `java -version`

### 2. MySQL 8.0+
Download from https://dev.mysql.com/downloads/installer/  
Verify: `mysql --version`

### 3. Apache Maven 3.6+
Download from https://maven.apache.org/download.cgi  
Add to PATH environment variable  
Verify: `mvn -version`

---

## Database Setup

1. Open MySQL and create the database:
```sql
CREATE DATABASE attendance_db;
```

2. The schema is auto-created on first run from `src/main/resources/schema.sql`

3. Configure your database connection in `db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/attendance_db
db.username=root
db.password=your_password
```

---

## Build & Run

### Build the project
```bash
mvn clean package -DskipTests
```
Wait for `BUILD SUCCESS` — downloads dependencies (~80MB on first run)

### Run the application
```bash
java -jar target\StudentAttendanceSystem-1.0.0-jar-with-dependencies.jar
```

### Or use the included batch file (Windows)
```bash
run.bat
```

---

## Default Admin Credentials

```
Email:    admin@attendance.com
Password: admin123
```

---

## First-Time Setup Order

Follow this order to set up the system from scratch:

1. **Admin → Departments** — Add a department
2. **Admin → Courses** — Add a course (linked to department)
3. **Admin → Classes** — Add a class (linked to course)
4. **Admin → Assign Teachers** — Link a teacher to a class
5. **Admin → Students** — Create student accounts
6. **Admin → Enrollments** — Enroll students into classes
7. **Teacher login → Mark Attendance** — Select class → Load Students → Mark → Save

---

## Project Structure

```
src/
└── main/
    ├── java/com/attendance/
    │   ├── config/          # Database configuration
    │   ├── dao/             # Data access objects
    │   ├── model/           # Entity models
    │   ├── service/         # Business logic
    │   ├── ui/
    │   │   ├── admin/       # Admin panels & dashboard
    │   │   ├── auth/        # Login page
    │   │   ├── common/      # Shared components (sidebar, base CRUD)
    │   │   ├── student/     # Student portal panels
    │   │   └── teacher/     # Teacher portal panels
    │   └── util/            # UIUtil, ThemeManager, ExportUtil
    └── resources/
        ├── icons/           # UI icons (PNG)
        └── schema.sql       # Database schema
```

---

## User Roles

### Admin
- Manage departments, courses, classes, teachers, students, enrollments
- View system-wide attendance reports
- Configure system settings (threshold, semester, email)
- Send low-attendance notifications

### Teacher
- Mark and edit attendance for assigned classes
- View class attendance reports
- Filter by date range

### Student
- View personal attendance dashboard
- Monthly attendance calendar
- Subject-wise breakdown
- Full attendance history

---

## Team

| Role | Responsibility |
|---|---|
| Frontend (UI) | Login page, Admin dashboard, UI design system |
| Backend | DAOs, services, database schema |
| Reports | Export utilities, report panels |
| Auth | Authentication, password management |

---

## License

This project is developed for academic purposes.
