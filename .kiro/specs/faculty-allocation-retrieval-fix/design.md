# Faculty Allocation Retrieval Bugfix Design

## Overview

This design addresses a critical ID mismatch bug in the faculty dashboard where allocated classes are not displayed to faculty members. The root cause is that `authentication.getName()` returns the User ID, but class allocations are stored and queried using the Faculty ID. Since these are distinct identifiers in the system, the query returns no results, leaving faculty dashboards empty.

The fix introduces a two-step resolution process: convert User ID → Faculty ID → retrieve allocations. This ensures faculty members can view their assigned classes while preserving all existing admin panel functionality.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when a faculty member's User ID is used directly to query class allocations instead of their Faculty ID
- **Property (P)**: The desired behavior - faculty members should see all their allocated classes when they log in
- **Preservation**: Existing admin panel allocation management and other faculty operations that must remain unchanged
- **User ID**: The authentication identifier returned by `authentication.getName()`, stored in the User collection
- **Faculty ID**: The MongoDB ObjectId of the Faculty document (Faculty._id), used as the foreign key in ClassAllocation records
- **FacultyController.getMyAllocations()**: The endpoint in `backend/src/main/java/com/college/activitytracker/controller/FacultyController.java` that retrieves allocations for the logged-in faculty member
- **ClassAllocationService.getAllocationsByFaculty()**: The service method in `backend/src/main/java/com/college/activitytracker/service/ClassAllocationService.java` that queries allocations by faculty ID
- **Faculty.userId**: The field in the Faculty model that links to the User ID from authentication

## Bug Details

### Fault Condition

The bug manifests when a faculty member logs into their dashboard and attempts to view their class allocations. The `FacultyController.getMyAllocations()` method retrieves the User ID from authentication and passes it directly to `ClassAllocationService.getAllocationsByFaculty()`, which queries the database using this ID. However, the ClassAllocation collection stores the Faculty ID (Faculty._id), not the User ID, causing the query to return zero results.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type Authentication (Spring Security)
  OUTPUT: boolean
  
  LET userId = input.getName()
  LET facultyRecord = facultyRepository.findByUserId(userId)
  LET facultyId = facultyRecord.getId()
  
  RETURN facultyRecord EXISTS
         AND facultyId != userId
         AND classAllocationRepository.findByFacultyId(userId).isEmpty()
         AND classAllocationRepository.findByFacultyId(facultyId).isNotEmpty()
END FUNCTION
```

### Examples

- **Example 1**: Faculty member "Rajesh Kumar" logs in
  - User ID from authentication: `"675e8f9a2b1c3d4e5f6a7b8c"`
  - Faculty ID in database: `"675e8f9a2b1c3d4e5f6a7b8d"`
  - Current behavior: Query with User ID returns 0 allocations
  - Expected behavior: Should return 2 allocations (Data Structures, Algorithms)

- **Example 2**: Faculty member "Priya Sharma" logs in
  - User ID: `"675e8f9a2b1c3d4e5f6a7b90"`
  - Faculty ID: `"675e8f9a2b1c3d4e5f6a7b91"`
  - Current behavior: Dashboard shows "No allocations found"
  - Expected behavior: Should display 3 allocated classes

- **Example 3**: Faculty member with no allocations logs in
  - User ID: `"675e8f9a2b1c3d4e5f6a7b95"`
  - Faculty ID: `"675e8f9a2b1c3d4e5f6a7b96"`
  - Expected behavior: Should show empty state (not an error) - this is correct behavior

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Admin panel class allocation creation must continue to store Faculty ID in allocation records
- Admin panel allocation viewing, editing, and deletion must continue to work correctly
- Faculty deletion protection (cannot delete faculty with active allocations) must continue to work
- Audit logging for faculty operations must continue to function
- All other faculty endpoints (attendance, performance) must remain unaffected
- Authentication mechanism must continue to use User ID for login validation

**Scope:**
All operations that do NOT involve retrieving allocations for the logged-in faculty member should be completely unaffected by this fix. This includes:
- Admin CRUD operations on class allocations
- Faculty marking attendance or performance
- Faculty report generation
- User authentication and authorization
- Other repository queries using Faculty ID directly

## Hypothesized Root Cause

Based on the bug description and code analysis, the root cause is:

1. **Direct User ID Usage**: The `FacultyController.getMyAllocations()` method calls `authentication.getName()` which returns the User ID, then passes this directly to the service layer without conversion
   - Line in FacultyController.java: `String facultyId = authentication.getName();`
   - This assumes User ID == Faculty ID, which is incorrect

2. **Missing ID Resolution Logic**: The `ClassAllocationService.getAllocationsByFaculty()` method expects a Faculty ID but receives a User ID
   - The service has no logic to detect or convert the ID type
   - It queries `allocationRepository.findByFacultyId(facultyId)` with the wrong ID

3. **Data Model Separation**: The system correctly maintains separate User and Faculty entities
   - Faculty.userId links to User._id
   - ClassAllocation.facultyId links to Faculty._id
   - The bug occurs because the controller skips the Faculty lookup step

4. **No Validation**: There's no error handling to detect when zero allocations are returned due to ID mismatch versus genuinely having no allocations

## Correctness Properties

Property 1: Fault Condition - Faculty Allocation Retrieval

_For any_ authenticated faculty user where the bug condition holds (User ID != Faculty ID and allocations exist for Faculty ID), the fixed getMyAllocations endpoint SHALL resolve the User ID to the Faculty ID and return all class allocations associated with that Faculty ID.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Admin Panel Operations

_For any_ admin operation on class allocations (create, read, update, delete) that does NOT involve the faculty dashboard endpoint, the fixed code SHALL produce exactly the same behavior as the original code, preserving all allocation management functionality.

**Validates: Requirements 3.1, 3.2, 3.3**

Property 3: Preservation - Empty Allocation Handling

_For any_ faculty member who genuinely has no class allocations (regardless of ID resolution), the fixed endpoint SHALL return an empty list and display the appropriate empty state, preserving the existing user experience for faculty without assignments.

**Validates: Requirements 3.4**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File 1**: `backend/src/main/java/com/college/activitytracker/repository/FacultyRepository.java`

**Changes**:
1. **Add findByUserId method**: Add a repository method to query Faculty by User ID
   ```java
   Optional<Faculty> findByUserId(String userId);
   ```

**File 2**: `backend/src/main/java/com/college/activitytracker/service/FacultyService.java`

**Changes**:
1. **Add getFacultyByUserId method**: Create a new service method to retrieve Faculty by User ID
   ```java
   public Faculty getFacultyByUserId(String userId) {
       return facultyRepository.findByUserId(userId)
           .orElseThrow(() -> new ResourceNotFoundException("Faculty", "userId", userId));
   }
   ```

2. **Add getAllocationsByUserId method**: Create a new service method that performs the two-step resolution
   ```java
   public List<ClassAllocationDTO> getAllocationsByUserId(String userId) {
       Faculty faculty = getFacultyByUserId(userId);
       return classAllocationService.getAllocationsByFaculty(faculty.getId());
   }
   ```

**File 3**: `backend/src/main/java/com/college/activitytracker/controller/FacultyController.java`

**Changes**:
1. **Update getMyAllocations endpoint**: Modify to use User ID correctly
   ```java
   @GetMapping("/allocations")
   public ResponseEntity<List<ClassAllocationDTO>> getMyAllocations(Authentication authentication) {
       String userId = authentication.getName();  // Renamed variable for clarity
       List<ClassAllocationDTO> allocations = facultyService.getAllocationsByUserId(userId);
       return ResponseEntity.ok(allocations);
   }
   ```

2. **Add dependency injection**: Inject FacultyService into the controller
   ```java
   private final FacultyService facultyService;
   
   public FacultyController(AttendanceService attendanceService,
                           PerformanceService performanceService,
                           ClassAllocationService classAllocationService,
                           ReportService reportService,
                           FacultyService facultyService) {
       // ... existing assignments
       this.facultyService = facultyService;
   }
   ```

**File 4**: `backend/src/main/java/com/college/activitytracker/service/FacultyService.java` (additional change)

**Changes**:
1. **Add ClassAllocationService dependency**: Inject the service to call allocation methods
   ```java
   private final ClassAllocationService classAllocationService;
   
   public FacultyService(FacultyRepository facultyRepository, 
                        AuditLogService auditLogService, 
                        ClassAllocationRepository classAllocationRepository,
                        ClassAllocationService classAllocationService) {
       // ... existing assignments
       this.classAllocationService = classAllocationService;
   }
   ```

### Error Handling Strategy

1. **Faculty Not Found**: If User ID doesn't map to a Faculty record, throw `ResourceNotFoundException` with clear message
2. **Null Authentication**: If authentication is null, Spring Security will handle with 401 Unauthorized
3. **Empty Allocations**: Return empty list (not an error) - existing behavior preserved
4. **Database Errors**: Let Spring's exception handling propagate MongoDB errors

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Fault Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write integration tests that simulate faculty login and allocation retrieval. Run these tests on the UNFIXED code to observe failures and confirm the ID mismatch hypothesis.

**Test Cases**:
1. **Faculty With Allocations Test**: Create a faculty member with User ID and Faculty ID, assign allocations using Faculty ID, then query using User ID (will fail on unfixed code)
2. **Multiple Allocations Test**: Faculty member with 3+ allocations, verify none are returned when querying with User ID (will fail on unfixed code)
3. **Cross-Faculty Test**: Verify that User ID from Faculty A doesn't accidentally return allocations for Faculty B (should fail correctly on unfixed code)
4. **Database State Verification**: Query database directly to confirm allocations exist with Faculty ID but not User ID (will confirm root cause)

**Expected Counterexamples**:
- `getAllocationsByFaculty(userId)` returns empty list when allocations exist
- Direct database query `db.classAllocations.find({facultyId: userId})` returns 0 documents
- Direct database query `db.classAllocations.find({facultyId: facultyId})` returns N documents (N > 0)
- Possible causes confirmed: ID mismatch, no conversion logic in controller/service

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL authentication WHERE isBugCondition(authentication) DO
  userId := authentication.getName()
  allocations := getMyAllocations_fixed(authentication)
  faculty := facultyRepository.findByUserId(userId)
  expectedAllocations := classAllocationRepository.findByFacultyId(faculty.getId())
  ASSERT allocations.equals(expectedAllocations)
  ASSERT allocations.isNotEmpty()
END FOR
```

**Test Cases**:
1. **Single Allocation Retrieval**: Faculty with 1 allocation, verify it's returned correctly
2. **Multiple Allocations Retrieval**: Faculty with 5 allocations, verify all are returned
3. **Allocation Details Verification**: Verify returned DTOs contain correct subject, course, year, section data
4. **Active Allocations Only**: Verify only active allocations are returned (if filtering is applied)

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL operation WHERE NOT isFacultyDashboardAllocationRetrieval(operation) DO
  ASSERT operation_original() = operation_fixed()
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for admin operations and other faculty endpoints, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Admin Allocation Creation Preservation**: Verify admin can still create allocations with Faculty ID, behavior unchanged
2. **Admin Allocation Viewing Preservation**: Verify admin panel displays all allocations correctly after fix
3. **Admin Allocation Deletion Preservation**: Verify admin can delete allocations, faculty deletion protection still works
4. **Faculty Attendance Marking Preservation**: Verify faculty can mark attendance, no impact from allocation fix
5. **Faculty Performance Entry Preservation**: Verify faculty can enter performance data, no impact from allocation fix
6. **Empty Allocation Handling Preservation**: Verify faculty with no allocations still see empty state (not error)
7. **Authentication Preservation**: Verify login process unchanged, User ID still used for authentication

### Unit Tests

- Test `FacultyRepository.findByUserId()` returns correct Faculty entity
- Test `FacultyService.getFacultyByUserId()` throws ResourceNotFoundException when User ID not found
- Test `FacultyService.getAllocationsByUserId()` calls correct service methods in sequence
- Test `FacultyController.getMyAllocations()` extracts User ID from authentication correctly
- Test error handling for null authentication, missing faculty, database errors

### Property-Based Tests

- Generate random User IDs and verify that if a Faculty exists, allocations are retrieved correctly
- Generate random Faculty records with varying allocation counts (0 to 10) and verify correct retrieval
- Generate random admin operations and verify they produce identical results before and after fix
- Test that User ID → Faculty ID → Allocations chain works for all valid faculty members

### Integration Tests

- Test full flow: faculty login → dashboard load → allocations displayed
- Test admin creates allocation → faculty refreshes dashboard → new allocation appears
- Test faculty with multiple subjects across different courses and years
- Test concurrent faculty logins don't interfere with each other's allocation retrieval
- Test database query performance with large allocation datasets
