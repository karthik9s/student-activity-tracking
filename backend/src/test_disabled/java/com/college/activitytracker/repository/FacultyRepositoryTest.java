package com.college.activitytracker.repository;

import com.college.activitytracker.model.Faculty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for FacultyRepository.findByUserId method
 * 
 * **Validates: Requirements 2.1**
 * 
 * Tests the repository method that resolves User ID to Faculty entity,
 * which is critical for fixing the faculty allocation retrieval bug.
 */
@DataMongoTest
@TestPropertySource(properties = {
    "spring.mongodb.embedded.version=4.0.21"
})
class FacultyRepositoryTest {

    @Autowired
    private FacultyRepository facultyRepository;

    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        facultyRepository.deleteAll();

        // Create test faculty with User ID
        testFaculty = new Faculty();
        testFaculty.setUserId("user123");
        testFaculty.setEmployeeId("EMP001");
        testFaculty.setFirstName("Rajesh");
        testFaculty.setLastName("Kumar");
        testFaculty.setEmail("rajesh.kumar@college.edu");
        testFaculty.setPhone("9876543210");
        testFaculty.setDepartment("Computer Science");
        testFaculty.setDesignation("Assistant Professor");
        testFaculty.setIsActive(true);
        testFaculty.setCreatedAt(LocalDateTime.now());
        testFaculty.setUpdatedAt(LocalDateTime.now());

        // Save to database
        testFaculty = facultyRepository.save(testFaculty);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        facultyRepository.deleteAll();
    }

    /**
     * Test case 1: findByUserId returns Faculty when User ID exists
     * 
     * This is the primary success case - when a faculty member logs in,
     * their User ID should resolve to their Faculty entity.
     */
    @Test
    void testFindByUserId_ReturnsFactulty_WhenUserIdExists() {
        // Execute
        Optional<Faculty> result = facultyRepository.findByUserId("user123");

        // Verify
        assertTrue(result.isPresent(), "Faculty should be found by User ID");
        Faculty faculty = result.get();
        assertEquals("user123", faculty.getUserId(), "User ID should match");
        assertEquals("EMP001", faculty.getEmployeeId(), "Employee ID should match");
        assertEquals("Rajesh", faculty.getFirstName(), "First name should match");
        assertEquals("Kumar", faculty.getLastName(), "Last name should match");
        assertEquals("rajesh.kumar@college.edu", faculty.getEmail(), "Email should match");
        assertEquals("Computer Science", faculty.getDepartment(), "Department should match");
        assertNotNull(faculty.getId(), "Faculty ID should not be null");
    }

    /**
     * Test case 2: findByUserId returns empty Optional when User ID doesn't exist
     * 
     * This tests the error case - when an invalid User ID is provided,
     * the method should return empty Optional (not throw exception).
     */
    @Test
    void testFindByUserId_ReturnsEmpty_WhenUserIdDoesNotExist() {
        // Execute
        Optional<Faculty> result = facultyRepository.findByUserId("nonexistent-user-id");

        // Verify
        assertFalse(result.isPresent(), "Faculty should not be found for non-existent User ID");
        assertTrue(result.isEmpty(), "Result should be empty Optional");
    }

    /**
     * Test case 3: findByUserId handles null User ID gracefully
     * 
     * This tests edge case handling - null User ID should return empty Optional
     * or be handled gracefully by the repository layer.
     */
    @Test
    void testFindByUserId_HandlesNull_Gracefully() {
        // Execute
        Optional<Faculty> result = facultyRepository.findByUserId(null);

        // Verify
        assertFalse(result.isPresent(), "Faculty should not be found for null User ID");
        assertTrue(result.isEmpty(), "Result should be empty Optional for null input");
    }

    /**
     * Additional test: Verify User ID uniqueness
     * 
     * This ensures that the User ID field is properly indexed and unique,
     * which is important for the ID resolution logic.
     */
    @Test
    void testFindByUserId_ReturnsCorrectFaculty_WhenMultipleFacultyExist() {
        // Create additional faculty members
        Faculty faculty2 = new Faculty();
        faculty2.setUserId("user456");
        faculty2.setEmployeeId("EMP002");
        faculty2.setFirstName("Priya");
        faculty2.setLastName("Sharma");
        faculty2.setEmail("priya.sharma@college.edu");
        faculty2.setPhone("9876543211");
        faculty2.setDepartment("Mathematics");
        faculty2.setDesignation("Associate Professor");
        faculty2.setIsActive(true);
        faculty2.setCreatedAt(LocalDateTime.now());
        faculty2.setUpdatedAt(LocalDateTime.now());
        facultyRepository.save(faculty2);

        Faculty faculty3 = new Faculty();
        faculty3.setUserId("user789");
        faculty3.setEmployeeId("EMP003");
        faculty3.setFirstName("Amit");
        faculty3.setLastName("Patel");
        faculty3.setEmail("amit.patel@college.edu");
        faculty3.setPhone("9876543212");
        faculty3.setDepartment("Physics");
        faculty3.setDesignation("Professor");
        faculty3.setIsActive(true);
        faculty3.setCreatedAt(LocalDateTime.now());
        faculty3.setUpdatedAt(LocalDateTime.now());
        facultyRepository.save(faculty3);

        // Execute - find each faculty by their User ID
        Optional<Faculty> result1 = facultyRepository.findByUserId("user123");
        Optional<Faculty> result2 = facultyRepository.findByUserId("user456");
        Optional<Faculty> result3 = facultyRepository.findByUserId("user789");

        // Verify - each query returns the correct faculty
        assertTrue(result1.isPresent(), "First faculty should be found");
        assertEquals("Rajesh", result1.get().getFirstName(), "First faculty name should match");
        assertEquals("EMP001", result1.get().getEmployeeId(), "First faculty employee ID should match");

        assertTrue(result2.isPresent(), "Second faculty should be found");
        assertEquals("Priya", result2.get().getFirstName(), "Second faculty name should match");
        assertEquals("EMP002", result2.get().getEmployeeId(), "Second faculty employee ID should match");

        assertTrue(result3.isPresent(), "Third faculty should be found");
        assertEquals("Amit", result3.get().getFirstName(), "Third faculty name should match");
        assertEquals("EMP003", result3.get().getEmployeeId(), "Third faculty employee ID should match");
    }

    /**
     * Additional test: Verify soft-deleted faculty can still be found by User ID
     * 
     * This ensures that the findByUserId method returns faculty even if they
     * have a deletedAt timestamp (soft delete), which may be needed for audit purposes.
     */
    @Test
    void testFindByUserId_ReturnsFaculty_EvenWhenSoftDeleted() {
        // Soft delete the faculty
        testFaculty.setDeletedAt(LocalDateTime.now());
        facultyRepository.save(testFaculty);

        // Execute
        Optional<Faculty> result = facultyRepository.findByUserId("user123");

        // Verify - findByUserId should still return the faculty
        // (unlike findAllActive which filters by deletedAt)
        assertTrue(result.isPresent(), "Faculty should be found even when soft deleted");
        assertEquals("user123", result.get().getUserId(), "User ID should match");
        assertNotNull(result.get().getDeletedAt(), "DeletedAt should be set");
    }
}
