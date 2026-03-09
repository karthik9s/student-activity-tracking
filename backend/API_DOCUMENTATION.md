# API Documentation

## Overview
REST API documentation for the Student Activity & Academic Tracking System.

**Base URL**: `http://localhost:8080/api`

**Authentication**: JWT Bearer Token (except login/register endpoints)

## Swagger/OpenAPI

### Access Swagger UI
Once the application is running, access interactive API documentation at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Enable Swagger (if not already configured)

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

Add to `application.properties`:
```properties
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

## Authentication Endpoints

### POST /auth/login
Authenticate user and receive JWT tokens.

**Request Body**:
```json
{
  "email": "admin@college.edu",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "user-id",
    "email": "admin@college.edu",
    "role": "ROLE_ADMIN"
  }
}
```

### POST /auth/refresh
Refresh access token using refresh token.

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

### POST /auth/logout
Invalidate current session.

**Headers**: `Authorization: Bearer {accessToken}`

## Admin Endpoints

### Student Management

#### GET /admin/students
Get paginated list of students.

**Query Parameters**:
- `page` (int, default: 0)
- `size` (int, default: 10)
- `search` (string, optional)
- `courseId` (string, optional)
- `year` (int, optional)
- `section` (string, optional)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "student-id",
      "rollNumber": "2024001",
      "name": "John Doe",
      "email": "john@college.edu",
      "phone": "1234567890",
      "courseId": "course-id",
      "year": 1,
      "section": "A",
      "isActive": true
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10
}
```

#### POST /admin/students
Create new student.

**Request Body**:
```json
{
  "rollNumber": "2024001",
  "name": "John Doe",
  "email": "john@college.edu",
  "phone": "1234567890",
  "courseId": "course-id",
  "year": 1,
  "section": "A"
}
```

#### PUT /admin/students/{id}
Update existing student.

#### DELETE /admin/students/{id}
Soft delete student.

### Faculty Management

#### GET /admin/faculty
Get paginated list of faculty.

#### POST /admin/faculty
Create new faculty member.

**Request Body**:
```json
{
  "employeeId": "FAC001",
  "name": "Dr. Smith",
  "email": "smith@college.edu",
  "phone": "9876543210",
  "department": "Computer Science"
}
```

### Course Management

#### GET /admin/courses
Get all courses.

#### POST /admin/courses
Create new course.

**Request Body**:
```json
{
  "code": "CS101",
  "name": "Computer Science",
  "duration": 4,
  "semesters": [1, 2, 3, 4, 5, 6, 7, 8]
}
```

### Subject Management

#### GET /admin/subjects
Get all subjects.

#### POST /admin/subjects
Create new subject.

**Request Body**:
```json
{
  "code": "CS101",
  "name": "Data Structures",
  "courseId": "course-id",
  "semester": 3,
  "credits": 4
}
```

## Faculty Endpoints

### Attendance Management

#### POST /faculty/attendance
Mark attendance for students.

**Request Body**:
```json
{
  "studentId": "student-id",
  "subjectId": "subject-id",
  "date": "2024-01-15",
  "present": true
}
```

#### GET /faculty/attendance/subject/{subjectId}
Get attendance records for a subject.

**Query Parameters**:
- `startDate` (date, optional)
- `endDate` (date, optional)

### Performance Management

#### POST /faculty/performance
Create performance record.

**Request Body**:
```json
{
  "studentId": "student-id",
  "subjectId": "subject-id",
  "assessmentType": "MIDTERM",
  "obtainedMarks": 85,
  "maxMarks": 100,
  "date": "2024-01-15"
}
```

#### PUT /faculty/performance/{id}
Update performance record.

### Reports

#### GET /faculty/reports/attendance
Generate attendance report.

**Query Parameters**:
- `subjectId` (string, required)
- `startDate` (date, optional)
- `endDate` (date, optional)
- `format` (string: "PDF" or "CSV", default: "PDF")

#### GET /faculty/reports/performance
Generate performance report.

## Student Endpoints

### GET /student/attendance
Get own attendance records.

**Response**:
```json
[
  {
    "subjectId": "subject-id",
    "subjectName": "Data Structures",
    "totalClasses": 40,
    "attendedClasses": 35,
    "percentage": 87.5
  }
]
```

### GET /student/performance
Get own performance records.

**Response**:
```json
[
  {
    "subjectId": "subject-id",
    "subjectName": "Data Structures",
    "assessments": [
      {
        "type": "MIDTERM",
        "obtainedMarks": 85,
        "maxMarks": 100,
        "grade": "A",
        "date": "2024-01-15"
      }
    ]
  }
]
```

### GET /student/dashboard
Get dashboard statistics.

**Response**:
```json
{
  "overallAttendance": 87.5,
  "currentGPA": 8.5,
  "totalSubjects": 6,
  "lowAttendanceSubjects": 1
}
```

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found"
}
```

## Rate Limiting

- **Limit**: 100 requests per minute per IP
- **Header**: `X-RateLimit-Remaining` shows remaining requests
- **Response** (429 Too Many Requests):
```json
{
  "message": "Rate limit exceeded. Try again in 60 seconds."
}
```

## Pagination

All list endpoints support pagination:
- `page`: Page number (0-indexed)
- `size`: Items per page (default: 10, max: 100)
- `sort`: Sort field and direction (e.g., `name,asc`)

## Testing with Postman

Import the Postman collection: `backend/POSTMAN_COLLECTION.json`

1. Set environment variable `baseUrl` to `http://localhost:8080`
2. Login to get access token
3. Set environment variable `accessToken` with the received token
4. Test other endpoints

## Security

- All endpoints (except auth) require JWT authentication
- Tokens expire after 1 hour (access) and 7 days (refresh)
- CORS is configured for allowed origins only
- Rate limiting prevents abuse
- Input validation on all endpoints
- SQL injection protection (using MongoDB)
- XSS protection with input sanitization

## Support

For API issues or questions:
- Check Swagger UI for interactive documentation
- Review Postman collection for examples
- Contact: support@college.edu
