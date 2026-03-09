# Design Document: Student Activity & Academic Tracking System

## Overview

The Student Activity & Academic Tracking System is a full-stack web application that provides comprehensive student management, attendance tracking, performance monitoring, and analytics for B.Tech colleges. The system implements a three-tier architecture with React frontend, Spring Boot backend, and MongoDB database, secured with JWT-based authentication and role-based access control.

### Technology Stack

- **Frontend**: React 18+ with functional components and hooks, Recharts for data visualization, Axios for HTTP requests, React Router for navigation
- **Backend**: Spring Boot 3.x with Spring Security, Spring Data MongoDB, Spring Validation
- **Database**: MongoDB 6.x with indexing and schema validation
- **Authentication**: JWT (JSON Web Tokens) with access and refresh token pattern
- **Security**: BCrypt password hashing, RBAC with three roles, CORS, CSRF protection

### Key Design Principles

1. **Separation of Concerns**: Clear layering (Controller → Service → Repository → Database)
2. **Security First**: Authentication and authorization at every layer
3. **Scalability**: Stateless design, pagination, caching, horizontal scaling readiness
4. **Maintainability**: Clean code, DTO pattern, consistent error handling
5. **Performance**: Database indexing, query optimization, lazy loading
6. **Testability**: Dependency injection, interface-based design, comprehensive test coverage

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     React Frontend (SPA)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Admin UI   │  │  Faculty UI  │  │  Student UI  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
│         └──────────────────┴──────────────────┘              │
│                            │                                 │
│                    Axios + Interceptors                      │
└────────────────────────────┬────────────────────────────────┘
                             │ HTTPS/REST API
                             │ JWT Token in Header
┌────────────────────────────┴────────────────────────────────┐
│              Spring Boot Backend (REST API)                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           Security Filter Chain                       │   │
│  │  JWT Authentication Filter → Authorization Filter    │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Controller Layer                         │   │
│  │  @RestController + @RequestMapping("/api/v1/...")    │   │
│  │  Input Validation (@Valid), Exception Handling       │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Service Layer                            │   │
│  │  @Service + Business Logic + DTO Mapping             │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Repository Layer                         │   │
│  │  MongoRepository + Custom Queries                    │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────┐
│                      MongoDB Database                        │
│  Collections: Users, Students, Faculty, Courses, Subjects,  │
│  Attendance, Performance, ClassAllocation, Notifications,   │
│  Announcements, AuditLogs                                   │
└─────────────────────────────────────────────────────────────┘
```

### Authentication Flow

```
1. User Login Request (POST /api/v1/auth/login)
   ↓
2. Backend validates credentials (BCrypt comparison)
   ↓
3. Generate JWT Access Token (1 hour expiry) + Refresh Token (7 days expiry)
   ↓
4. Return tokens to frontend
   ↓
5. Frontend stores tokens (localStorage/sessionStorage)
   ↓
6. All subsequent requests include: Authorization: Bearer <access_token>
   ↓
7. JWT Filter validates token and extracts user details
   ↓
8. Authorization Filter checks role permissions
   ↓
9. Request proceeds to controller if authorized
   ↓
10. When access token expires, use refresh token to get new access token
```

### Role-Based Access Control (RBAC)

```
Role Hierarchy:
ROLE_ADMIN (highest privileges)
  ├─ All CRUD operations on Students, Faculty, Courses, Subjects
  ├─ View all data across the system
  ├─ Manage system settings and announcements
  ├─ Access audit logs
  └─ System-wide analytics

ROLE_FACULTY (teaching privileges)
  ├─ Mark and edit attendance for allocated classes
  ├─ Add and update performance records for allocated subjects
  ├─ View student data for allocated classes
  ├─ Generate reports for allocated classes
  └─ View faculty dashboard

ROLE_STUDENT (view-only for own data)
  ├─ View own attendance records
  ├─ View own performance records
  ├─ View own dashboard and analytics
  └─ View notifications
```

## Components and Interfaces

### Backend Components

#### 1. Authentication & Security Components

**JwtTokenProvider**
- Purpose: Generate and validate JWT tokens
- Methods:
  - `generateAccessToken(UserDetails userDetails) -> String`: Creates access token with 1-hour expiry
  - `generateRefreshToken(UserDetails userDetails) -> String`: Creates refresh token with 7-day expiry
  - `validateToken(String token) -> boolean`: Validates token signature and expiry
  - `getUsernameFromToken(String token) -> String`: Extracts username from token
  - `getRolesFromToken(String token) -> List<String>`: Extracts roles from token

**JwtAuthenticationFilter**
- Purpose: Intercept requests and validate JWT tokens
- Methods:
  - `doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)`: Extracts token from header, validates, and sets authentication context

**CustomUserDetailsService**
- Purpose: Load user details for authentication
- Methods:
  - `loadUserByUsername(String username) -> UserDetails`: Loads user from database and returns UserDetails object

#### 2. Controller Layer

**AuthController**
- Endpoints:
  - `POST /api/v1/auth/register`: Register new user (admin only)
  - `POST /api/v1/auth/login`: Authenticate user and return tokens
  - `POST /api/v1/auth/refresh`: Refresh access token using refresh token
  - `POST /api/v1/auth/logout`: Invalidate tokens

**AdminController**
- Endpoints:
  - `POST /api/v1/admin/students`: Create student
  - `GET /api/v1/admin/students`: List students with pagination and filters
  - `GET /api/v1/admin/students/{id}`: Get student by ID
  - `PUT /api/v1/admin/students/{id}`: Update student
  - `DELETE /api/v1/admin/students/{id}`: Soft delete student
  - `POST /api/v1/admin/students/bulk-upload`: Bulk upload students from Excel
  - `POST /api/v1/admin/faculty`: Create faculty
  - `GET /api/v1/admin/faculty`: List faculty with pagination
  - `PUT /api/v1/admin/faculty/{id}`: Update faculty
  - `DELETE /api/v1/admin/faculty/{id}`: Soft delete faculty
  - `POST /api/v1/admin/courses`: Create course
  - `GET /api/v1/admin/courses`: List courses
  - `POST /api/v1/admin/subjects`: Create subject
  - `GET /api/v1/admin/subjects`: List subjects
  - `POST /api/v1/admin/class-allocations`: Allocate class to faculty
  - `GET /api/v1/admin/dashboard`: Get admin dashboard statistics
  - `GET /api/v1/admin/audit-logs`: Get audit logs with filters

**FacultyController**
- Endpoints:
  - `POST /api/v1/faculty/attendance`: Mark attendance (bulk)
  - `PUT /api/v1/faculty/attendance/{id}`: Edit attendance record
  - `GET /api/v1/faculty/attendance`: Get attendance records with filters
  - `GET /api/v1/faculty/attendance/summary`: Get attendance summary for class
  - `POST /api/v1/faculty/performance`: Add performance record
  - `PUT /api/v1/faculty/performance/{id}`: Update performance record
  - `GET /api/v1/faculty/performance`: Get performance records with filters
  - `GET /api/v1/faculty/reports/attendance`: Generate attendance report
  - `GET /api/v1/faculty/reports/performance`: Generate performance report
  - `GET /api/v1/faculty/reports/low-attendance`: Get low attendance list
  - `GET /api/v1/faculty/dashboard`: Get faculty dashboard statistics

**StudentController**
- Endpoints:
  - `GET /api/v1/student/attendance`: Get own attendance records
  - `GET /api/v1/student/attendance/summary`: Get own attendance summary
  - `GET /api/v1/student/performance`: Get own performance records
  - `GET /api/v1/student/performance/gpa`: Get calculated GPA
  - `GET /api/v1/student/dashboard`: Get student dashboard statistics
  - `GET /api/v1/student/notifications`: Get notifications

#### 3. Service Layer

**AuthService**
- Methods:
  - `register(RegisterRequest request) -> AuthResponse`: Register new user
  - `login(LoginRequest request) -> AuthResponse`: Authenticate and return tokens
  - `refreshToken(String refreshToken) -> AuthResponse`: Generate new access token
  - `logout(String token)`: Invalidate token

**StudentService**
- Methods:
  - `createStudent(StudentDTO dto) -> StudentDTO`: Create student record
  - `updateStudent(String id, StudentDTO dto) -> StudentDTO`: Update student
  - `deleteStudent(String id)`: Soft delete student
  - `getStudentById(String id) -> StudentDTO`: Get student by ID
  - `getAllStudents(Pageable pageable, StudentFilter filter) -> Page<StudentDTO>`: Get paginated students
  - `bulkUploadStudents(MultipartFile file) -> BulkUploadResult`: Process Excel file

**FacultyService**
- Methods:
  - `createFaculty(FacultyDTO dto) -> FacultyDTO`: Create faculty record
  - `updateFaculty(String id, FacultyDTO dto) -> FacultyDTO`: Update faculty
  - `deleteFaculty(String id)`: Soft delete faculty
  - `getFacultyById(String id) -> FacultyDTO`: Get faculty by ID
  - `getAllFaculty(Pageable pageable) -> Page<FacultyDTO>`: Get paginated faculty

**AttendanceService**
- Methods:
  - `markAttendance(BulkAttendanceRequest request) -> List<AttendanceDTO>`: Mark attendance for multiple students
  - `updateAttendance(String id, AttendanceDTO dto) -> AttendanceDTO`: Update attendance record
  - `getAttendanceRecords(AttendanceFilter filter, Pageable pageable) -> Page<AttendanceDTO>`: Get filtered attendance
  - `getAttendanceSummary(String studentId, String subjectId) -> AttendanceSummary`: Calculate attendance percentage
  - `getStudentAttendance(String studentId) -> List<AttendanceDTO>`: Get student's attendance

**PerformanceService**
- Methods:
  - `addPerformance(PerformanceDTO dto) -> PerformanceDTO`: Add performance record
  - `updatePerformance(String id, PerformanceDTO dto) -> PerformanceDTO`: Update performance
  - `getPerformanceRecords(PerformanceFilter filter, Pageable pageable) -> Page<PerformanceDTO>`: Get filtered performance
  - `calculateGPA(String studentId) -> Double`: Calculate student GPA
  - `getStudentPerformance(String studentId) -> List<PerformanceDTO>`: Get student's performance

**NotificationService**
- Methods:
  - `createNotification(NotificationDTO dto) -> NotificationDTO`: Create notification
  - `sendLowAttendanceAlert(String studentId, String subjectId)`: Send low attendance notification
  - `sendPerformanceAlert(String studentId, String subjectId)`: Send performance notification
  - `getUserNotifications(String userId) -> List<NotificationDTO>`: Get user notifications
  - `markAsRead(String notificationId)`: Mark notification as read

**AuditLogService**
- Methods:
  - `logOperation(AuditLogDTO dto)`: Create audit log entry
  - `getAuditLogs(AuditLogFilter filter, Pageable pageable) -> Page<AuditLogDTO>`: Get filtered audit logs

**ReportService**
- Methods:
  - `generateAttendanceReport(ReportRequest request) -> byte[]`: Generate PDF attendance report
  - `generatePerformanceReport(ReportRequest request) -> byte[]`: Generate PDF performance report
  - `exportToCSV(ReportRequest request) -> byte[]`: Export data to CSV

#### 4. Repository Layer

All repositories extend `MongoRepository<Entity, String>` and include custom query methods:

**UserRepository**
- `findByEmail(String email) -> Optional<User>`
- `existsByEmail(String email) -> boolean`

**StudentRepository**
- `findByRollNumber(String rollNumber) -> Optional<Student>`
- `findByCourseAndYearAndSection(String course, Integer year, String section, Pageable pageable) -> Page<Student>`
- `findByDeletedAtIsNull(Pageable pageable) -> Page<Student>`

**FacultyRepository**
- `findByEmployeeId(String employeeId) -> Optional<Faculty>`
- `findByDeletedAtIsNull(Pageable pageable) -> Page<Faculty>`

**AttendanceRepository**
- `findByStudentIdAndSubjectId(String studentId, String subjectId) -> List<Attendance>`
- `findByStudentIdAndSubjectIdAndDateBetween(String studentId, String subjectId, LocalDate start, LocalDate end) -> List<Attendance>`
- `existsByStudentIdAndSubjectIdAndDate(String studentId, String subjectId, LocalDate date) -> boolean`

**PerformanceRepository**
- `findByStudentIdAndSubjectId(String studentId, String subjectId) -> List<Performance>`
- `findByStudentId(String studentId) -> List<Performance>`

**ClassAllocationRepository**
- `findByFacultyId(String facultyId) -> List<ClassAllocation>`
- `findByFacultyIdAndSubjectId(String facultyId, String subjectId) -> Optional<ClassAllocation>`

### Frontend Components

#### 1. Authentication Components

**Login Component**
- Form with email and password fields
- Calls `/api/v1/auth/login` endpoint
- Stores tokens in localStorage
- Redirects to role-appropriate dashboard

**ProtectedRoute Component**
- Wraps routes requiring authentication
- Checks for valid token
- Redirects to login if unauthenticated
- Checks role permissions and redirects if unauthorized

#### 2. Admin Components

**AdminDashboard**
- Displays system-wide statistics (total students, faculty, courses)
- Shows charts for enrollment trends, attendance overview
- Uses Recharts BarChart and PieChart

**StudentManagement**
- Table with student list (pagination, sorting, filtering)
- CRUD operations (Create, Edit, Delete buttons)
- Bulk upload modal with Excel file upload
- Search and filter controls

**FacultyManagement**
- Table with faculty list
- CRUD operations
- Subject assignment modal
- Class allocation modal

**CourseManagement**
- Course list with semester management
- Subject mapping interface

#### 3. Faculty Components

**FacultyDashboard**
- Shows allocated subjects, total students
- Attendance trends chart (LineChart)
- Quick actions for marking attendance

**AttendanceMarking**
- Class selection dropdown (allocated classes only)
- Date picker
- Student list with checkboxes for present/absent
- Bulk mark all present/absent buttons
- Submit button

**PerformanceEntry**
- Class and subject selection
- Student selection
- Assessment type dropdown (Internal, Assignment, Exam)
- Marks input field with validation
- Auto-calculated grade display

**ReportsView**
- Report type selection (Attendance, Performance, Low Attendance)
- Filter controls (date range, subject, class)
- Data table with results
- Export buttons (PDF, CSV)
- Charts showing analytics (BarChart, LineChart)

#### 4. Student Components

**StudentDashboard**
- Overall attendance percentage (large display)
- Current GPA (large display)
- Attendance chart by subject (BarChart)
- Performance trend chart (LineChart)
- Recent notifications list

**AttendanceView**
- Subject-wise attendance table with percentages
- Monthly attendance chart (AreaChart)
- Overall attendance pie chart (Present vs Absent)
- Low attendance warning banner (if < 75%)

**PerformanceView**
- Subject-wise performance table (Internal, Assignment, Exam marks)
- Grade distribution pie chart
- Performance comparison bar chart (across subjects)
- GPA trend line chart (across semesters)

#### 5. Shared Components

**Navbar**
- Logo and app name
- Role-based navigation links
- Notification bell with unread count
- User profile dropdown with logout

**NotificationPanel**
- Dropdown panel showing recent notifications
- Mark as read functionality
- Click to view details

**DataTable**
- Reusable table component with pagination, sorting, filtering
- Props: columns, data, onPageChange, onSort, onFilter

**ChartWrapper**
- Wrapper for Recharts components with responsive container
- Common styling and tooltip configuration

## Data Models

### MongoDB Collections

#### Users Collection
```javascript
{
  _id: ObjectId,
  email: String (unique, indexed),
  password: String (BCrypt hashed),
  role: String (enum: ROLE_ADMIN, ROLE_FACULTY, ROLE_STUDENT),
  isActive: Boolean,
  lastLogin: Date,
  createdAt: Date,
  updatedAt: Date,
  deletedAt: Date (null if not deleted)
}
```

**Indexes:**
- `{ email: 1 }` (unique)
- `{ role: 1 }`
- `{ deletedAt: 1 }`

#### Students Collection
```javascript
{
  _id: ObjectId,
  userId: ObjectId (reference to Users),
  rollNumber: String (unique, indexed),
  firstName: String,
  lastName: String,
  email: String,
  phone: String,
  dateOfBirth: Date,
  courseId: ObjectId (reference to Courses),
  year: Number (1-4),
  section: String,
  profileImage: String (file path),
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date,
  deletedAt: Date
}
```

**Indexes:**
- `{ rollNumber: 1 }` (unique)
- `{ userId: 1 }`
- `{ courseId: 1, year: 1, section: 1 }` (compound)
- `{ deletedAt: 1 }`

#### Faculty Collection
```javascript
{
  _id: ObjectId,
  userId: ObjectId (reference to Users),
  employeeId: String (unique, indexed),
  firstName: String,
  lastName: String,
  email: String,
  phone: String,
  department: String,
  designation: String,
  profileImage: String,
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date,
  deletedAt: Date
}
```

**Indexes:**
- `{ employeeId: 1 }` (unique)
- `{ userId: 1 }`
- `{ deletedAt: 1 }`

#### Courses Collection
```javascript
{
  _id: ObjectId,
  code: String (unique, indexed),
  name: String,
  description: String,
  duration: Number (years),
  semesters: [
    {
      semesterNumber: Number,
      subjectIds: [ObjectId] (references to Subjects)
    }
  ],
  createdAt: Date,
  updatedAt: Date
}
```

**Indexes:**
- `{ code: 1 }` (unique)

#### Subjects Collection
```javascript
{
  _id: ObjectId,
  code: String (unique, indexed),
  name: String,
  credits: Number,
  description: String,
  createdAt: Date,
  updatedAt: Date
}
```

**Indexes:**
- `{ code: 1 }` (unique)

#### ClassAllocation Collection
```javascript
{
  _id: ObjectId,
  facultyId: ObjectId (reference to Faculty),
  subjectId: ObjectId (reference to Subjects),
  courseId: ObjectId (reference to Courses),
  year: Number,
  section: String,
  semester: Number,
  academicYear: String,
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

**Indexes:**
- `{ facultyId: 1, subjectId: 1 }` (compound)
- `{ courseId: 1, year: 1, section: 1, semester: 1 }` (compound)

#### Attendance Collection
```javascript
{
  _id: ObjectId,
  studentId: ObjectId (reference to Students),
  subjectId: ObjectId (reference to Subjects),
  facultyId: ObjectId (reference to Faculty),
  date: Date,
  status: String (enum: PRESENT, ABSENT),
  markedBy: ObjectId (reference to Users),
  remarks: String,
  createdAt: Date,
  updatedAt: Date
}
```

**Indexes:**
- `{ studentId: 1, subjectId: 1, date: 1 }` (compound, unique)
- `{ studentId: 1 }`
- `{ subjectId: 1, date: 1 }` (compound)
- `{ date: 1 }`

#### Performance Collection
```javascript
{
  _id: ObjectId,
  studentId: ObjectId (reference to Students),
  subjectId: ObjectId (reference to Subjects),
  facultyId: ObjectId (reference to Faculty),
  assessmentType: String (enum: INTERNAL, ASSIGNMENT, EXAM),
  assessmentName: String,
  maxMarks: Number,
  obtainedMarks: Number,
  grade: String (auto-calculated),
  date: Date,
  remarks: String,
  createdAt: Date,
  updatedAt: Date
}
```

**Indexes:**
- `{ studentId: 1, subjectId: 1 }` (compound)
- `{ studentId: 1 }`
- `{ subjectId: 1 }`

#### Notifications Collection
```javascript
{
  _id: ObjectId,
  userId: ObjectId (reference to Users),
  type: String (enum: LOW_ATTENDANCE, PERFORMANCE_UPDATE, ANNOUNCEMENT, SYSTEM),
  title: String,
  message: String,
  isRead: Boolean,
  metadata: Object (additional context),
  createdAt: Date
}
```

**Indexes:**
- `{ userId: 1, isRead: 1 }` (compound)
- `{ createdAt: 1 }`

#### Announcements Collection
```javascript
{
  _id: ObjectId,
  title: String,
  content: String,
  targetRole: String (enum: ALL, ROLE_ADMIN, ROLE_FACULTY, ROLE_STUDENT),
  createdBy: ObjectId (reference to Users),
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

**Indexes:**
- `{ targetRole: 1, isActive: 1 }` (compound)
- `{ createdAt: 1 }`

#### AuditLogs Collection
```javascript
{
  _id: ObjectId,
  userId: ObjectId (reference to Users),
  action: String (enum: CREATE, UPDATE, DELETE, LOGIN, LOGOUT),
  entityType: String (e.g., STUDENT, FACULTY, ATTENDANCE),
  entityId: ObjectId,
  oldValue: Object,
  newValue: Object,
  ipAddress: String,
  userAgent: String,
  timestamp: Date
}
```

**Indexes:**
- `{ userId: 1, timestamp: 1 }` (compound)
- `{ entityType: 1, entityId: 1 }` (compound)
- `{ timestamp: 1 }`

### DTOs (Data Transfer Objects)

#### AuthResponse
```java
{
  accessToken: String,
  refreshToken: String,
  tokenType: String ("Bearer"),
  expiresIn: Long (seconds),
  user: UserDTO
}
```

#### UserDTO
```java
{
  id: String,
  email: String,
  role: String,
  firstName: String,
  lastName: String,
  isActive: Boolean
}
```

#### StudentDTO
```java
{
  id: String,
  rollNumber: String,
  firstName: String,
  lastName: String,
  email: String,
  phone: String,
  dateOfBirth: LocalDate,
  course: CourseDTO,
  year: Integer,
  section: String,
  profileImage: String,
  isActive: Boolean
}
```

#### AttendanceDTO
```java
{
  id: String,
  student: StudentDTO,
  subject: SubjectDTO,
  faculty: FacultyDTO,
  date: LocalDate,
  status: String,
  remarks: String
}
```

#### PerformanceDTO
```java
{
  id: String,
  student: StudentDTO,
  subject: SubjectDTO,
  assessmentType: String,
  assessmentName: String,
  maxMarks: Double,
  obtainedMarks: Double,
  grade: String,
  date: LocalDate,
  remarks: String
}
```

#### AttendanceSummary
```java
{
  studentId: String,
  subjectId: String,
  totalClasses: Integer,
  attendedClasses: Integer,
  attendancePercentage: Double,
  status: String (GOOD, WARNING, CRITICAL)
}
```

