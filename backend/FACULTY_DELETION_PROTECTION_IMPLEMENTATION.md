# Faculty Deletion Protection Implementation

## Task 11.7: Faculty Deletion Protection with Active Allocations

### Requirement
**Requirement 4.6**: THE System SHALL prevent deletion of faculty records that have active class allocations

### Implementation Summary

#### Changes Made

1. **FacultyService.java** - Added protection logic to `deleteFaculty()` method:
   - Added `ClassAllocationRepository` dependency injection
   - Added check for active class allocations before deletion
   - Throws `RuntimeException` with clear message if active allocations exist
   - Proceeds with soft delete only if no active allocations found

#### Code Changes

**File**: `backend/src/main/java/com/college/activitytracker/service/FacultyService.java`

**Added Import**:
```java
import com.college.activitytracker.repository.ClassAllocationRepository;
```

**Added Dependency**:
```java
private final ClassAllocationRepository classAllocationRepository;
```

**Modified Method**:
```java
@Transactional
public void deleteFaculty(String id) {
    log.info("Soft deleting faculty with id: {}", id);
    
    Faculty faculty = facultyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));
    
    // Check for active class allocations
    long activeAllocations = classAllocationRepository.countByFacultyIdAndIsActive(id, true);
    if (activeAllocations > 0) {
        throw new RuntimeException("Cannot delete faculty with active class allocations");
    }
    
    // Capture old values before delete
    Map<String, Object> oldValue = facultyToMap(faculty);
    
    faculty.setDeletedAt(LocalDateTime.now());
    faculty.setIsActive(false);
    facultyRepository.save(faculty);
    
    // Log the delete operation
    String userId = getCurrentUserId();
    auditLogService.logDelete(userId, "FACULTY", faculty.getId(), oldValue);
    
    log.info("Faculty soft deleted successfully");
}
```

### Test Coverage

**File**: `backend/src/test/java/com/college/activitytracker/service/FacultyDeletionProtectionTest.java`

Three test scenarios:

1. **testDeleteFacultyWithActiveAllocations_ShouldThrowException**
   - Creates a faculty with an active class allocation (isActive = true)
   - Attempts to delete the faculty
   - Verifies that RuntimeException is thrown with message "Cannot delete faculty with active class allocations"
   - Verifies that faculty was NOT deleted (deletedAt is null, isActive is true)

2. **testDeleteFacultyWithInactiveAllocations_ShouldSucceed**
   - Creates a faculty with an inactive class allocation (isActive = false)
   - Deletes the faculty
   - Verifies that deletion succeeds (deletedAt is set, isActive is false)

3. **testDeleteFacultyWithNoAllocations_ShouldSucceed**
   - Creates a faculty with no class allocations
   - Deletes the faculty
   - Verifies that deletion succeeds (deletedAt is set, isActive is false)

### How It Works

1. When `deleteFaculty(id)` is called, it first retrieves the faculty record
2. It then queries `ClassAllocationRepository.countByFacultyIdAndIsActive(id, true)` to count active allocations
3. If count > 0, it throws an exception preventing deletion
4. If count = 0, it proceeds with the normal soft delete operation

### Benefits

- **Data Integrity**: Prevents orphaned class allocations
- **Clear Error Messages**: Users understand why deletion failed
- **Minimal Code**: Leverages existing repository method
- **Transactional**: Entire operation is atomic
- **Audit Trail**: Deletion is still logged when successful

### Usage

When attempting to delete a faculty member through the API:

**Success Case** (no active allocations):
```
DELETE /api/admin/faculty/{id}
Response: 200 OK
```

**Failure Case** (has active allocations):
```
DELETE /api/admin/faculty/{id}
Response: 500 Internal Server Error
Body: {
  "message": "Cannot delete faculty with active class allocations"
}
```

### Notes

- The implementation uses the existing `countByFacultyIdAndIsActive` method from `ClassAllocationRepository`
- Only ACTIVE allocations (isActive = true) prevent deletion
- Inactive or historical allocations do not block deletion
- The soft delete mechanism remains unchanged - only the validation was added
