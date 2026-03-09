# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Fault Condition** - Faculty Allocation ID Mismatch Bug
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: For deterministic bugs, scope the property to the concrete failing case(s) to ensure reproducibility
  - Create integration test in `backend/src/test/java/com/college/activitytracker/controller/FacultyAllocationRetrievalBugTest.java`
  - Test setup: Create faculty member with User ID `"user123"` and Faculty ID `"faculty456"`, create 2 class allocations using Faculty ID
  - Test execution: Simulate authenticated request with User ID, call `FacultyController.getMyAllocations()`
  - Test assertion: Verify allocations list is NOT empty and contains exactly 2 allocations (from Fault Condition in design)
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS with empty list returned (this is correct - it proves the bug exists)
  - Document counterexamples found: "getMyAllocations() with userId='user123' returns 0 allocations, but database has 2 allocations for facultyId='faculty456'"
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Admin Panel and Other Faculty Operations
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-buggy inputs
  - Write property-based tests capturing observed behavior patterns from Preservation Requirements
  - Property-based testing generates many test cases for stronger guarantees
  - Create test file: `backend/src/test/java/com/college/activitytracker/preservation/FacultyAllocationPreservationTest.java`
  - Test 1: Admin creates allocation with Faculty ID → verify allocation is stored correctly (observe on unfixed code)
  - Test 2: Admin views all allocations → verify all allocations are returned (observe on unfixed code)
  - Test 3: Admin deletes allocation → verify deletion works and faculty deletion protection triggers (observe on unfixed code)
  - Test 4: Faculty marks attendance → verify attendance marking unaffected (observe on unfixed code)
  - Test 5: Faculty enters performance data → verify performance entry unaffected (observe on unfixed code)
  - Test 6: Faculty with no allocations logs in → verify empty list returned, not error (observe on unfixed code)
  - Test 7: Authentication flow → verify User ID still used for login validation (observe on unfixed code)
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 3. Fix for Faculty Allocation Retrieval ID Mismatch

  - [x] 3.1 Add FacultyRepository.findByUserId method
    - Open `backend/src/main/java/com/college/activitytracker/repository/FacultyRepository.java`
    - Add method signature: `Optional<Faculty> findByUserId(String userId);`
    - Spring Data MongoDB will auto-implement the query
    - _Bug_Condition: isBugCondition(input) where userId != facultyId AND allocations exist for facultyId_
    - _Expected_Behavior: Repository method returns Faculty entity when queried by User ID_
    - _Preservation: No impact on existing repository methods_
    - _Requirements: 2.1_

  - [x] 3.2 Add FacultyService.getFacultyByUserId method
    - Open `backend/src/main/java/com/college/activitytracker/service/FacultyService.java`
    - Add method:
      ```java
      public Faculty getFacultyByUserId(String userId) {
          return facultyRepository.findByUserId(userId)
              .orElseThrow(() -> new ResourceNotFoundException("Faculty", "userId", userId));
      }
      ```
    - _Bug_Condition: User ID needs to be resolved to Faculty ID_
    - _Expected_Behavior: Service method retrieves Faculty by User ID with proper error handling_
    - _Preservation: No impact on existing service methods_
    - _Requirements: 2.1_

  - [x] 3.3 Inject ClassAllocationService into FacultyService
    - Open `backend/src/main/java/com/college/activitytracker/service/FacultyService.java`
    - Add private field: `private final ClassAllocationService classAllocationService;`
    - Update constructor to inject ClassAllocationService:
      ```java
      public FacultyService(FacultyRepository facultyRepository, 
                           AuditLogService auditLogService, 
                           ClassAllocationRepository classAllocationRepository,
                           ClassAllocationService classAllocationService) {
          this.facultyRepository = facultyRepository;
          this.auditLogService = auditLogService;
          this.classAllocationRepository = classAllocationRepository;
          this.classAllocationService = classAllocationService;
      }
      ```
    - _Bug_Condition: Service needs to call ClassAllocationService to retrieve allocations_
    - _Expected_Behavior: Dependency injection enables two-step resolution_
    - _Preservation: No impact on existing dependencies_
    - _Requirements: 2.2_

  - [x] 3.4 Add FacultyService.getAllocationsByUserId method
    - Open `backend/src/main/java/com/college/activitytracker/service/FacultyService.java`
    - Add method:
      ```java
      public List<ClassAllocationDTO> getAllocationsByUserId(String userId) {
          Faculty faculty = getFacultyByUserId(userId);
          return classAllocationService.getAllocationsByFaculty(faculty.getId());
      }
      ```
    - This implements the two-step resolution: User ID → Faculty ID → Allocations
    - _Bug_Condition: isBugCondition(input) where User ID is used directly without conversion_
    - _Expected_Behavior: expectedBehavior(result) - method returns all allocations for the faculty member_
    - _Preservation: No impact on existing allocation retrieval methods_
    - _Requirements: 2.2, 2.3_

  - [x] 3.5 Inject FacultyService into FacultyController
    - Open `backend/src/main/java/com/college/activitytracker/controller/FacultyController.java`
    - Add private field: `private final FacultyService facultyService;`
    - Update constructor to inject FacultyService:
      ```java
      public FacultyController(AttendanceService attendanceService,
                              PerformanceService performanceService,
                              ClassAllocationService classAllocationService,
                              ReportService reportService,
                              FacultyService facultyService) {
          this.attendanceService = attendanceService;
          this.performanceService = performanceService;
          this.classAllocationService = classAllocationService;
          this.reportService = reportService;
          this.facultyService = facultyService;
      }
      ```
    - _Bug_Condition: Controller needs to call FacultyService for ID resolution_
    - _Expected_Behavior: Dependency injection enables controller to use new service method_
    - _Preservation: No impact on existing controller dependencies_
    - _Requirements: 2.3_

  - [x] 3.6 Update FacultyController.getMyAllocations endpoint
    - Open `backend/src/main/java/com/college/activitytracker/controller/FacultyController.java`
    - Locate the `getMyAllocations` method (around line 40-45)
    - Replace the implementation:
      ```java
      @GetMapping("/allocations")
      public ResponseEntity<List<ClassAllocationDTO>> getMyAllocations(Authentication authentication) {
          String userId = authentication.getName();  // Renamed variable for clarity
          List<ClassAllocationDTO> allocations = facultyService.getAllocationsByUserId(userId);
          return ResponseEntity.ok(allocations);
      }
      ```
    - Key changes: Rename variable to `userId`, call `facultyService.getAllocationsByUserId()` instead of direct service call
    - _Bug_Condition: isBugCondition(input) where authentication.getName() returns User ID but allocations are stored by Faculty ID_
    - _Expected_Behavior: expectedBehavior(result) from design - endpoint returns all allocations for logged-in faculty_
    - _Preservation: Preservation Requirements from design - no impact on other endpoints_
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.7 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Faculty Allocation Retrieval Works Correctly
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1: `FacultyAllocationRetrievalBugTest`
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - Verify test assertions: allocations list contains exactly 2 allocations with correct details
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.8 Verify preservation tests still pass
    - **Property 2: Preservation** - Admin Panel and Other Faculty Operations Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2: `FacultyAllocationPreservationTest`
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Verify all 7 preservation test cases still pass:
      - Admin allocation creation works
      - Admin allocation viewing works
      - Admin allocation deletion and faculty deletion protection work
      - Faculty attendance marking unaffected
      - Faculty performance entry unaffected
      - Empty allocation handling works
      - Authentication flow unchanged
    - Confirm all tests still pass after fix (no regressions)
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 4. Write comprehensive unit tests

  - [x] 4.1 Test FacultyRepository.findByUserId
    - Create test file: `backend/src/test/java/com/college/activitytracker/repository/FacultyRepositoryTest.java`
    - Test case 1: findByUserId returns Faculty when User ID exists
    - Test case 2: findByUserId returns empty Optional when User ID doesn't exist
    - Test case 3: findByUserId handles null User ID gracefully
    - Use @DataMongoTest annotation for repository testing
    - _Requirements: 2.1_

  - [x] 4.2 Test FacultyService.getFacultyByUserId
    - Create test file: `backend/src/test/java/com/college/activitytracker/service/FacultyServiceTest.java`
    - Test case 1: getFacultyByUserId returns Faculty entity when found
    - Test case 2: getFacultyByUserId throws ResourceNotFoundException when User ID not found
    - Test case 3: getFacultyByUserId handles null User ID with appropriate exception
    - Mock FacultyRepository using @MockBean
    - _Requirements: 2.1_

  - [x] 4.3 Test FacultyService.getAllocationsByUserId
    - Add tests to `backend/src/test/java/com/college/activitytracker/service/FacultyServiceTest.java`
    - Test case 1: getAllocationsByUserId returns allocations when faculty exists
    - Test case 2: getAllocationsByUserId returns empty list when faculty has no allocations
    - Test case 3: getAllocationsByUserId throws ResourceNotFoundException when faculty not found
    - Test case 4: getAllocationsByUserId calls ClassAllocationService with correct Faculty ID
    - Mock both FacultyRepository and ClassAllocationService
    - Verify method call sequence: getFacultyByUserId → getAllocationsByFaculty
    - _Requirements: 2.2, 2.3_

  - [x] 4.4 Test FacultyController.getMyAllocations
    - Create test file: `backend/src/test/java/com/college/activitytracker/controller/FacultyControllerTest.java`
    - Test case 1: getMyAllocations returns 200 OK with allocations list
    - Test case 2: getMyAllocations extracts User ID from authentication correctly
    - Test case 3: getMyAllocations handles ResourceNotFoundException with 404 response
    - Test case 4: getMyAllocations returns empty list when faculty has no allocations
    - Use @WebMvcTest annotation for controller testing
    - Mock FacultyService and Authentication
    - _Requirements: 2.3_

  - [x] 4.5 Test error handling scenarios
    - Add tests to existing test files
    - Test case 1: Null authentication object returns 401 Unauthorized
    - Test case 2: Database connection error propagates correctly
    - Test case 3: Invalid User ID format handled gracefully
    - Test case 4: Concurrent requests don't interfere with each other
    - _Requirements: 2.1, 2.2, 2.3_

- [x] 5. Write integration tests

  - [x] 5.1 Test full faculty dashboard flow
    - Create test file: `backend/src/test/java/com/college/activitytracker/integration/FacultyDashboardIntegrationTest.java`
    - Test case 1: Faculty logs in → dashboard loads → allocations displayed
    - Test case 2: Faculty with multiple allocations sees all of them
    - Test case 3: Faculty with no allocations sees empty state
    - Use @SpringBootTest annotation for full integration testing
    - Use TestRestTemplate for HTTP requests
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 5.2 Test admin allocation creation flow
    - Add tests to integration test file
    - Test case 1: Admin creates allocation → faculty refreshes dashboard → new allocation appears
    - Test case 2: Admin updates allocation → faculty sees updated details
    - Test case 3: Admin deletes allocation → faculty no longer sees it
    - Verify end-to-end flow with real database operations
    - _Requirements: 3.1, 3.2_

  - [x] 5.3 Test cross-faculty isolation
    - Add tests to integration test file
    - Test case 1: Faculty A's allocations don't appear in Faculty B's dashboard
    - Test case 2: Multiple faculty members can retrieve allocations concurrently
    - Test case 3: User ID from Faculty A doesn't accidentally match Faculty B's Faculty ID
    - _Requirements: 2.3_

  - [x] 5.4 Test database query performance
    - Add performance tests to integration test file
    - Test case 1: Allocation retrieval with 1 allocation completes in < 100ms
    - Test case 2: Allocation retrieval with 50 allocations completes in < 500ms
    - Test case 3: Concurrent requests from 10 faculty members complete successfully
    - Use @Timed annotation or manual timing
    - _Requirements: 2.2, 2.3_

- [ ] 6. Manual testing verification

  - [x] 6.1 Test with real faculty accounts
    - Log in as "Rajesh Kumar" (User ID: `675e8f9a2b1c3d4e5f6a7b8c`)
    - Verify dashboard displays 2 allocations: Data Structures, Algorithms
    - Log in as "Priya Sharma" (User ID: `675e8f9a2b1c3d4e5f6a7b90`)
    - Verify dashboard displays 3 allocations
    - Log in as faculty with no allocations
    - Verify empty state is displayed (not error message)
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ] 6.2 Test admin panel operations
    - Log in as admin
    - Create new class allocation for a faculty member
    - Verify allocation appears in admin panel
    - Log in as that faculty member
    - Verify new allocation appears in faculty dashboard
    - Return to admin panel and delete the allocation
    - Verify faculty dashboard no longer shows it
    - _Requirements: 3.1, 3.2_

  - [ ] 6.3 Test faculty operations unaffected
    - Log in as faculty member
    - Mark attendance for a class
    - Verify attendance marking works correctly
    - Enter performance data for students
    - Verify performance entry works correctly
    - Generate a report
    - Verify report generation works correctly
    - _Requirements: 3.3_

  - [ ] 6.4 Test error scenarios
    - Attempt to access faculty dashboard without authentication
    - Verify 401 Unauthorized response
    - Create faculty account without User ID (if possible)
    - Verify appropriate error handling
    - Test with invalid authentication token
    - Verify proper error response
    - _Requirements: 2.1, 2.3_

- [ ] 7. Checkpoint - Ensure all tests pass
  - Run all unit tests: `mvn test`
  - Run all integration tests: `mvn verify`
  - Verify test coverage meets requirements (>80% for modified files)
  - Review test output for any warnings or failures
  - Ensure all manual testing scenarios completed successfully
  - Ask the user if questions arise or if additional testing is needed
