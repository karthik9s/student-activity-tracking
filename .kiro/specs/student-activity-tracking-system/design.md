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


## Correctness Properties

A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.

### Property Reflection

After analyzing all acceptance criteria, several patterns emerged that allow us to consolidate redundant properties:

1. **CRUD Operations**: Create, update, and soft delete operations follow similar patterns across Students, Faculty, Courses, and Subjects. We can consolidate these into generic entity management properties.

2. **Audit Logging**: All admin and faculty operations require audit logging. Rather than separate properties for each operation type, we can have a single comprehensive audit logging property.

3. **Authorization**: Role-based access control applies uniformly across all protected endpoints. We can consolidate authorization checks into fewer comprehensive properties.

4. **Data Isolation**: Students can only view their own data (attendance, performance). This is a single property that applies to all student data access.

5. **Soft Delete Filtering**: All queries should exclude soft-deleted records. This is a single property across all entity types.

6. **Validation**: Input validation, referential integrity, and format validation follow similar patterns and can be consolidated.

7. **Calculation Properties**: Attendance percentage, GPA calculation, and grade calculation are distinct mathematical properties that need separate validation.

### Authentication Properties

**Property 1: Valid Credentials Generate Tokens**
*For any* valid user credentials (email and password), when submitted to the login endpoint, the system should return both a JWT access token and a refresh token.
**Validates: Requirements 1.1**

**Property 2: Invalid Credentials Rejected**
*For any* invalid credentials (wrong password, non-existent email, or malformed input), the authentication attempt should be rejected with an appropriate error message.
**Validates: Requirements 1.2**

**Property 3: Password Hashing Round Trip**
*For any* password string, when hashed using BCrypt and then verified against the original password, the verification should succeed.
**Validates: Requirements 1.3**

**Property 4: Token Refresh Extends Session**
*For any* valid refresh token, when used to request a new access token, the system should generate a new valid access token
 with the same user context.
**Validates: Requirements 1.4**

**Property 5: Token Invalidation on Logout**
*For any* valid token, after logout is called with that token, subsequent requests using that token should be rejected.
**Validates: Requirements 1.5**

**Property 6: Token Contains Role Information**
*For any* authenticated user, the JWT token payload should contain the user's role information that matches their assigned role in the database.
**Validates: Requirements 1.6**

**Property 7: Tampered Tokens Rejected**
*For any* JWT token with modified payload or signature, the system should reject the token and deny access.
**Validates: Requirements 1.7**

### Authorization Properties

**Property 8: Role-Based Endpoint Access**
*For any* protected endpoint with a required role, when a user with insufficient privileges attempts access, the system should return a 403 Forbidden response.
**Validates: Requirements 2.3**

**Property 9: Role Verification on Protected Operations**
*For any* request to a protected endpoint, the system should verify the user's role matches the required role before processing the request.
**Validates: Requirements 2.2**

**Property 10: Role Hierarchy Enforcement**
*For any* operation, ROLE_ADMIN should have access to all endpoints, ROLE_FACULTY should have access to teaching-related endpoints, and ROLE_STUDENT should have access only to view endpoints for their own data.
**Validates: Requirements 2.5**

### Entity Management Properties (CRUD)

**Property 11: Valid Entity Creation**
*For any* valid entity data (Student, Faculty, Course, Subject) with all required fields, when submitted by an authorized user, the system should create the entity and return the created record with a generated ID.
**Validates: Requirements 3.1, 4.1, 5.1, 5.3**

**Property 12: Entity Update Preserves Identity**
*For any* existing entity, when updated with valid data, the entity ID should remain unchanged and only specified fields should be modified.
**Validates: Requirements 3.2, 4.2**

**Property 13: Soft Delete Marks Entity**
*For any* entity deletion request, the system should set a deletedAt timestamp instead of physically removing the record.
**Validates: Requirements 3.3, 4.3**

**Property 14: Soft Deleted Entities Excluded from Queries**
*For any* query operation, entities with a non-null deletedAt field should be excluded from results unless explicitly requested.
**Validates: Requirements 3.4**

### Data Validation Properties

**Property 15: Required Field Validation**
*For any* API request with missing required fields, the system should return a 400 Bad Request response with detailed validation errors.
**Validates: Requirements 14.2**

**Property 16: Referential Integrity Validation**
*For any* operation that references another entity (e.g., assigning a course to a student), the system should validate the referenced entity exists before proceeding.
**Validates: Requirements 14.5**

**Property 17: Email Format Validation**
*For any* email input, the system should validate it matches a valid email pattern before accepting it.
**Validates: Requirements 14.6**

**Property 18: Unique Constraint Enforcement**
*For any* field with a uniqueness constraint (email, roll number, employee ID, course code, subject code), the system should reject duplicate values.
**Validates: Requirements 14.4**

### Attendance Management Properties

**Property 19: Faculty Allocation Validation for Attendance**
*For any* attendance marking operation, the system should verify the faculty member is allocated to the specified class before allowing the operation.
**Validates: Requirements 6.1, 6.6**

**Property 20: No Duplicate Attendance Records**
*For any* combination of student, subject, and date, the system should prevent creation of duplicate attendance records.
**Validates: Requirements 6.7**

**Property 21: Attendance Percentage Calculation**
*For any* student and subject combination, the attendance percentage should equal (attended classes / total classes) × 100, rounded to two decimal places.
**Validates: Requirements 6.5**

**Property 22: Attendance Edit Logging**
*For any* attendance record modification, the system should create an audit log entry with old and new values.
**Validates: Requirements 6.3, 13.2**

### Performance Management Properties

**Property 23: Marks Range Validation**
*For any* performance record, the obtained marks should be greater than or equal to 0 and less than or equal to the maximum marks.
**Validates: Requirements 7.1**

**Property 24: Grade Auto-Calculation**
*For any* performance record with obtained marks and maximum marks, the system should automatically calculate and assign a grade based on the percentage and predefined grade boundaries.
**Validates: Requirements 7.3**

**Property 25: GPA Calculation Accuracy**
*For any* student with multiple graded performance records, the GPA should equal the weighted average of grade points based on subject credits.
**Validates: Requirements 7.7**

**Property 26: Faculty Subject Authorization for Performance**
*For any* performance record creation or update, the system should verify the faculty member is allocated to the specified subject.
**Validates: Requirements 7.5**

**Property 27: Performance Update Triggers Grade Recalculation**
*For any* performance record update that changes marks, the system should recalculate the grade and update it accordingly.
**Validates: Requirements 7.6**

### Data Isolation Properties

**Property 28: Student Data Isolation**
*For any* student user accessing attendance or performance data, the system should return only records belonging to that specific student.
**Validates: Requirements 9.1, 10.1**

**Property 29: Faculty Class Allocation Filtering**
*For any* faculty user accessing student data, the system should return only data for students in classes allocated to that faculty member.
**Validates: Requirements 6.1**

### Notification Properties

**Property 30: Low Attendance Alert Trigger**
*For any* student whose attendance percentage falls below 75% for any subject, the system should create both an in-app notification and send an email notification.
**Validates: Requirements 12.1, 12.2**

**Property 31: Performance Update Notification**
*For any* new performance record creation, the system should create an in-app notification for the affected student.
**Validates: Requirements 12.3**

**Property 32: Notification Read Status Update**
*For any* notification viewed by a user, the system should mark it as read and update the unread count.
**Validates: Requirements 12.6**

### Audit Logging Properties

**Property 33: Admin Operation Logging**
*For any* CRUD operation performed by an admin user, the system should create an audit log entry with timestamp, user ID, operation type, and affected entity.
**Validates: Requirements 13.1**

**Property 34: Audit Log Immutability**
*For any* audit log entry, once created, it should never be modified or deleted.
**Validates: Requirements 13.6**

### Search and Filtering Properties

**Property 35: Pagination Consistency**
*For any* paginated query with page size N, each page should contain at most N records, and the total count should match the sum of all pages.
**Validates: Requirements 15.4, 15.6**

**Property 36: Filter Application Correctness**
*For any* query with filters applied, all returned records should match all specified filter criteria.
**Validates: Requirements 15.2, 15.3**

### Security Properties

**Property 37: Input Sanitization**
*For any* user input, the system should sanitize it to prevent XSS attacks before processing or storage.
**Validates: Requirements 18.1**

**Property 38: Token Expiration Enforcement**
*For any* access token older than 1 hour, the system should reject it and require token refresh.
**Validates: Requirements 18.6**

**Property 39: Brute Force Protection**
*For any* user account with more than 5 failed login attempts within 15 minutes, the system should temporarily lock the account.
**Validates: Requirements 18.4**

### Report Generation Properties

**Property 40: Report Data Accuracy**
*For any* generated report (attendance or performance), the data should exactly match the underlying database records for the specified filters and date range.
**Validates: Requirements 8.1, 17.1**

**Property 41: Export Format Validity**
*For any* report export operation, the generated file (PDF or CSV) should be valid and openable in standard applications.
**Validates: Requirements 17.1, 17.2**

### File Upload Properties

**Property 42: Excel Validation Before Import**
*For any* Excel file upload for bulk import, if any row contains validation errors, the system should reject the entire import and return a detailed error report without creating any records.
**Validates: Requirements 16.3**

**Property 43: Image File Validation**
*For any* profile image upload, the system should validate the file is an image format (jpg, png, gif) and size is less than 5MB before accepting it.
**Validates: Requirements 16.5, 16.6**

### Class Allocation Properties

**Property 44: Faculty Allocation Uniqueness**
*For any* class allocation, the combination of faculty, subject, course, year, section, and semester should be unique within an academic year.
**Validates: Requirements 4.5**

**Property 45: Active Allocation Protection**
*For any* faculty member with active class allocations, the system should prevent deletion of the faculty record.
**Validates: Requirements 4.6**

### Course and Subject Integrity Properties

**Property 46: Course Deletion Protection**
*For any* course with enrolled students, the system should prevent deletion of the course.
**Validates: Requirements 5.6**

**Property 47: Subject Deletion Protection**
*For any* subject with attendance or performance records, the system should prevent deletion of the subject.
**Validates: Requirements 5.7**

## Implementation Plan

### Phase 1: Foundation Setup (Week 1-2)

#### Backend Setup
1. Initialize Spring Boot project with dependencies:
   - Spring Web
   - Spring Security
   - Spring Data MongoDB
   - Spring Validation
   - JWT library (io.jsonwebtoken)
   - Lombok
   - MapStruct (for DTO mapping)

2. Configure MongoDB connection in application.properties

3. Set up project structure:
   ```
   src/main/java/com/college/activitytracker/
   ├── config/
   │   ├── SecurityConfig.java
   │   ├── MongoConfig.java
   │   └── CorsConfig.java
   ├── security/
   │   ├── JwtTokenProvider.java
   │   ├── JwtAuthenticationFilter.java
   │   └── CustomUserDetailsService.java
   ├── controller/
   ├── service/
   ├── repository/
   ├── model/
   ├── dto/
   ├── exception/
   │   ├── GlobalExceptionHandler.java
   │   └── custom exceptions
   └── util/
   ```

4. Create MongoDB collections with indexes

#### Frontend Setup
1. Initialize React project with Create React App or Vite

2. Install dependencies:
   - react-router-dom
   - axios
   - recharts
   - react-toastify
   - formik (for forms)
   - yup (for validation)

3. Set up project structure:
   ```
   src/
   ├── api/
   │   ├── axios.config.js
   │   └── endpoints/
   ├── components/
   │   ├── common/
   │   ├── charts/
   │   └── layout/
   ├── pages/
   │   ├── admin/
   │   ├── faculty/
   │   ├── student/
   │   └── auth/
   ├── context/
   │   └── AuthContext.js
   ├── routes/
   │   ├── ProtectedRoute.js
   │   └── AppRoutes.js
   ├── utils/
   └── App.js
   ```

### Phase 2: Authentication & Authorization (Week 2-3)

#### Backend Tasks
1. Implement User model and repository
2. Implement JWT token generation and validation
3. Implement authentication endpoints (login, register, refresh, logout)
4. Implement JWT filter and security configuration
5. Implement role-based method security
6. Write unit tests for authentication flow

#### Frontend Tasks
1. Create Login component
2. Create AuthContext for global auth state
3. Implement Axios interceptors for token attachment
4. Create ProtectedRoute component
5. Implement token refresh logic
6. Create Navbar with user profile and logout

### Phase 3: Admin Module (Week 3-5)

#### Backend Tasks
1. Implement Student, Faculty, Course, Subject models
2. Implement repositories with custom queries
3. Implement admin service layer with CRUD operations
4. Implement admin controllers with validation
5. Implement bulk upload functionality for students
6. Implement audit logging for admin operations
7. Write unit and integration tests

#### Frontend Tasks
1. Create AdminDashboard with statistics
2. Create StudentManagement component with CRUD
3. Create FacultyManagement component with CRUD
4. Create CourseManagement component
5. Create SubjectManagement component
6. Implement bulk upload modal with Excel file handling
7. Implement search, filter, and pagination
8. Create data tables with sorting

### Phase 4: Faculty Module (Week 5-7)

#### Backend Tasks
1. Implement ClassAllocation model and repository
2. Implement Attendance model and repository
3. Implement Performance model and repository
4. Implement attendance service with bulk marking
5. Implement performance service with grade calculation
6. Implement report service with PDF generation
7. Implement faculty authorization checks
8. Write unit and integration tests

#### Frontend Tasks
1. Create FacultyDashboard
2. Create AttendanceMarking component with bulk operations
3. Create PerformanceEntry component with grade display
4. Create ReportsView with filters and export
5. Implement charts for attendance and performance analytics
6. Create class allocation view

### Phase 5: Student Module (Week 7-8)

#### Backend Tasks
1. Implement student-specific service methods
2. Implement GPA calculation logic
3. Implement attendance summary calculation
4. Implement notification service
5. Write unit and integration tests

#### Frontend Tasks
1. Create StudentDashboard with overall statistics
2. Create AttendanceView with charts (BarChart, PieChart, AreaChart)
3. Create PerformanceView with charts (LineChart, BarChart, PieChart)
4. Implement low attendance warning banner
5. Create NotificationPanel component
6. Implement all Recharts visualizations

### Phase 6: Notifications & Audit (Week 8-9)

#### Backend Tasks
1. Implement Notification model and repository
2. Implement notification service with email integration
3. Implement automatic low attendance alerts
4. Implement performance update notifications
5. Implement AuditLog model and repository
6. Implement audit log service with filtering
7. Write unit tests

#### Frontend Tasks
1. Create notification bell with unread count
2. Create notification dropdown panel
3. Implement mark as read functionality
4. Create admin audit log viewer with filters

### Phase 7: Advanced Features (Week 9-10)

#### Backend Tasks
1. Implement caching for dashboard statistics
2. Implement rate limiting
3. Implement file upload to cloud storage (optional)
4. Optimize database queries with proper indexing
5. Implement API documentation with Swagger
6. Performance testing and optimization

#### Frontend Tasks
1. Implement lazy loading for routes
2. Implement error boundaries
3. Add loading spinners and skeletons
4. Implement dark mode toggle (optional)
5. Optimize bundle size
6. Add accessibility features

### Phase 8: Testing & Deployment (Week 10-12)

#### Backend Tasks
1. Complete unit test coverage (target 80%+)
2. Complete integration tests for all endpoints
3. Perform security testing
4. Load testing and performance optimization
5. Create Docker configuration
6. Set up CI/CD pipeline

#### Frontend Tasks
1. Complete component tests with Jest and React Testing Library
2. End-to-end testing with Cypress (optional)
3. Cross-browser testing
4. Mobile responsiveness testing
5. Create production build
6. Deploy to hosting platform (Vercel/Netlify)

#### Deployment
1. Set up MongoDB Atlas cluster
2. Deploy backend to AWS EC2 / Render / Railway
3. Configure environment variables
4. Set up domain and SSL certificates
5. Configure CORS for production
6. Set up monitoring and logging
7. Create deployment documentation

## Testing Strategy

### Backend Testing

#### Unit Tests (JUnit 5 + Mockito)
- Test all service layer methods with mocked repositories
- Test JWT token generation and validation
- Test DTO mapping logic
- Test calculation methods (GPA, attendance percentage, grade)
- Test validation logic
- Target: 80%+ code coverage

Example test structure:
```java
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;
    
    @InjectMocks
    private AttendanceService attendanceService;
    
    @Test
    void testCalculateAttendancePercentage() {
        // Arrange
        List<Attendance> records = createMockAttendanceRecords();
        when(attendanceRepository.findByStudentIdAndSubjectId(any(), any()))
            .thenReturn(records);
        
        // Act
        AttendanceSummary summary = attendanceService
            .getAttendanceSummary("student1", "subject1");
        
        // Assert
        assertEquals(75.0, summary.getAttendancePercentage());
    }
}
```

#### Integration Tests (Spring Boot Test)
- Test all controller endpoints with MockMvc
- Test authentication and authorization flows
- Test database operations with embedded MongoDB
- Test file upload functionality
- Test error handling and validation

Example test structure:
```java
@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateStudent() throws Exception {
        String studentJson = "{ \"rollNumber\": \"BT2023001\", ... }";
        
        mockMvc.perform(post("/api/v1/admin/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(studentJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.rollNumber").value("BT2023001"));
    }
}
```

#### Property-Based Tests (jqwik)
- Test authentication properties (Properties 1-7)
- Test authorization properties (Properties 8-10)
- Test calculation properties (Properties 21, 24, 25)
- Test validation properties (Properties 15-18)
- Test data isolation properties (Properties 28-29)

Example property test:
```java
class AttendancePropertyTest {
    @Property
    void attendancePercentageIsAlwaysBetween0And100(
        @ForAll @IntRange(min = 0, max = 100) int attended,
        @ForAll @IntRange(min = 1, max = 100) int total
    ) {
        Assume.that(attended <= total);
        
        double percentage = (attended * 100.0) / total;
        
        assertThat(percentage).isBetween(0.0, 100.0);
    }
}
```

### Frontend Testing

#### Component Tests (Jest + React Testing Library)
- Test all components render correctly
- Test user interactions (button clicks, form submissions)
- Test conditional rendering based on props/state
- Test protected routes redirect correctly
- Test form validation

Example test:
```javascript
describe('Login Component', () => {
  test('submits form with valid credentials', async () => {
    const mockLogin = jest.fn();
    render(<Login onLogin={mockLogin} />);
    
    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: 'test@example.com' }
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'password123' }
    });
    
    fireEvent.click(screen.getByRole('button', { name: /login/i }));
    
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123'
      });
    });
  });
});
```

#### API Integration Tests
- Test Axios interceptors attach tokens correctly
- Test API error handling
- Test token refresh flow
- Mock API responses for testing

### API Testing (Postman)
- Create Postman collection for all endpoints
- Test happy paths and error cases
- Test authentication and authorization
- Export collection for CI/CD integration

## Performance Optimization

### Database Optimization
1. **Indexing Strategy**:
   - Single field indexes on frequently queried fields (email, rollNumber, employeeId)
   - Compound indexes for multi-field queries (studentId + subjectId + date)
   - Text indexes for full-text search

2. **Query Optimization**:
   - Use projection to fetch only required fields
   - Implement pagination for large result sets
   - Use aggregation pipeline for complex queries
   - Avoid N+1 query problems with proper data modeling

3. **Connection Pooling**:
   - Configure MongoDB connection pool size based on load
   - Monitor connection usage and adjust as needed

### Backend Optimization
1. **Caching**:
   - Cache dashboard statistics (5-minute TTL)
   - Cache user profile data
   - Use Redis for distributed caching (future enhancement)

2. **Async Processing**:
   - Process bulk uploads asynchronously
   - Generate reports asynchronously for large datasets
   - Send email notifications asynchronously

3. **DTO Optimization**:
   - Use MapStruct for efficient DTO mapping
   - Avoid unnecessary data transfer
   - Implement lazy loading for relationships

### Frontend Optimization
1. **Code Splitting**:
   - Lazy load route components
   - Split vendor bundles
   - Use dynamic imports for large libraries

2. **Asset Optimization**:
   - Compress images
   - Minify CSS and JavaScript
   - Use CDN for static assets

3. **Rendering Optimization**:
   - Use React.memo for expensive components
   - Implement virtualization for large lists
   - Debounce search inputs
   - Optimize Recharts rendering with data sampling for large datasets

## Security Implementation

### Authentication Security
1. **Password Security**:
   - Use BCrypt with salt rounds = 12
   - Enforce password complexity requirements
   - Implement password reset functionality with secure tokens

2. **Token Security**:
   - Use strong secret keys (256-bit minimum)
   - Implement token rotation
   - Store refresh tokens securely (HTTP-only cookies preferred)
   - Implement token blacklisting for logout

3. **Session Security**:
   - Implement brute force protection (5 attempts, 15-minute lockout)
   - Log all authentication attempts
   - Implement account lockout after repeated failures

### API Security
1. **Input Validation**:
   - Validate all inputs with @Valid annotations
   - Sanitize inputs to prevent XSS
   - Implement request size limits

2. **Authorization**:
   - Implement method-level security with @PreAuthorize
   - Validate user permissions at service layer
   - Implement resource-level authorization (users can only access their own data)

3. **CORS Configuration**:
   - Whitelist allowed origins
   - Restrict allowed methods and headers
   - Configure credentials handling

4. **Rate Limiting**:
   - Implement API rate limiting (100 requests per minute per user)
   - Implement stricter limits for authentication endpoints
   - Return 429 Too Many Requests when limit exceeded

5. **Security Headers**:
   - X-Frame-Options: DENY
   - X-Content-Type-Options: nosniff
   - X-XSS-Protection: 1; mode=block
   - Strict-Transport-Security: max-age=31536000

### Data Security
1. **Encryption**:
   - Use HTTPS for all communications
   - Encrypt sensitive data at rest (future enhancement)
   - Use secure random generators for tokens

2. **Audit Logging**:
   - Log all security-relevant events
   - Include IP address and user agent
   - Implement log retention policy
   - Protect logs from tampering

## Deployment Architecture

### Development Environment
```
Local Machine
├── React Dev Server (localhost:3000)
├── Spring Boot (localhost:8080)
└── MongoDB (localhost:27017)
```

### Production Environment
```
┌─────────────────────────────────────────────────────────┐
│                    Cloudflare / CDN                      │
│                  (SSL/TLS Termination)                   │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────┐
│                  Vercel / Netlify                        │
│              (React Frontend - Static)                   │
└─────────────────────────────────────────────────────────┘
                         │
                         │ HTTPS REST API
                         │
┌────────────────────────┴────────────────────────────────┐
│                   Nginx Reverse Proxy                    │
│              (Load Balancing, SSL, Caching)              │
└────────────────────────┬────────────────────────────────┘
                         │
         ┌───────────────┴───────────────┐
         │                               │
┌────────┴────────┐            ┌────────┴────────┐
│  Spring Boot    │            │  Spring Boot    │
│   Instance 1    │            │   Instance 2    │
│  (AWS EC2 /     │            │  (AWS EC2 /     │
│   Render)       │            │   Render)       │
└────────┬────────┘            └────────┬────────┘
         │                               │
         └───────────────┬───────────────┘
                         │
┌────────────────────────┴────────────────────────────────┐
│                  MongoDB Atlas                           │
│              (Replica Set - 3 nodes)                     │
│         Primary + Secondary + Secondary                  │
└─────────────────────────────────────────────────────────┘
```

### Deployment Steps

#### 1. MongoDB Atlas Setup
- Create MongoDB Atlas account
- Create cluster (M10 or higher for production)
- Configure network access (whitelist application IPs)
- Create database user with appropriate permissions
- Enable backup and monitoring

#### 2. Backend Deployment (AWS EC2 / Render)
- Create application instance
- Install Java 17+
- Build Spring Boot JAR: `mvn clean package`
- Configure environment variables:
  ```
  MONGODB_URI=mongodb+srv://...
  JWT_SECRET=<strong-secret-key>
  JWT_EXPIRATION=3600000
  CORS_ALLOWED_ORIGINS=https://yourdomain.com
  ```
- Run application: `java -jar app.jar`
- Configure reverse proxy (Nginx)
- Set up SSL certificates (Let's Encrypt)
- Configure auto-restart on failure

#### 3. Frontend Deployment (Vercel / Netlify)
- Build React app: `npm run build`
- Configure environment variables:
  ```
  REACT_APP_API_URL=https://api.yourdomain.com
  ```
- Deploy build folder
- Configure custom domain
- Enable HTTPS
- Configure redirects for SPA routing

#### 4. CI/CD Pipeline (GitHub Actions)
```yaml
name: Deploy

on:
  push:
    branches: [ main ]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean package
      - name: Deploy to server
        # Deploy steps

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Node
        uses: actions/setup-node@v2
        with:
          node-version: '18'
      - name: Install dependencies
        run: npm install
      - name: Build
        run: npm run build
      - name: Deploy to Vercel
        # Deploy steps
```

## Monitoring and Maintenance

### Application Monitoring
1. **Logging**:
   - Use SLF4J with Logback
   - Configure log levels per environment
   - Implement structured logging (JSON format)
   - Use log aggregation service (ELK stack or cloud service)

2. **Metrics**:
   - Monitor API response times
   - Track error rates
   - Monitor database query performance
   - Track user activity metrics

3. **Alerts**:
   - Set up alerts for high error rates
   - Alert on slow API responses
   - Alert on database connection issues
   - Alert on high memory/CPU usage

### Database Monitoring
1. **MongoDB Atlas Monitoring**:
   - Monitor query performance
   - Track slow queries
   - Monitor connection pool usage
   - Set up alerts for high resource usage

2. **Backup Strategy**:
   - Enable automated daily backups
   - Test backup restoration regularly
   - Implement point-in-time recovery
   - Store backups in multiple regions

### Security Monitoring
1. **Audit Log Review**:
   - Regularly review audit logs for suspicious activity
   - Monitor failed login attempts
   - Track privilege escalation attempts

2. **Vulnerability Scanning**:
   - Regularly update dependencies
   - Scan for known vulnerabilities
   - Perform security audits

## Future Enhancements

### Phase 2 Features (Post-MVP)
1. **Mobile Application**:
   - React Native mobile app for iOS and Android
   - Push notifications for mobile devices

2. **Advanced Analytics**:
   - Predictive analytics for student performance
   - Machine learning models for dropout prediction
   - Comparative analytics across batches

3. **Communication Module**:
   - In-app messaging between faculty and students
   - Announcement broadcast system
   - Email integration for notifications

4. **Timetable Management**:
   - Class schedule management
   - Room allocation
   - Conflict detection

5. **Exam Management**:
   - Exam scheduling
   - Hall ticket generation
   - Result processing

6. **Library Integration**:
   - Book issue/return tracking
   - Fine management

### Scalability Enhancements
1. **Microservices Architecture**:
   - Split into separate services:
     - Auth Service
     - Student Service
     - Faculty Service
     - Attendance Service
     - Performance Service
     - Notification Service
     - Report Service

2. **Caching Layer**:
   - Implement Redis for distributed caching
   - Cache frequently accessed data
   - Implement cache invalidation strategies

3. **Message Queue**:
   - Implement RabbitMQ or Kafka for async processing
   - Process bulk operations asynchronously
   - Implement event-driven architecture

4. **API Gateway**:
   - Implement API gateway for routing
   - Centralized authentication
   - Rate limiting at gateway level

## Conclusion

This design document provides a comprehensive blueprint for building a production-ready Student Activity & Academic Tracking System. The architecture emphasizes security, scalability, and maintainability while providing a clear implementation path through phased development.

Key strengths of this design:
- **Security-first approach** with JWT authentication and RBAC
- **Scalable architecture** ready for horizontal scaling
- **Comprehensive testing strategy** with property-based testing
- **Clear separation of concerns** with layered architecture
- **Performance optimization** through caching and indexing
- **Production-ready deployment** with CI/CD pipeline

The implementation plan provides a realistic 12-week timeline with clear milestones and deliverables. The system is designed to be maintainable, testable, and extensible for future enhancements.
