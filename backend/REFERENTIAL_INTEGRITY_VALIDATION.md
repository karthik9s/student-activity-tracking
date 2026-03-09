# Referential Integrity Validation Implementation

## Overview

Task 13.5 has been completed. The system implements comprehensive referential integrity validation across all service layers to ensure that referenced entities exist before creating or updating records.

## Requirement

**Requirement 14.5**: System must validate that referenced entities exist before creating/updating records

## Implementation Summary

Referential integrity validation has been implemented in all service classes that handle entity relationships. The validation ensures that:

1. When creating a Student, the referenced Course exists
2. When creating Attendance, the referenced Student, Subject, and Faculty exist
3. When creating Performance, the referenced Student and Subject exist
4. When creating ClassAllocation, the referenced Faculty, Subject, and Course exist
5. When creating Subject, the referenced Course exists

## Detailed Implementation

### 1. StudentService

**Location**: `backend/src/main/java/com/college/activitytracker/service/StudentService.java`

**Validation Points**:
- `createStudent()`: Validates courseId exists before creating student
- `updateStudent()`: Validates courseId exists before updating student

```java
Course course = courseRepository.findById(dto.getCourseId())
        .orElseThrow(() -> new ResourceNotFoundException("Course", "id", dto.getCourseId()));
```

### 2. AttendanceService

**Location**: `backend/src/main/java/com/college/activitytracker/service/AttendanceService.java`

**Validation Points**:
- `markAttendance()`: Validates studentId, subjectId, and facultyId exist before marking attendance

```java
studentRepository.findById(dto.getStudentId())
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
subjectRepository.findById(dto.getSubjectId())
        .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
facultyRepository.findById(dto.getFacultyId())
        .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
```

### 3. PerformanceService

**Location**: `backend/src/main/java/com/college/activitytracker/service/PerformanceService.java`

**Validation Points**:
- `addPerformance()`: Validates studentId and subjectId exist before adding performance record

```java
studentRepository.findById(dto.getStudentId())
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
subjectRepository.findById(dto.getSubjectId())
        .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
```

### 4. ClassAllocationService

**Location**: `backend/src/main/java/com/college/activitytracker/service/ClassAllocationService.java`

**Validation Points**:
- `createAllocation()`: Validates facultyId, subjectId, and courseId exist before creating allocation

```java
facultyRepository.findById(dto.getFacultyId())
        .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
subjectRepository.findById(dto.getSubjectId())
        .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
courseRepository.findById(dto.getCourseId())
        .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
```

### 5. SubjectService

**Location**: `backend/src/main/java/com/college/activitytracker/service/SubjectService.java`

**Validation Points**:
- `createSubject()`: Validates courseId exists before creating subject
- `updateSubject()`: Validates courseId exists if it's being changed

```java
courseRepository.findById(subjectDTO.getCourseId())
        .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
```

### 6. CourseService

**Location**: `backend/src/main/java/com/college/activitytracker/service/CourseService.java`

**Validation Points**:
- `deleteCourse()`: Validates no students are enrolled before allowing deletion (referential integrity protection)

```java
long studentCount = studentRepository.countByCourseId(id);
if (studentCount > 0) {
    throw new IllegalStateException("Cannot delete course with enrolled students");
}
```

## Validation Pattern

All services follow a consistent validation pattern:

1. **Early Validation**: Check referenced entities exist at the start of create/update operations
2. **Fail Fast**: Throw `ResourceNotFoundException` immediately if referenced entity doesn't exist
3. **Transactional Safety**: All operations are wrapped in `@Transactional` to ensure atomicity
4. **Clear Error Messages**: Exceptions include entity type and ID for debugging

## Error Handling

When referential integrity validation fails:

1. A `ResourceNotFoundException` is thrown with details about the missing entity
2. The transaction is rolled back (no partial data is saved)
3. The exception is caught by the global exception handler
4. A 404 Not Found response is returned to the client with error details

## Benefits

1. **Data Integrity**: Prevents orphaned records and maintains database consistency
2. **Early Detection**: Catches invalid references before database operations
3. **Clear Errors**: Provides meaningful error messages for debugging
4. **Transactional Safety**: Ensures all-or-nothing operations
5. **Consistent Pattern**: Same validation approach across all services

## Testing Recommendations

To verify referential integrity validation:

1. **Unit Tests**: Test each service method with invalid entity IDs
2. **Integration Tests**: Test API endpoints with non-existent references
3. **Property-Based Tests**: Test with randomly generated invalid IDs (Task 13.12)

Example test scenarios:
- Create student with non-existent courseId → Should return 404
- Mark attendance with non-existent studentId → Should return 404
- Add performance with non-existent subjectId → Should return 404
- Create allocation with non-existent facultyId → Should return 404

## Related Requirements

- **Requirement 14.5**: System must validate that referenced entities exist before creating/updating records ✅
- **Requirement 14.1**: System SHALL validate all input fields using @Valid annotations
- **Requirement 14.2**: System SHALL return 400 Bad Request with detailed error messages on validation failure
- **Requirement 20.4**: System SHALL return appropriate HTTP status codes (404 for not found)

## Status

✅ **COMPLETED** - All service classes implement referential integrity validation for entity references.

## Next Steps

- Task 13.7: Write unit tests for validation logic (Optional)
- Task 13.12: Write property-based test for Property 16: Referential Integrity Validation (Optional)
