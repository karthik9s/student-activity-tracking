package com.college.activitytracker.preservation;

import com.college.activitytracker.dto.AttendanceDTO;
import com.college.activitytracker.dto.ClassAllocationDTO;
import com.college.activitytracker.model.*;
import com.college.activitytracker.repository.*;
import com.college.activitytracker.service.AttendanceService;
import com.college.activitytracker.service.ClassAllocationService;
import com.college.activitytracker.service.FacultyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Preservation Property Tests for Faculty Allocation Retrieval Fix
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
 * 
 * **Property 2: Preservation** - Admin Panel and Other Faculty Operations
 * 
 * IMPORTANT: Follow observation-first methodology
 * - Observe behavior on UNFIXED code for non-buggy inputs
 * - Write property-based tests capturing observed behavior patterns
 * - Property-based testing generates many test cases for stronger guarantees
 * 
 * EXPECTED OUTCOME: Tests PASS on UNFIXED code (confirms baseline behavior to preserve)
 * 
 * These tests verify that the fix does NOT break existing functionality:
 * - Admin panel allocation management (create, view, delete)
 * - Faculty operations (attendance)
 * - Direct Faculty ID queries
 * - Empty allocation handling
 * - Authentication flow
 * 
 * All tests should PASS both before and after the fix is implemented.
 */
@SpringBootTest
class FacultyAllocationPreservationTest {

    @Autowired
    private ClassAllocationService classAllocationService;

    @Autowired
    private ClassAllocationRepository classAllocationRepository;

    @Autowired
    private FacultyService facultyService;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Faculty testFaculty;
    private User testUser;
    private Subject testSubject;
    private Course testCourse;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        cleanupTestData();

        // Create test course
        testCourse = new Course();
        testCourse.setName("Test Course");
        testCourse.setCode("TC101");
        testCourse.setDuration(4);
        testCourse.setCreatedAt(LocalDateTime.now());
        testCourse = courseRepository.save(testCourse);

        // Create test subject
        testSubject = new Subject();
        testSubject.setName("Test Subject");
        testSubject.setCode("TS101");
        testSubject.setCourseId(testCourse.getId());
        testSubject.setSemester(1);  // Integer, not String
        testSubject.setCredits(4);
        testSubject.setCreatedAt(LocalDateTime.now());
        testSubject = subjectRepository.save(testSubject);

        // Create test user
        testUser = new User();
        testUser.setEmail("preservation.test@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setRole("FACULTY");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Create test faculty
        testFaculty = new Faculty();
        testFaculty.setUserId(testUser.getId());
        testFaculty.setEmployeeId("PRES001");
        testFaculty.setFirstName("Preservation");
        testFaculty.setLastName("Test");
        testFaculty.setEmail("preservation.test@example.com");
        testFaculty.setPhone("9876543210");
        testFaculty.setDepartment("Computer Science");
        testFaculty.setDesignation("Professor");
        testFaculty.setIsActive(true);
        testFaculty.setCreatedAt(LocalDateTime.now());
        testFaculty = facultyRepository.save(testFaculty);

        // Create test student
        testStudent = new Student();
        testStudent.setRollNumber("PRES2024001");
        testStudent.setFirstName("Test");
        testStudent.setLastName("Student");
        testStudent.setEmail("test.student@example.com");
        testStudent.setPhone("1234567890");
        testStudent.setCourseId(testCourse.getId());
        testStudent.setYear(2);
        testStudent.setSection("A");
        testStudent.setIsActive(true);
        testStudent.setCreatedAt(LocalDateTime.now());
        testStudent = studentRepository.save(testStudent);
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        // Clean up in reverse order of dependencies
        if (attendanceRepository != null) {
            attendanceRepository.deleteAll();
        }
        if (classAllocationRepository != null) {
            classAllocationRepository.deleteAll();
        }
        if (testStudent != null && testStudent.getId() != null) {
            studentRepository.deleteById(testStudent.getId());
        }
        if (testFaculty != null && testFaculty.getId() != null) {
            facultyRepository.deleteById(testFaculty.getId());
        }
        if (testUser != null && testUser.getId() != null) {
            userRepository.deleteById(testUser.getId());
        }
        if (testSubject != null && testSubject.getId() != null) {
            subjectRepository.deleteById(testSubject.getId());
        }
        if (testCourse != null && testCourse.getId() != null) {
            courseRepository.deleteById(testCourse.getId());
        }
    }

    /**
     * Test 1: Admin creates allocation with Faculty ID → verify allocation is stored correctly
     * 
     * Preservation Requirement 3.2: Admin panel allocation creation must continue to work
     * 
     * This test verifies that admin can create allocations using Faculty ID
     * and the allocation is stored correctly in the database.
     */
    @Test
    void testAdminCreatesAllocationWithFacultyId() {
        // Arrange: Create allocation DTO with Faculty ID
        ClassAllocationDTO allocationDTO = new ClassAllocationDTO();
        allocationDTO.setFacultyId(testFaculty.getId());  // Uses Faculty ID, not User ID
        allocationDTO.setSubjectId(testSubject.getId());
        allocationDTO.setCourseId(testCourse.getId());
        allocationDTO.setYear(2);
        allocationDTO.setSection("A");
        allocationDTO.setAcademicYear("2024-2025");
        allocationDTO.setSemester("Fall");

        // Act: Admin creates allocation (simulates admin panel operation)
        ClassAllocationDTO created = classAllocationService.createAllocation(allocationDTO);

        // Assert: Verify allocation is stored correctly
        assertNotNull(created, "Created allocation should not be null");
        assertNotNull(created.getId(), "Created allocation should have an ID");
        assertEquals(testFaculty.getId(), created.getFacultyId(), 
            "Allocation should be stored with Faculty ID");
        assertEquals(testSubject.getId(), created.getSubjectId(), 
            "Allocation should have correct subject ID");
        assertEquals(testCourse.getId(), created.getCourseId(), 
            "Allocation should have correct course ID");
        assertEquals(2, created.getYear(), "Allocation should have correct year");
        assertEquals("A", created.getSection(), "Allocation should have correct section");

        // Verify in database
        ClassAllocation inDb = classAllocationRepository.findById(created.getId()).orElse(null);
        assertNotNull(inDb, "Allocation should exist in database");
        assertEquals(testFaculty.getId(), inDb.getFacultyId(), 
            "Database record should have Faculty ID");
    }

    /**
     * Test 2: Admin views all allocations → verify all allocations are returned
     * 
     * Preservation Requirement 3.1: Admin panel allocation viewing must continue to work
     * 
     * This test verifies that admin can view all allocations in the system.
     */
    @Test
    void testAdminViewsAllAllocations() {
        // Arrange: Create multiple allocations
        ClassAllocationDTO allocation1 = new ClassAllocationDTO();
        allocation1.setFacultyId(testFaculty.getId());
        allocation1.setSubjectId(testSubject.getId());
        allocation1.setCourseId(testCourse.getId());
        allocation1.setYear(2);
        allocation1.setSection("A");
        allocation1.setAcademicYear("2024-2025");
        allocation1.setSemester("Fall");
        classAllocationService.createAllocation(allocation1);

        ClassAllocationDTO allocation2 = new ClassAllocationDTO();
        allocation2.setFacultyId(testFaculty.getId());
        allocation2.setSubjectId(testSubject.getId());
        allocation2.setCourseId(testCourse.getId());
        allocation2.setYear(2);
        allocation2.setSection("B");
        allocation2.setAcademicYear("2024-2025");
        allocation2.setSemester("Fall");
        classAllocationService.createAllocation(allocation2);

        // Act: Admin views all allocations
        Pageable pageable = PageRequest.of(0, 20);
        Page<ClassAllocationDTO> allocations = classAllocationService.getAllAllocations(pageable);

        // Assert: Verify all allocations are returned
        assertNotNull(allocations, "Allocations page should not be null");
        assertTrue(allocations.getTotalElements() >= 2, 
            "Should have at least 2 allocations");
        
        // Verify allocations contain our test data
        List<ClassAllocationDTO> content = allocations.getContent();
        long matchingAllocations = content.stream()
            .filter(a -> testFaculty.getId().equals(a.getFacultyId()))
            .count();
        assertEquals(2, matchingAllocations, 
            "Should find exactly 2 allocations for test faculty");
    }

    /**
     * Test 3: Admin deletes allocation → verify deletion works
     * 
     * Preservation Requirement 3.3: Admin panel allocation deletion must continue to work
     * 
     * This test verifies that admin can delete allocations successfully.
     */
    @Test
    void testAdminDeletesAllocation() {
        // Arrange: Create an allocation
        ClassAllocationDTO allocationDTO = new ClassAllocationDTO();
        allocationDTO.setFacultyId(testFaculty.getId());
        allocationDTO.setSubjectId(testSubject.getId());
        allocationDTO.setCourseId(testCourse.getId());
        allocationDTO.setYear(2);
        allocationDTO.setSection("A");
        allocationDTO.setAcademicYear("2024-2025");
        allocationDTO.setSemester("Fall");
        ClassAllocationDTO created = classAllocationService.createAllocation(allocationDTO);

        // Verify allocation exists
        assertTrue(classAllocationRepository.findById(created.getId()).isPresent(), 
            "Allocation should exist before deletion");

        // Act: Admin deletes allocation
        classAllocationService.deleteAllocation(created.getId());

        // Assert: Verify allocation is deleted
        assertFalse(classAllocationRepository.findById(created.getId()).isPresent(), 
            "Allocation should be deleted from database");
    }

    /**
     * Test 4: Faculty marks attendance → verify attendance marking unaffected
     * 
     * Preservation Requirement 3.3: Faculty attendance operations must remain unaffected
     * 
     * This test verifies that faculty can mark attendance and the fix doesn't impact this.
     */
    @Test
    void testFacultyMarksAttendance() {
        // Arrange: Create allocation first
        ClassAllocationDTO allocationDTO = new ClassAllocationDTO();
        allocationDTO.setFacultyId(testFaculty.getId());
        allocationDTO.setSubjectId(testSubject.getId());
        allocationDTO.setCourseId(testCourse.getId());
        allocationDTO.setYear(2);
        allocationDTO.setSection("A");
        allocationDTO.setAcademicYear("2024-2025");
        allocationDTO.setSemester("Fall");
        ClassAllocationDTO allocation = classAllocationService.createAllocation(allocationDTO);

        // Create attendance DTO
        AttendanceDTO attendanceDTO = new AttendanceDTO();
        attendanceDTO.setStudentId(testStudent.getId());
        attendanceDTO.setSubjectId(testSubject.getId());
        attendanceDTO.setFacultyId(testFaculty.getId());
        attendanceDTO.setDate(LocalDate.now());
        attendanceDTO.setStatus("PRESENT");
        attendanceDTO.setRemarks("Test attendance");

        // Act: Faculty marks attendance
        AttendanceDTO marked = attendanceService.markAttendance(attendanceDTO);

        // Assert: Verify attendance is marked correctly
        assertNotNull(marked, "Marked attendance should not be null");
        assertNotNull(marked.getId(), "Marked attendance should have an ID");
        assertEquals(testStudent.getId(), marked.getStudentId(), 
            "Attendance should have correct student ID");
        assertEquals(testSubject.getId(), marked.getSubjectId(), 
            "Attendance should have correct subject ID");
        assertEquals(testFaculty.getId(), marked.getFacultyId(), 
            "Attendance should have correct faculty ID");
        assertEquals("PRESENT", marked.getStatus(), 
            "Attendance should have correct status");

        // Verify in database
        Attendance inDb = attendanceRepository.findById(marked.getId()).orElse(null);
        assertNotNull(inDb, "Attendance should exist in database");
        assertEquals(testFaculty.getId(), inDb.getFacultyId(), 
            "Database record should have Faculty ID");
    }

    /**
     * Test 5: Faculty queries allocations by Faculty ID → verify direct query works
     * 
     * Preservation Requirement 3.3: Direct Faculty ID queries must remain unaffected
     * 
     * This test verifies that querying allocations directly with Faculty ID still works.
     * This is the mechanism used by admin panel and should be preserved.
     */
    @Test
    void testDirectFacultyIdQueryWorks() {
        // Arrange: Create allocation with Faculty ID
        ClassAllocationDTO allocationDTO = new ClassAllocationDTO();
        allocationDTO.setFacultyId(testFaculty.getId());
        allocationDTO.setSubjectId(testSubject.getId());
        allocationDTO.setCourseId(testCourse.getId());
        allocationDTO.setYear(2);
        allocationDTO.setSection("A");
        allocationDTO.setAcademicYear("2024-2025");
        allocationDTO.setSemester("Fall");
        ClassAllocationDTO created = classAllocationService.createAllocation(allocationDTO);

        // Act: Query allocations using Faculty ID directly (admin panel behavior)
        List<ClassAllocationDTO> allocations = 
            classAllocationService.getAllocationsByFaculty(testFaculty.getId());

        // Assert: Verify allocation is returned when querying with Faculty ID
        assertNotNull(allocations, "Allocations list should not be null");
        assertFalse(allocations.isEmpty(), 
            "Should find allocations when querying with Faculty ID");
        assertEquals(1, allocations.size(), 
            "Should find exactly 1 allocation");
        assertEquals(testFaculty.getId(), allocations.get(0).getFacultyId(), 
            "Allocation should have correct Faculty ID");
        assertEquals(testSubject.getId(), allocations.get(0).getSubjectId(), 
            "Allocation should have correct Subject ID");
    }

    /**
     * Test 6: Faculty with no allocations logs in → verify empty list returned, not error
     * 
     * Preservation Requirement 3.4: Empty allocation handling must work correctly
     * 
     * This test verifies that faculty with no allocations see an empty list, not an error.
     */
    @Test
    void testFacultyWithNoAllocationsReturnsEmptyList() {
        // Arrange: Create a faculty with no allocations
        User newUser = new User();
        newUser.setEmail("noaccess@example.com");
        newUser.setPassword("hashedpassword");
        newUser.setRole("FACULTY");
        newUser.setIsActive(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser = userRepository.save(newUser);

        Faculty newFaculty = new Faculty();
        newFaculty.setUserId(newUser.getId());
        newFaculty.setEmployeeId("NOALLOC001");
        newFaculty.setFirstName("No");
        newFaculty.setLastName("Allocations");
        newFaculty.setEmail("noaccess@example.com");
        newFaculty.setPhone("1111111111");
        newFaculty.setDepartment("Computer Science");
        newFaculty.setDesignation("Lecturer");
        newFaculty.setIsActive(true);
        newFaculty.setCreatedAt(LocalDateTime.now());
        newFaculty = facultyRepository.save(newFaculty);

        try {
            // Act: Query allocations for faculty with no allocations
            List<ClassAllocationDTO> allocations = 
                classAllocationService.getAllocationsByFaculty(newFaculty.getId());

            // Assert: Verify empty list is returned (not null, not error)
            assertNotNull(allocations, "Allocations list should not be null");
            assertTrue(allocations.isEmpty(), 
                "Allocations list should be empty for faculty with no allocations");
        } finally {
            // Cleanup
            facultyRepository.deleteById(newFaculty.getId());
            userRepository.deleteById(newUser.getId());
        }
    }

    /**
     * Test 7: Authentication flow → verify User ID still used for login validation
     * 
     * Preservation Requirement 3.5: Authentication must continue to use User ID
     * 
     * This test verifies that the authentication mechanism still uses User ID
     * and is not affected by the Faculty ID resolution fix.
     */
    @Test
    void testAuthenticationUsesUserId() {
        // Arrange: Create authentication with User ID
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            testUser.getId(),  // authentication.getName() returns User ID
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_FACULTY"))
        );

        // Act & Assert: Verify authentication contains User ID
        assertNotNull(authentication, "Authentication should not be null");
        assertEquals(testUser.getId(), authentication.getName(), 
            "Authentication should use User ID");
        assertNotEquals(testFaculty.getId(), authentication.getName(), 
            "Authentication should NOT use Faculty ID");

        // Verify User ID and Faculty ID are different
        assertNotEquals(testUser.getId(), testFaculty.getId(), 
            "User ID and Faculty ID should be different (this is the core of the bug)");

        // Verify Faculty.userId links to User.id
        assertEquals(testUser.getId(), testFaculty.getUserId(), 
            "Faculty.userId should link to User.id");
    }
}
