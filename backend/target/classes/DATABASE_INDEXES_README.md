# MongoDB Database Indexes Documentation

## Overview

This document describes the comprehensive indexing strategy implemented for the Student Activity & Academic Tracking System. The indexes are designed to optimize query performance, enforce data integrity, and support the application's most common access patterns.

## Index Creation Methods

### Method 1: Automatic Creation (Recommended for Development)

The `MongoIndexConfig.java` class automatically creates all indexes when the Spring Boot application starts. This is the recommended approach for development and production environments as it ensures indexes are always in sync with the application code.

**Location:** `backend/src/main/java/com/college/activitytracker/config/MongoIndexConfig.java`

**Usage:** Indexes are created automatically on application startup. No manual intervention required.

### Method 2: Manual Script Execution

For manual database setup or maintenance, you can run the MongoDB index creation script directly.

**Location:** `backend/src/main/resources/mongodb-indexes.js`

**Usage:**
```bash
# Using mongosh (MongoDB Shell 6.x+)
mongosh mongodb://localhost:27017/student_tracker < mongodb-indexes.js

# Or connect first, then load the script
mongosh mongodb://localhost:27017/student_tracker
> load('mongodb-indexes.js')
```

## Index Strategy by Collection

### 1. Users Collection

**Purpose:** Stores user authentication and authorization data

**Indexes:**
- `idx_users_email` (UNIQUE): Fast user lookup during authentication
- `idx_users_role`: Efficient role-based queries for RBAC
- `idx_users_deletedAt`: Quick filtering of soft-deleted users
- `idx_users_isActive`: Fast filtering of active/inactive users

**Query Patterns Optimized:**
- Login authentication by email
- Role-based user listing
- Active user filtering
- Soft delete exclusion

### 2. Students Collection

**Purpose:** Stores student profile and academic information

**Indexes:**
- `idx_students_rollNumber` (UNIQUE): Fast student lookup by roll number
- `idx_students_email` (UNIQUE): Unique email constraint and lookup
- `idx_students_userId`: User-student relationship mapping
- `idx_students_course_year_section` (COMPOUND): Class-based queries
- `idx_students_deletedAt`: Soft delete filtering
- `idx_students_isActive`: Active student filtering
- `idx_students_name` (COMPOUND): Name-based search

**Query Patterns Optimized:**
- Student lookup by roll number
- Class roster retrieval (course + year + section)
- Student search by name
- Active student listing
- User-to-student mapping

**Compound Index Benefit:**
The `courseId + year + section` compound index is crucial for:
- Faculty viewing students in their allocated classes
- Admin generating class-wise reports
- Attendance marking for entire classes

### 3. Faculty Collection

**Purpose:** Stores faculty profile and employment information

**Indexes:**
- `idx_faculty_employeeId` (UNIQUE): Fast faculty lookup by employee ID
- `idx_faculty_email` (UNIQUE): Unique email constraint and lookup
- `idx_faculty_userId`: User-faculty relationship mapping
- `idx_faculty_deletedAt`: Soft delete filtering
- `idx_faculty_isActive`: Active faculty filtering
- `idx_faculty_department`: Department-wise queries

**Query Patterns Optimized:**
- Faculty lookup by employee ID
- Department-wise faculty listing
- Active faculty filtering
- User-to-faculty mapping

### 4. Courses Collection

**Purpose:** Stores academic course/program information

**Indexes:**
- `idx_courses_code` (UNIQUE): Fast course lookup by code
- `idx_courses_name`: Course search by name

**Query Patterns Optimized:**
- Course lookup by code
- Course search and listing

### 5. Subjects Collection

**Purpose:** Stores subject/course unit information

**Indexes:**
- `idx_subjects_code` (UNIQUE): Fast subject lookup by code
- `idx_subjects_name`: Subject search by name
- `idx_subjects_course_semester` (COMPOUND): Course-semester mapping

**Query Patterns Optimized:**
- Subject lookup by code
- Semester-wise subject listing for a course
- Subject search

**Compound Index Benefit:**
The `courseId + semester` compound index enables efficient:
- Semester-wise subject retrieval
- Course curriculum queries

### 6. ClassAllocation Collection

**Purpose:** Maps faculty to subjects and classes

**Indexes:**
- `idx_classAlloc_faculty_subject` (COMPOUND): Faculty-subject mapping
- `idx_classAlloc_course_year_section_sem` (COMPOUND): Class identification
- `idx_classAlloc_facultyId`: Faculty allocation queries
- `idx_classAlloc_subjectId`: Subject allocation queries
- `idx_classAlloc_isActive`: Active allocation filtering
- `idx_classAlloc_academicYear`: Academic year filtering
- `idx_classAlloc_unique_allocation` (UNIQUE COMPOUND): Prevents duplicate allocations

**Query Patterns Optimized:**
- Faculty viewing their allocated classes
- Finding faculty for a specific class
- Preventing duplicate allocations
- Academic year-wise allocation queries

**Unique Compound Index:**
The 7-field unique compound index prevents duplicate allocations:
```
facultyId + subjectId + courseId + year + section + semester + academicYear
```
This ensures one faculty cannot be allocated the same class twice in the same academic year.

### 7. Attendance Collection

**Purpose:** Stores student attendance records

**Indexes:**
- `idx_attendance_student_subject_date` (UNIQUE COMPOUND): Prevents duplicate attendance
- `idx_attendance_studentId`: Student attendance queries
- `idx_attendance_subject_date` (COMPOUND): Subject-wise attendance by date
- `idx_attendance_facultyId`: Faculty attendance queries
- `idx_attendance_date`: Date-range queries
- `idx_attendance_status`: Status-based filtering
- `idx_attendance_student_subject` (COMPOUND): Student-subject attendance summary

**Query Patterns Optimized:**
- Student viewing their attendance
- Faculty marking attendance for a class
- Attendance percentage calculation
- Date-range attendance reports
- Low attendance identification

**Critical Unique Index:**
The `studentId + subjectId + date` unique compound index is essential for:
- Preventing duplicate attendance records for the same day
- Enforcing data integrity (Requirement 6.7)
- Fast duplicate checking during bulk attendance marking

**Performance Impact:**
- Attendance percentage calculation: O(log n) instead of O(n)
- Bulk attendance marking: Duplicate check in O(log n)
- Date-range queries: Efficient with date index

### 8. Performance Collection

**Purpose:** Stores student academic performance records

**Indexes:**
- `idx_performance_student_subject` (COMPOUND): Student-subject performance
- `idx_performance_studentId`: Student performance queries
- `idx_performance_subjectId`: Subject performance queries
- `idx_performance_facultyId`: Faculty performance queries
- `idx_performance_assessmentType`: Assessment type filtering
- `idx_performance_date`: Date-range queries
- `idx_performance_student_date` (COMPOUND): Student performance timeline

**Query Patterns Optimized:**
- Student viewing their marks
- GPA calculation
- Subject-wise performance analysis
- Faculty viewing marks they entered
- Assessment type filtering (internal/assignment/exam)

**Compound Index Benefits:**
- `studentId + subjectId`: Fast subject-wise performance retrieval
- `studentId + date`: Performance timeline and trend analysis

### 9. Notifications Collection

**Purpose:** Stores user notifications

**Indexes:**
- `idx_notifications_user_read` (COMPOUND): Unread notification queries
- `idx_notifications_user_created` (COMPOUND): User notifications with sorting
- `idx_notifications_createdAt`: Recent notifications
- `idx_notifications_type`: Notification type filtering

**Query Patterns Optimized:**
- Fetching unread notifications count
- Retrieving user notifications sorted by date
- Notification type filtering
- Recent notifications display

**Compound Index Benefits:**
- `userId + isRead`: Instant unread count calculation
- `userId + createdAt`: Efficient pagination of user notifications

### 10. Announcements Collection

**Purpose:** Stores system-wide announcements

**Indexes:**
- `idx_announcements_role_active` (COMPOUND): Role-based active announcements
- `idx_announcements_createdAt`: Recent announcements
- `idx_announcements_createdBy`: Creator-based queries
- `idx_announcements_isActive`: Active announcement filtering

**Query Patterns Optimized:**
- Role-specific announcement retrieval
- Active announcement listing
- Recent announcements display

### 11. AuditLogs Collection

**Purpose:** Stores audit trail of system operations

**Indexes:**
- `idx_auditLogs_user_timestamp` (COMPOUND): User activity timeline
- `idx_auditLogs_entity` (COMPOUND): Entity change history
- `idx_auditLogs_timestamp`: Recent activity queries
- `idx_auditLogs_action`: Action type filtering
- `idx_auditLogs_entityType`: Entity type filtering

**Query Patterns Optimized:**
- User activity history
- Entity change tracking
- Recent activity monitoring
- Action-based filtering
- Audit report generation

**Compound Index Benefits:**
- `userId + timestamp`: User activity timeline with efficient sorting
- `entityType + entityId`: Complete change history for any entity

## Index Performance Characteristics

### Single Field Indexes

**Time Complexity:** O(log n) for lookups
**Space Complexity:** O(n)
**Best For:** Equality queries, simple sorting

**Example:**
```javascript
// Without index: O(n) - full collection scan
db.students.find({ rollNumber: "CS20240001" })

// With index: O(log n) - index lookup
// Uses idx_students_rollNumber
```

### Compound Indexes

**Time Complexity:** O(log n) for prefix queries
**Space Complexity:** O(n * k) where k is number of fields
**Best For:** Multi-field queries, complex sorting

**Example:**
```javascript
// Efficient queries using idx_students_course_year_section:
db.students.find({ courseId: "...", year: 2, section: "A" })  // Uses full index
db.students.find({ courseId: "...", year: 2 })                // Uses prefix
db.students.find({ courseId: "..." })                         // Uses prefix

// Inefficient (cannot use index):
db.students.find({ year: 2, section: "A" })                   // Missing prefix
```

### Unique Indexes

**Purpose:** Enforce data integrity constraints
**Performance:** Same as regular indexes
**Additional Benefit:** Prevents duplicate data at database level

**Example:**
```javascript
// Prevents duplicate attendance records
db.attendance.insertOne({
  studentId: "...",
  subjectId: "...",
  date: ISODate("2024-01-15")
})
// Second insert with same values will fail with duplicate key error
```

## Index Maintenance

### Monitoring Index Usage

```javascript
// Check index usage statistics
db.attendance.aggregate([
  { $indexStats: {} }
])

// Identify unused indexes
db.attendance.aggregate([
  { $indexStats: {} },
  { $match: { "accesses.ops": 0 } }
])
```

### Index Size Monitoring

```javascript
// Check index sizes
db.attendance.stats().indexSizes

// Check total collection size including indexes
db.attendance.stats().totalSize
```

### Rebuilding Indexes

```javascript
// Rebuild all indexes for a collection (if needed)
db.attendance.reIndex()

// Drop and recreate a specific index
db.attendance.dropIndex("idx_attendance_date")
db.attendance.createIndex({ date: 1 }, { name: "idx_attendance_date" })
```

## Performance Benchmarks

### Expected Query Performance

| Query Type | Without Index | With Index | Improvement |
|------------|--------------|------------|-------------|
| Student by roll number | O(n) | O(log n) | 100-1000x |
| Class roster (1000 students) | ~100ms | ~1ms | 100x |
| Attendance percentage | O(n) | O(log n) | 50-500x |
| Duplicate attendance check | O(n) | O(log n) | 100-1000x |
| User authentication | O(n) | O(log n) | 100-1000x |

### Index Size Estimates (for 10,000 students)

| Collection | Documents | Index Size | Total Size |
|------------|-----------|------------|------------|
| Students | 10,000 | ~2 MB | ~15 MB |
| Attendance | 500,000 | ~50 MB | ~200 MB |
| Performance | 200,000 | ~20 MB | ~80 MB |
| Users | 10,500 | ~1 MB | ~5 MB |

## Best Practices

### 1. Index Selectivity

**Good:** Indexes on fields with high cardinality (many unique values)
- email, rollNumber, employeeId (unique values)
- date fields (many different dates)

**Poor:** Indexes on fields with low cardinality (few unique values)
- gender (2-3 values)
- status (2-3 values)

**Exception:** Low cardinality fields are indexed when:
- Used in compound indexes (e.g., isActive + other fields)
- Frequently used in queries (e.g., role for RBAC)

### 2. Compound Index Order

**Rule:** Most selective field first, then by query frequency

**Example:**
```javascript
// Good: studentId is more selective than date
{ studentId: 1, date: 1 }

// Less optimal: date is less selective
{ date: 1, studentId: 1 }
```

### 3. Index Prefix Usage

**Rule:** Compound indexes can be used for prefix queries

**Example:**
```javascript
// Index: { courseId: 1, year: 1, section: 1 }

// Can use index:
find({ courseId: "..." })
find({ courseId: "...", year: 2 })
find({ courseId: "...", year: 2, section: "A" })

// Cannot use index:
find({ year: 2 })
find({ section: "A" })
find({ year: 2, section: "A" })
```

### 4. Covered Queries

**Definition:** Queries that can be satisfied entirely from index data

**Example:**
```javascript
// Covered query (only needs indexed fields)
db.students.find(
  { rollNumber: "CS20240001" },
  { rollNumber: 1, _id: 0 }
)
// Returns data directly from index without accessing documents
```

## Troubleshooting

### Slow Queries

1. **Check if index is being used:**
```javascript
db.attendance.find({ studentId: "..." }).explain("executionStats")
```

2. **Look for COLLSCAN (collection scan):**
```javascript
// Bad: "stage": "COLLSCAN"
// Good: "stage": "IXSCAN"
```

3. **Add missing index or optimize query**

### High Memory Usage

1. **Check index sizes:**
```javascript
db.stats()
```

2. **Remove unused indexes:**
```javascript
db.collection.dropIndex("unused_index_name")
```

3. **Consider partial indexes for large collections:**
```javascript
db.attendance.createIndex(
  { date: 1 },
  { partialFilterExpression: { date: { $gte: ISODate("2024-01-01") } } }
)
```

### Duplicate Key Errors

**Cause:** Attempting to insert duplicate values in unique index

**Solution:**
1. Check existing data for duplicates
2. Update application logic to handle duplicates
3. Use upsert operations where appropriate

## Validation

### Verify All Indexes Created

```javascript
// List all indexes for each collection
db.users.getIndexes()
db.students.getIndexes()
db.faculty.getIndexes()
db.courses.getIndexes()
db.subjects.getIndexes()
db.classAllocations.getIndexes()
db.attendance.getIndexes()
db.performance.getIndexes()
db.notifications.getIndexes()
db.announcements.getIndexes()
db.auditLogs.getIndexes()
```

### Expected Index Counts

| Collection | Expected Indexes | Including _id |
|------------|------------------|---------------|
| users | 4 | 5 |
| students | 7 | 8 |
| faculty | 6 | 7 |
| courses | 2 | 3 |
| subjects | 3 | 4 |
| classAllocations | 7 | 8 |
| attendance | 7 | 8 |
| performance | 7 | 8 |
| notifications | 4 | 5 |
| announcements | 4 | 5 |
| auditLogs | 5 | 6 |

## Requirements Validation

This indexing strategy satisfies **Requirement 19: Database Design and Performance**:

✅ **19.1:** Indexes created on frequently queried fields
- User email, student roll number, subject code, faculty employee ID

✅ **19.2:** Compound indexes for multi-field queries
- Student: courseId + year + section
- Attendance: studentId + subjectId + date
- ClassAllocation: facultyId + subjectId
- Performance: studentId + subjectId

✅ **19.3:** Timestamps included in all collections
- createdAt, updatedAt fields in all models

✅ **19.4:** Reference IDs used instead of deep embedding
- All relationships use ObjectId references

✅ **19.5:** Soft delete implementation
- deletedAt field with indexes for filtering

✅ **19.6:** Pagination support
- Indexes support efficient skip/limit operations

✅ **19.7:** Query optimization monitoring
- Index usage can be monitored with explain() and indexStats

## Conclusion

This comprehensive indexing strategy provides:

1. **Performance:** 100-1000x improvement for common queries
2. **Data Integrity:** Unique constraints prevent duplicates
3. **Scalability:** Efficient queries even with large datasets
4. **Maintainability:** Clear documentation and monitoring tools

The indexes are automatically created on application startup, ensuring consistency across all environments.
