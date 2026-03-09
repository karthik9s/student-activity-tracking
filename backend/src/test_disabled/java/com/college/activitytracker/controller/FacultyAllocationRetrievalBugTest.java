package com.college.activitytracker.controller;

import com.college.activitytracker.dto.ClassAllocationDTO;
import com.college.activitytracker.model.ClassAllocation;
import com.college.activitytracker.model.Faculty;
import com.college.activitytracker.model.User;
import com.college.activitytracker.repository.ClassAllocationRepository;
import com.college.activitytracker.repository.FacultyRepository;
import com.college.activitytracker.repository.UserRepository;
import com.college.activitytracker.service.ClassAllocationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Test for Faculty Allocation Retrieval
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3**
 * 
 * **Property 1: Fault Condition** - Faculty Allocation ID Mismatch Bug
 * 
 * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists
 * DO NOT attempt to fix the test or the code when it fails
 * 
 * NOTE: This test encodes the expected behavior - it will validate the fix when it passes after implementation
 * 
 * GOAL: Surface counterexamples that demonstrate the bug exists
 * 
 * Bug Description:
 * - Admin panel stores class allocations using Faculty ID (Faculty._id)
 * - Faculty dashboard API queries allocations using User ID (authentication.getName())
 * - Since User ID != Faculty ID, the query returns empty results
 * - Faculty members cannot see their assigned classes
 * 
 * Test Setup:
 * - Create faculty member with User ID "user123" and Faculty ID "faculty456"
 * - Create 2 class allocations using Faculty ID "faculty456"
 * 
 * Test Execution:
 * - Simulate authenticated request with User ID "user123"
 * - Call FacultyController.getMyAllocations()
 * 
 * Expected Outcome on UNFIXED code:
 * - Test FAILS with empty list returned (proves bug exists)
 * - Counterexample: "getMyAllocations() with userId='user123' returns 0 allocations, 
 *   but database has 2 allocations for facultyId='faculty456'"
 * 
 * Expected Outcome on FIXED code:
 * - Test PASSES with 2 allocations returned (proves bug is fixed)
 */
@SpringBootTest
class FacultyAllocationRetrievalBugTest {

    @Autowired
    private FacultyController facultyController;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private ClassAllocationRepository classAllocationRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String USER_ID = "user123";
    private static final String FACULTY_ID = "faculty456";

    private Faculty testFaculty;
    private User testUser;
    private ClassAllocation allocation1;
    private ClassAllocation allocation2;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();

        // Create User entity
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setEmail("testfaculty@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setRole("FACULTY");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Create Faculty entity with different ID than User ID
        testFaculty = new Faculty();
        testFaculty.setId(FACULTY_ID);
        testFaculty.setUserId(USER_ID);  // Links to User ID
        testFaculty.setEmployeeId("EMP001");
        testFaculty.setFirstName("Test");
        testFaculty.setLastName("Faculty");
        testFaculty.setEmail("testfaculty@example.com");
        testFaculty.setPhone("1234567890");
        testFaculty.setDepartment("Computer Science");
        testFaculty.setDesignation("Assistant Professor");
        testFaculty.setIsActive(true);
        testFaculty.setCreatedAt(LocalDateTime.now());
        testFaculty = facultyRepository.save(testFaculty);

        // Create first class allocation using Faculty ID (not User ID)
        allocation1 = new ClassAllocation();
        allocation1.setFacultyId(FACULTY_ID);  // Uses Faculty ID, not User ID
        allocation1.setSubjectId("subject1");
        allocation1.setCourseId("course1");
        allocation1.setYear(2);
        allocation1.setSection("A");
        allocation1.setAcademicYear("2024-2025");
        allocation1.setSemester("Fall");
        allocation1.setIsActive(true);
        allocation1.setCreatedAt(LocalDateTime.now());
        allocation1 = classAllocationRepository.save(allocation1);

        // Create second class allocation using Faculty ID (not User ID)
        allocation2 = new ClassAllocation();
        allocation2.setFacultyId(FACULTY_ID);  // Uses Faculty ID, not User ID
        allocation2.setSubjectId("subject2");
        allocation2.setCourseId("course1");
        allocation2.setYear(2);
        allocation2.setSection("B");
        allocation2.setAcademicYear("2024-2025");
        allocation2.setSemester("Fall");
        allocation2.setIsActive(true);
        allocation2.setCreatedAt(LocalDateTime.now());
        allocation2 = classAllocationRepository.save(allocation2);
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
        // Clear security context
        SecurityContextHolder.clearContext();
    }

    private void cleanupTestData() {
        // Clean up in reverse order of dependencies
        if (allocation1 != null && allocation1.getId() != null) {
            classAllocationRepository.deleteById(allocation1.getId());
        }
        if (allocation2 != null && allocation2.getId() != null) {
            classAllocationRepository.deleteById(allocation2.getId());
        }
        if (testFaculty != null && testFaculty.getId() != null) {
            facultyRepository.deleteById(testFaculty.getId());
        }
        if (testUser != null && testUser.getId() != null) {
            userRepository.deleteById(testUser.getId());
        }
    }

    /**
     * Test the bug condition: Faculty member cannot see allocations due to ID mismatch
     * 
     * This test demonstrates the core bug:
     * 1. Faculty member has User ID "user123" (from authentication)
     * 2. Faculty member has Faculty ID "faculty456" (in Faculty collection)
     * 3. Class allocations are stored with Faculty ID "faculty456"
     * 4. getMyAllocations() uses User ID "user123" to query allocations
     * 5. Query returns empty because allocations are stored with Faculty ID, not User ID
     * 
     * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS
     * - allocations list will be empty (size = 0)
     * - This proves the bug exists
     * 
     * EXPECTED OUTCOME ON FIXED CODE: Test PASSES
     * - allocations list will contain 2 items
     * - This proves the bug is fixed
     */
    @Test
    void testFacultyAllocationRetrievalWithIdMismatch() {
        // Arrange: Create authentication with User ID (simulates faculty login)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            USER_ID,  // authentication.getName() returns User ID
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_FACULTY"))
        );
        
        // Set the security context for the test
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Verify test setup: Allocations exist in database with Faculty ID
        List<ClassAllocation> allocationsInDb = classAllocationRepository.findByFacultyId(FACULTY_ID);
        assertEquals(2, allocationsInDb.size(), 
            "Setup verification failed: Database should contain 2 allocations for Faculty ID " + FACULTY_ID);

        // Verify bug condition: Querying with User ID returns empty
        List<ClassAllocation> allocationsWithUserId = classAllocationRepository.findByFacultyId(USER_ID);
        assertEquals(0, allocationsWithUserId.size(),
            "Bug condition verification: Querying with User ID should return 0 allocations (this confirms the bug exists)");

        // Act: Call the controller method (simulates faculty dashboard request)
        var response = facultyController.getMyAllocations(authentication);

        // Assert: Verify allocations are returned (this will FAIL on unfixed code)
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        List<ClassAllocationDTO> allocations = response.getBody();
        
        // This assertion will FAIL on unfixed code (allocations will be empty)
        // This assertion will PASS on fixed code (allocations will contain 2 items)
        assertFalse(allocations.isEmpty(), 
            "COUNTEREXAMPLE FOUND: getMyAllocations() with userId='" + USER_ID + 
            "' returns 0 allocations, but database has 2 allocations for facultyId='" + FACULTY_ID + "'. " +
            "This confirms the bug: User ID is used to query allocations, but allocations are stored with Faculty ID.");
        
        assertEquals(2, allocations.size(),
            "Faculty member should see exactly 2 allocations (the ones stored with Faculty ID " + FACULTY_ID + ")");
        
        // Additional verification: Check that the returned allocations are the correct ones
        assertTrue(allocations.stream().anyMatch(a -> "subject1".equals(a.getSubjectId())),
            "Allocations should include subject1");
        assertTrue(allocations.stream().anyMatch(a -> "subject2".equals(a.getSubjectId())),
            "Allocations should include subject2");
    }

    /**
     * Additional test: Verify that querying directly with Faculty ID works
     * 
     * This test confirms that the data is correctly stored and can be retrieved
     * when using the correct Faculty ID. This helps isolate the bug to the
     * ID resolution logic in the controller.
     */
    @Test
    void testDirectQueryWithFacultyIdWorks() {
        // This test verifies that the data is correctly stored
        // and can be retrieved when using Faculty ID directly
        
        List<ClassAllocation> allocations = classAllocationRepository.findByFacultyId(FACULTY_ID);
        
        assertEquals(2, allocations.size(),
            "Direct query with Faculty ID should return 2 allocations");
        
        // Verify that querying with User ID returns nothing (confirms the mismatch)
        List<ClassAllocation> allocationsWithUserId = classAllocationRepository.findByFacultyId(USER_ID);
        assertEquals(0, allocationsWithUserId.size(),
            "Direct query with User ID should return 0 allocations (confirms ID mismatch)");
    }
}
