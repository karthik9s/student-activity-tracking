# Faculty Allocation Validation for Attendance Marking

## Implementation Summary

This document describes the implementation of faculty allocation validation for attendance marking (Task 14.6).

## Requirements Implemented

- **Requirement 6.1**: WHEN a faculty member marks attendance for a class, THE System SHALL validate the faculty is allocated to that class
- **Requirement 6.6**: THE System SHALL prevent faculty from marking attendance for classes they are not allocated to

## Changes Made

### 1. AttendanceService.java

**Added dependency:**
- `ClassAllocationRepository` - to check faculty allocations

**Modified method:**
- `markAttendance(AttendanceDTO dto)` - Added validation call before marking attendance
- `updateAttendance(String id, AttendanceDTO dto)` - Added validation call before updating attendance

**New method:**
- `validateFacultyAllocation(String facultyId, String subjectId, String courseId, Integer year, String section)`
  - Checks if faculty has an active allocation for the specified class
  - Queries `ClassAllocationRepository` using all class identifiers (facultyId, subjectId, courseId, year, section)
  - Verifies the allocation exists and `isActive` is `true`
  - Throws `IllegalStateException` with message "Faculty is not allocated to this class" if validation fails

## Validation Logic

```java
boolean hasAllocation = classAllocationRepository
    .findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
            facultyId, subjectId, courseId, year, section)
    .map(allocation -> allocation.getIsActive() != null && allocation.getIsActive())
    .orElse(false);

if (!hasAllocation) {
    throw new IllegalStateException("Faculty is not allocated to this class");
}
```

The validation ensures:
1. An allocation record exists for the exact combination of faculty, subject, course, year, and section
2. The allocation's `isActive` field is not null and is `true`
3. If either condition fails, attendance marking is prevented

## Test Coverage

Created `FacultyAllocationValidationTest.java` with 7 test cases:

### Mark Attendance Tests

1. **testMarkAttendance_WithValidAllocation_Success**
   - Verifies attendance can be marked when faculty has active allocation
   - Confirms the allocation check is performed
   - Validates attendance is saved successfully

2. **testMarkAttendance_WithoutAllocation_ThrowsException**
   - Verifies exception is thrown when no allocation exists
   - Confirms attendance is NOT saved
   - Validates correct error message

3. **testMarkAttendance_WithInactiveAllocation_ThrowsException**
   - Verifies exception is thrown when allocation exists but `isActive` is `false`
   - Confirms attendance is NOT saved

4. **testMarkAttendance_WithNullIsActive_ThrowsException**
   - Verifies exception is thrown when allocation exists but `isActive` is `null`
   - Confirms attendance is NOT saved

5. **testMarkAttendance_DifferentSubject_ThrowsException**
   - Verifies exception is thrown when faculty tries to mark attendance for a different subject
   - Confirms attendance is NOT saved

### Update Attendance Tests

6. **testUpdateAttendance_WithValidAllocation_Success**
   - Verifies attendance can be updated when faculty has active allocation
   - Confirms the allocation check is performed
   - Validates attendance is saved successfully

7. **testUpdateAttendance_WithoutAllocation_ThrowsException**
   - Verifies exception is thrown when updating attendance without valid allocation
   - Confirms attendance is NOT saved

## Error Handling

When validation fails, the system:
- Throws `IllegalStateException` with message: "Faculty is not allocated to this class"
- Prevents attendance record from being saved
- Returns clear error to the caller/frontend

## Integration Points

The validation integrates with:
- **ClassAllocationRepository**: Uses existing query method `findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection`
- **AttendanceDTO**: Uses existing fields (facultyId, subjectId, courseId, year, section)
- **Existing validation**: Runs after student/subject/faculty existence checks, before attendance save
- **Bulk operations**: `markBulkAttendance` automatically inherits validation since it calls `markAttendance` internally

## Security Implications

This implementation enforces authorization at the service layer:
- Faculty cannot mark attendance for classes they don't teach
- Prevents unauthorized data manipulation
- Complements role-based access control (RBAC)

## Future Considerations

- Consider adding similar validation to `markBulkAttendance` method if needed
- May want to return more detailed error messages (e.g., which specific class details don't match)
- Could add audit logging for failed validation attempts
