# Student Activity & Academic Tracking System

A full-stack web application for B.Tech colleges to manage student activities, attendance tracking, performance monitoring, and analytics.

---

## Table of Contents

1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [Features](#features)
4. [Project Structure](#project-structure)
5. [Prerequisites](#prerequisites)
6. [Setup & Installation](#setup--installation)
7. [Configuration](#configuration)
8. [API Reference](#api-reference)
9. [Data Models](#data-models)
10. [Role-Based Access Control](#role-based-access-control)
11. [Security](#security)
12. [Building for Production](#building-for-production)

---

## Overview

The Student Activity Tracking System is designed for B.Tech colleges to digitize and streamline:
- Student and faculty management
- Class attendance tracking
- Academic performance recording
- Report generation and analytics

Three user roles interact with the system: **Admin**, **Faculty**, and **Student**, each with a dedicated dashboard and scoped permissions.

---

## Technology Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Spring Boot | 3.2.0 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| MongoDB | Atlas / 6.x | Database |
| JWT (jjwt) | 0.12.3 | Token-based auth |
| Apache POI | 5.2.5 | Excel bulk upload |
| iText7 | 8.0.2 | PDF generation |
| SpringDoc OpenAPI | 2.3.0 | Swagger UI |
| Maven | 3.8+ | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 18.2 | UI framework |
| Vite | 5.x | Build tool |
| React Router | 6.x | Client-side routing |
| Axios | 1.6 | HTTP client |
| Recharts | 2.x | Charts & analytics |
| Formik + Yup | 2.x / 1.x | Forms & validation |
| React Toastify | 9.x | Notifications |

---

## Features

### Admin
- Manage students (create, update, soft delete, bulk Excel upload)
- Manage faculty accounts
- Manage courses and subjects
- Assign faculty to classes (class allocations)
- View audit logs of all system actions
- Dashboard with system-wide statistics and charts

### Faculty
- View assigned class allocations
- Mark attendance (individual or bulk for entire class)
- Enter student performance/marks with auto grade calculation
- Generate attendance and performance reports (view + CSV export)
- Search students by roll number

### Student
- View personal attendance with subject-wise breakdown
- View performance records and grades
- View enrolled subjects
- Download personal attendance report as CSV
- Dashboard with overall attendance % and GPA

---

## Project Structure

```
student-activity-tracking/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/college/activitytracker/
│   │   ├── config/                   # CORS, Security, MongoDB, Rate limiting
│   │   ├── controller/               # REST controllers
│   │   │   ├── AuthController        # Login, register, token refresh
│   │   │   ├── AdminController       # Admin-only endpoints
│   │   │   ├── FacultyController     # Faculty-only endpoints
│   │   │   ├── StudentController     # Student-only endpoints
│   │   │   └── StudentSearchController # Roll number search
│   │   ├── service/                  # Business logic
│   │   ├── repository/               # MongoDB repositories
│   │   ├── model/                    # MongoDB document models
│   │   ├── dto/                      # Request/response DTOs
│   │   ├── security/                 # JWT filter, UserDetails
│   │   ├── exception/                # Global exception handler
│   │   └── util/                     # Mappers and helpers
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
└── frontend/                         # React application
    └── src/
        ├── api/
        │   ├── axios.config.js       # Axios instance with interceptors
        │   └── endpoints/            # API call functions per domain
        ├── components/
        │   ├── layout/               # Navbar
        │   └── common/               # Shared UI components
        ├── context/
        │   └── AuthContext.jsx       # Auth state, login/logout, token storage
        ├── pages/
        │   ├── auth/                 # Login page
        │   ├── admin/                # Admin dashboard and management pages
        │   ├── faculty/              # Faculty dashboard, attendance, performance, reports
        │   └── student/              # Student dashboard, attendance, performance views
        ├── routes/
        │   └── AppRoutes.jsx         # Route definitions with role guards
        ├── App.jsx
        └── main.jsx
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- MongoDB Atlas account or local MongoDB 6.x

---

## Setup & Installation

### 1. Clone the repository

```bash
git clone https://github.com/karthik9s/student-activity-tracking.git
cd student-activity-tracking
```

### 2. Backend setup

```bash
cd backend

# Edit MongoDB URI and JWT secret in:
# src/main/resources/application.properties

# Compile
mvn clean compile

# Run
mvn spring-boot:run
# Backend starts on http://localhost:8080
```

### 3. Frontend setup

```bash
cd frontend

npm install

# Create environment file
echo "VITE_API_URL=http://localhost:8080/api/v1" > .env

npm run dev
# Frontend starts on http://localhost:5173
```

---

## Configuration

### `backend/src/main/resources/application.properties`

```properties
# MongoDB connection
spring.data.mongodb.uri=mongodb+srv://<user>:<password>@cluster.mongodb.net/student_tracker

# JWT
jwt.secret=your-256-bit-secret-key          # Change in production
jwt.access-token-expiration=3600000          # 1 hour (ms)
jwt.refresh-token-expiration=604800000       # 7 days (ms)

# CORS - add your frontend URL
cors.allowed-origins=http://localhost:5173

# Rate limiting
security.rate-limit.requests-per-minute=100
security.brute-force.max-attempts=5
security.brute-force.lockout-duration-minutes=15

# File upload
spring.servlet.multipart.max-file-size=10MB
file.upload-dir=./uploads
```

### `frontend/.env`

```env
VITE_API_URL=http://localhost:8080/api/v1
```

---

## API Reference

Interactive Swagger UI is available at `http://localhost:8080/swagger-ui.html` when the backend is running.

### Authentication — `/api/v1/auth`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/login` | Login with email + password | Public |
| POST | `/register` | Register new user | Public |
| POST | `/refresh` | Refresh access token | Public |

**Login request:**
```json
{
  "email": "admin@cvr.ac.in",
  "password": "admin123"
}
```

**Login response:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "user": {
    "id": "...",
    "email": "admin@cvr.ac.in",
    "role": "ROLE_ADMIN"
  }
}
```

---

### Admin — `/api/v1/admin` *(ROLE_ADMIN)*

**Students**
| Method | Endpoint | Description |
|---|---|---|
| GET | `/students` | List all students (paginated) |
| POST | `/students` | Create student |
| PUT | `/students/{id}` | Update student |
| DELETE | `/students/{id}` | Soft delete student |
| POST | `/students/bulk-upload` | Bulk import from Excel |

**Faculty**
| Method | Endpoint | Description |
|---|---|---|
| GET | `/faculty` | List all faculty |
| POST | `/faculty` | Create faculty |
| PUT | `/faculty/{id}` | Update faculty |
| DELETE | `/faculty/{id}` | Delete faculty |

**Courses & Subjects**
| Method | Endpoint | Description |
|---|---|---|
| GET | `/courses` | List courses |
| POST | `/courses` | Create course |
| GET | `/subjects` | List subjects |
| POST | `/subjects` | Create subject |

**Class Allocations**
| Method | Endpoint | Description |
|---|---|---|
| GET | `/allocations` | List all allocations |
| POST | `/allocations` | Assign faculty to class |
| PUT | `/allocations/{id}` | Update allocation |
| DELETE | `/allocations/{id}` | Remove allocation |

**Audit Logs**
| Method | Endpoint | Description |
|---|---|---|
| GET | `/audit-logs` | View all audit logs |

---

### Faculty — `/api/v1/faculty` *(ROLE_FACULTY)*

**Attendance**
| Method | Endpoint | Description |
|---|---|---|
| POST | `/attendance` | Mark single attendance |
| POST | `/attendance/bulk` | Mark bulk attendance |
| GET | `/attendance/subject/{subjectId}/date/{date}` | Get attendance by subject and date |
| PUT | `/attendance/{id}` | Update attendance record |

**Performance**
| Method | Endpoint | Description |
|---|---|---|
| POST | `/performance` | Add performance record |
| GET | `/performance/subject/{subjectId}` | Get all performance for a subject |
| PUT | `/performance/{id}` | Update performance record |

**Reports**
| Method | Endpoint | Description |
|---|---|---|
| GET | `/reports/attendance` | Attendance report (all students) |
| GET | `/reports/low-attendance` | Students below 75% attendance |
| GET | `/reports/attendance/pdf` | Download attendance PDF |
| GET | `/reports/attendance/csv` | Download attendance CSV |
| GET | `/reports/performance/csv` | Download performance CSV |

**Other**
| Method | Endpoint | Description |
|---|---|---|
| GET | `/allocations` | Get my class allocations |
| GET | `/students` | Get students by course/year/section |

---

### Student — `/api/v1/student` *(ROLE_STUDENT)*

| Method | Endpoint | Description |
|---|---|---|
| GET | `/profile` | Get my profile |
| GET | `/attendance` | Get my attendance records |
| GET | `/attendance/summary` | Attendance summary with subject breakdown |
| GET | `/performance` | Get my performance records |
| GET | `/performance/summary` | Performance summary with GPA |
| GET | `/subjects` | Get enrolled subjects |
| GET | `/dashboard/stats` | Dashboard statistics |
| GET | `/notifications` | Get notifications |
| PUT | `/notifications/{id}/read` | Mark notification as read |

---

### Student Search — `/api/v1/students` *(ROLE_ADMIN or ROLE_FACULTY)*

| Method | Endpoint | Description |
|---|---|---|
| GET | `/search?rollNumber=DS21` | Search students by partial roll number |

**Response:**
```json
[
  {
    "studentId": "69b06fe9470fd61eb7fdc815",
    "name": "Varun Reddy",
    "rollNumber": "DS21A001"
  }
]
```

---

## Data Models

### Student
```
id, userId, rollNumber, name, email, phone,
dateOfBirth, courseId, year, section,
isActive, createdAt, updatedAt, deletedAt
```

### Faculty
```
id, userId, employeeId, firstName, lastName, email,
phone, department, designation, isActive,
createdAt, updatedAt, deletedAt
```

### Attendance
```
id, studentId, subjectId, facultyId, courseId,
year, section, date, status (PRESENT/ABSENT),
remarks, createdAt, updatedAt
```

### Performance
```
id, studentId, subjectId, facultyId, courseId,
year, section, semester, examType (INTERNAL/ASSIGNMENT/EXAM/FINAL),
marksObtained, totalMarks, percentage, grade,
remarks, createdAt, updatedAt
```

**Grade scale:**
| Percentage | Grade |
|---|---|
| ≥ 90 | A+ |
| ≥ 80 | A |
| ≥ 70 | B+ |
| ≥ 60 | B |
| ≥ 50 | C |
| ≥ 40 | D |
| < 40 | F |

### ClassAllocation
```
id, facultyId, subjectId, courseId, year, section,
academicYear, isActive, createdAt
```

### Course
```
id, name, code, duration (years), description, isActive
```

### Subject
```
id, name, code, courseId, year, semester,
credits, description, isActive
```

---

## Role-Based Access Control

| Feature | Admin | Faculty | Student |
|---|---|---|---|
| Manage students | ✅ | ❌ | ❌ |
| Manage faculty | ✅ | ❌ | ❌ |
| Manage courses/subjects | ✅ | ❌ | ❌ |
| Class allocations | ✅ | ❌ | ❌ |
| Audit logs | ✅ | ❌ | ❌ |
| Mark attendance | ❌ | ✅ | ❌ |
| Enter performance | ❌ | ✅ | ❌ |
| Generate reports | ❌ | ✅ | ❌ |
| Search students | ✅ | ✅ | ❌ |
| View own attendance | ❌ | ❌ | ✅ |
| View own performance | ❌ | ❌ | ✅ |
| Download own report | ❌ | ❌ | ✅ |

---

## Security

- JWT access tokens expire in **1 hour**; refresh tokens in **7 days**
- Tokens stored in `localStorage` (remember me) or `sessionStorage` (session only)
- Brute-force protection: account locked for **15 minutes** after **5 failed attempts**
- Rate limiting: **100 requests/minute** per IP
- CORS restricted to configured origins
- All sensitive endpoints require valid JWT in `Authorization: Bearer <token>` header
- Faculty can only mark attendance/performance for classes they are allocated to

**Production checklist:**
- Change `jwt.secret` to a strong random value and store in environment variable
- Set `cors.allowed-origins` to your actual frontend domain
- Enable HTTPS
- Enable MongoDB authentication

---

## Building for Production

### Backend
```bash
cd backend
mvn clean package
java -jar target/activity-tracker-1.0.0.jar
```

### Frontend
```bash
cd frontend
npm run build
# Output in dist/ — deploy to Vercel, Netlify, or serve via Nginx
```

---

## License

Developed for educational purposes — CVR College of Engineering.
