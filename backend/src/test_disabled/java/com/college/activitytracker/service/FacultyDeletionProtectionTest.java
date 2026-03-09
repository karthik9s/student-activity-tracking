package com.college.activitytracker.service;

import com.college.activitytracker.model.ClassAllocation;
import com.college.activitytracker.model.Faculty;
import com.college.activitytracker.repository.ClassAllocationRepository;
import com.college.activitytracker.repository.FacultyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test_activity_tracker"
})
class FacultyDeletionProtectionTest {

    @Autowired
    private FacultyService facultyService;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private ClassAllocationRepository classAllocationRepository;

    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        // Clean up test data
        classAllocationRepository.deleteAll();
        facultyRepository.deleteAll();

        // Create test faculty
        testFaculty = Faculty.builder()
                .employeeId("TEST001")
                .firstName("Test")
                .lastName("Faculty")
                .email("test@example.com")
                .phone("1234567890")
                .department("Computer Science")
                .designation("Professor")
                .isActive(true)
                .build();
        testFaculty = facultyRepository.save(testFaculty);
    }

    @Test
    void testDeleteFacultyWithActiveAllocations_ShouldThrowException() {
        // Create an active class allocation for the faculty
        ClassAllocation allocation = ClassAllocation.builder()
                .facultyId(testFaculty.getId())
                .subjectId("subject123")
                .courseId("course123")
                .year(1)
                .section("A")
                .isActive(true)
                .build();
        classAllocationRepository.save(allocation);

        // Attempt to delete faculty with active allocation
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facultyService.deleteFaculty(testFaculty.getId());
        });

        assertEquals("Cannot delete faculty with active class allocations", exception.getMessage());

        // Verify faculty was not deleted
        Faculty faculty = facultyRepository.findById(testFaculty.getId()).orElse(null);
        assertNotNull(faculty);
        assertNull(faculty.getDeletedAt());
        assertTrue(faculty.getIsActive());
    }

    @Test
    void testDeleteFacultyWithInactiveAllocations_ShouldSucceed() {
        // Create an inactive class allocation for the faculty
        ClassAllocation allocation = ClassAllocation.builder()
                .facultyId(testFaculty.getId())
                .subjectId("subject123")
                .courseId("course123")
                .year(1)
                .section("A")
                .isActive(false)
                .build();
        classAllocationRepository.save(allocation);

        // Delete faculty should succeed
        assertDoesNotThrow(() -> {
            facultyService.deleteFaculty(testFaculty.getId());
        });

        // Verify faculty was soft deleted
        Faculty faculty = facultyRepository.findById(testFaculty.getId()).orElse(null);
        assertNotNull(faculty);
        assertNotNull(faculty.getDeletedAt());
        assertFalse(faculty.getIsActive());
    }

    @Test
    void testDeleteFacultyWithNoAllocations_ShouldSucceed() {
        // Delete faculty with no allocations should succeed
        assertDoesNotThrow(() -> {
            facultyService.deleteFaculty(testFaculty.getId());
        });

        // Verify faculty was soft deleted
        Faculty faculty = facultyRepository.findById(testFaculty.getId()).orElse(null);
        assertNotNull(faculty);
        assertNotNull(faculty.getDeletedAt());
        assertFalse(faculty.getIsActive());
    }
}
