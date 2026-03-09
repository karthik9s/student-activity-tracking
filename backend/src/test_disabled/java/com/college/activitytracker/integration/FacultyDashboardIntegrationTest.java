package com.college.activitytracker.integration;

import com.college.activitytracker.dto.ClassAllocationDTO;
import com.college.activitytracker.model.*;
import com.college.activitytracker.repository.*;
import com.college.activitytracker.security.JwtTokenProvider;
import com.college.activitytracker.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full Faculty Dashboard Integration Tests
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3**
 * 
 * These tests verify the complete end-to-end flow from authentication to allocation display:
 * - Faculty logs in → dashboard loads → allocations displayed
 * - Faculty with multiple allocations sees all of them
 * - Faculty with no allocations sees empty state
 * 
 * Uses @SpringBootTest for full integration testing with real HTTP requests via TestRestTemplate
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacultyDashboardIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassAllocationRepository classAllocationRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String baseUrl;
    private Faculty testFaculty1;
    private Faculty testFaculty2;
    private Faculty testFaculty3;
    private User testUser1;
    private User testUser2;
    private User testUser3;
    private Course testCourse;
    private Subject testSubject1;
    private Subject testSubject2;
    private Subject testSubject3;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/faculty";
        cleanupTestData();

        // Create test course
        testCourse = new Course();
        testCourse.setName("Computer Science");
        testCourse.setCode("CS");
        testCourse.setDuration(4);
        testCourse.setCreatedAt(LocalDateTime.now());
        testCourse = courseRepository.save(testCourse);

        // Create test subjects
        testSubject1 = new Subject();
        testSubject1.setName("Data Structures");
        testSubject1.setCode("CS201");
        testSubject1.setCourseId(testCourse.getId());
        testSubject1.setSemester(3);
        testSubject1.setCredits(4);
        testSubject1.setCreatedAt(LocalDateTime.now());
        testSubject1 = subjectRepository.save(testSubject1);

        testSubject2 = new Subject();
        testSubject2.setName("Algorithms");
        testSubject2.setCode("CS202");
        testSubject2.setCourseId(testCourse.getId());
        testSubject2.setSemester(3);
        testSubject2.setCredits(4);
        testSubject2.setCreatedAt(LocalDateTime.now());
        testSubject2 = subjectRepository.save(testSubject2);

        testSubject3 = new Subject();
        testSubject3.setName("Database Systems");
        testSubject3.setCode("CS301");
        testSubject3.setCourseId(testCourse.getId());
        testSubject3.setSemester(5);
        testSubject3.setCredits(4);
        testSubject3.setCreatedAt(LocalDateTime.now());
        testSubject3 = subjectRepository.save(testSubject3);

        // Create test users and faculty members
        setupFacultyWithAllocations();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void setupFacultyWithAllocations() {
        // Faculty 1: Has 2 allocations
        testUser1 = new User();
        testUser1.setEmail("faculty1@test.com");
        testUser1.setPassword("hashedpassword");
        testUser1.setRole("FACULTY");
        testUser1.setIsActive(true);
        testUser1.setCreatedAt(LocalDateTime.now());
        testUser1 = userRepository.save(testUser1);

        testFaculty1 = new Faculty();
        testFaculty1.setUserId(testUser1.getId());
        testFaculty1.setEmployeeId("FAC001");
        testFaculty1.setFirstName("John");
        testFaculty1.setLastName("Doe");
        testFaculty1.setEmail("faculty1@test.com");
        testFaculty1.setPhone("1234567890");
        testFaculty1.setDepartment("Computer Science");
        testFaculty1.setDesignation("Assistant Professor");
        testFaculty1.setIsActive(true);
        testFaculty1.setCreatedAt(LocalDateTime.now());
        testFaculty1 = facultyRepository.save(testFaculty1);

        // Create 2 allocations for faculty 1
        ClassAllocation allocation1 = new ClassAllocation();
        allocation1.setFacultyId(testFaculty1.getId());
        allocation1.setSubjectId(testSubject1.getId());
        allocation1.setCourseId(testCourse.getId());
        allocation1.setYear(2);
        allocation1.setSection("A");
        allocation1.setAcademicYear("2024-2025");
        allocation1.setSemester("Fall");
        allocation1.setIsActive(true);
        allocation1.setCreatedAt(LocalDateTime.now());
        classAllocationRepository.save(allocation1);

        ClassAllocation allocation2 = new ClassAllocation();
        allocation2.setFacultyId(testFaculty1.getId());
        allocation2.setSubjectId(testSubject2.getId());
        allocation2.setCourseId(testCourse.getId());
        allocation2.setYear(2);
        allocation2.setSection("B");
        allocation2.setAcademicYear("2024-2025");
        allocation2.setSemester("Fall");
        allocation2.setIsActive(true);
        allocation2.setCreatedAt(LocalDateTime.now());
        classAllocationRepository.save(allocation2);

        // Faculty 2: Has 3 allocations
        testUser2 = new User();
        testUser2.setEmail("faculty2@test.com");
        testUser2.setPassword("hashedpassword");
        testUser2.setRole("FACULTY");
        testUser2.setIsActive(true);
        testUser2.setCreatedAt(LocalDateTime.now());
        testUser2 = userRepository.save(testUser2);

        testFaculty2 = new Faculty();
        testFaculty2.setUserId(testUser2.getId());
        testFaculty2.setEmployeeId("FAC002");
        testFaculty2.setFirstName("Jane");
        testFaculty2.setLastName("Smith");
        testFaculty2.setEmail("faculty2@test.com");
        testFaculty2.setPhone("0987654321");
        testFaculty2.setDepartment("Computer Science");
        testFaculty2.setDesignation("Associate Professor");
        testFaculty2.setIsActive(true);
        testFaculty2.setCreatedAt(LocalDateTime.now());
        testFaculty2 = facultyRepository.save(testFaculty2);

        // Create 3 allocations for faculty 2
        ClassAllocation allocation3 = new ClassAllocation();
        allocation3.setFacultyId(testFaculty2.getId());
        allocation3.setSubjectId(testSubject1.getId());
        allocation3.setCourseId(testCourse.getId());
        allocation3.setYear(2);
        allocation3.setSection("C");
        allocation3.setAcademicYear("2024-2025");
        allocation3.setSemester("Fall");
        allocation3.setIsActive(true);
        allocation3.setCreatedAt(LocalDateTime.now());
        classAllocationRepository.save(allocation3);

        ClassAllocation allocation4 = new ClassAllocation();
        allocation4.setFacultyId(testFaculty2.getId());
        allocation4.setSubjectId(testSubject2.getId());
        allocation4.setCourseId(testCourse.getId());
        allocation4.setYear(3);
        allocation4.setSection("A");
        allocation4.setAcademicYear("2024-2025");
        allocation4.setSemester("Fall");
        allocation4.setIsActive(true);
        allocation4.setCreatedAt(LocalDateTime.now());
        classAllocationRepository.save(allocation4);

        ClassAllocation allocation5 = new ClassAllocation();
        allocation5.setFacultyId(testFaculty2.getId());
        allocation5.setSubjectId(testSubject3.getId());
        allocation5.setCourseId(testCourse.getId());
        allocation5.setYear(3);
        allocation5.setSection("B");
        allocation5.setAcademicYear("2024-2025");
        allocation5.setSemester("Fall");
        allocation5.setIsActive(true);
        allocation5.setCreatedAt(LocalDateTime.now());
        classAllocationRepository.save(allocation5);

        // Faculty 3: Has no allocations
        testUser3 = new User();
        testUser3.setEmail("faculty3@test.com");
        testUser3.setPassword("hashedpassword");
        testUser3.setRole("FACULTY");
        testUser3.setIsActive(true);
        testUser3.setCreatedAt(LocalDateTime.now());
        testUser3 = userRepository.save(testUser3);

        testFaculty3 = new Faculty();
        testFaculty3.setUserId(testUser3.getId());
        testFaculty3.setEmployeeId("FAC003");
        testFaculty3.setFirstName("Bob");
        testFaculty3.setLastName("Johnson");
        testFaculty3.setEmail("faculty3@test.com");
        testFaculty3.setPhone("5555555555");
        testFaculty3.setDepartment("Computer Science");
        testFaculty3.setDesignation("Lecturer");
        testFaculty3.setIsActive(true);
        testFaculty3.setCreatedAt(LocalDateTime.now());
        testFaculty3 = facultyRepository.save(testFaculty3);
    }

    private void cleanupTestData() {
        if (classAllocationRepository != null) {
            classAllocationRepository.deleteAll();
        }
        if (facultyRepository != null) {
            facultyRepository.deleteAll();
        }
        if (userRepository != null) {
            userRepository.deleteAll();
        }
        if (subjectRepository != null) {
            subjectRepository.deleteAll();
        }
        if (courseRepository != null) {
            courseRepository.deleteAll();
        }
    }

    private String generateJwtToken(User user, String role) {
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                "ROLE_" + role,
                user.getIsActive()
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
        return jwtTokenProvider.generateAccessToken(authentication);
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    /**
     * Test Case 1: Faculty logs in → dashboard loads → allocations displayed
     * 
     * This test verifies the complete end-to-end flow:
     * 1. Faculty authenticates (JWT token generated with User ID)
     * 2. Faculty requests their allocations from the dashboard
     * 3. System resolves User ID → Faculty ID → Allocations
     * 4. Dashboard displays the correct allocations
     * 
     * Requirements: 2.1, 2.2, 2.3
     */
    @Test
    void testFacultyLoginAndDashboardLoadsAllocations() {
        // Arrange: Generate JWT token for faculty 1 (simulates login)
        String token = generateJwtToken(testUser1, "FACULTY");
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Act: Faculty requests their allocations (simulates dashboard loading)
        ResponseEntity<List<ClassAllocationDTO>> response = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Assert: Verify successful response with allocations
        assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "Should return 200 OK");
        assertNotNull(response.getBody(), 
                "Response body should not be null");
        
        List<ClassAllocationDTO> allocations = response.getBody();
        assertEquals(2, allocations.size(), 
                "Faculty 1 should see exactly 2 allocations");

        // Verify allocation details
        assertTrue(allocations.stream().anyMatch(a -> 
                testSubject1.getId().equals(a.getSubjectId()) && "A".equals(a.getSection())),
                "Should include Data Structures section A");
        assertTrue(allocations.stream().anyMatch(a -> 
                testSubject2.getId().equals(a.getSubjectId()) && "B".equals(a.getSection())),
                "Should include Algorithms section B");

        // Verify all allocations belong to the correct faculty
        assertTrue(allocations.stream().allMatch(a -> 
                testFaculty1.getId().equals(a.getFacultyId())),
                "All allocations should belong to faculty 1");
    }

    /**
     * Test Case 2: Faculty with multiple allocations sees all of them
     * 
     * This test verifies that faculty members with multiple class allocations
     * can see all their assignments correctly displayed in the dashboard.
     * 
     * Requirements: 2.2, 2.3
     */
    @Test
    void testFacultyWithMultipleAllocationsSeeAll() {
        // Arrange: Generate JWT token for faculty 2 (has 3 allocations)
        String token = generateJwtToken(testUser2, "FACULTY");
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Act: Faculty requests their allocations
        ResponseEntity<List<ClassAllocationDTO>> response = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Assert: Verify all allocations are returned
        assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "Should return 200 OK");
        assertNotNull(response.getBody(), 
                "Response body should not be null");
        
        List<ClassAllocationDTO> allocations = response.getBody();
        assertEquals(3, allocations.size(), 
                "Faculty 2 should see exactly 3 allocations");

        // Verify each allocation is present
        assertTrue(allocations.stream().anyMatch(a -> 
                testSubject1.getId().equals(a.getSubjectId()) && "C".equals(a.getSection())),
                "Should include Data Structures section C");
        assertTrue(allocations.stream().anyMatch(a -> 
                testSubject2.getId().equals(a.getSubjectId()) && "A".equals(a.getSection()) && a.getYear() == 3),
                "Should include Algorithms year 3 section A");
        assertTrue(allocations.stream().anyMatch(a -> 
                testSubject3.getId().equals(a.getSubjectId()) && "B".equals(a.getSection())),
                "Should include Database Systems section B");

        // Verify all allocations belong to the correct faculty
        assertTrue(allocations.stream().allMatch(a -> 
                testFaculty2.getId().equals(a.getFacultyId())),
                "All allocations should belong to faculty 2");

        // Verify allocations span different years and sections
        long distinctYears = allocations.stream().map(ClassAllocationDTO::getYear).distinct().count();
        assertEquals(2, distinctYears, 
                "Allocations should span 2 different years");
        
        long distinctSections = allocations.stream().map(ClassAllocationDTO::getSection).distinct().count();
        assertEquals(3, distinctSections, 
                "Allocations should span 3 different sections");
    }

    /**
     * Test Case 3: Faculty with no allocations sees empty state
     * 
     * This test verifies that faculty members without any class allocations
     * receive an empty list (not an error) and can display an appropriate
     * empty state in the dashboard.
     * 
     * Requirements: 2.3
     */
    @Test
    void testFacultyWithNoAllocationsSeeEmptyState() {
        // Arrange: Generate JWT token for faculty 3 (has no allocations)
        String token = generateJwtToken(testUser3, "FACULTY");
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Act: Faculty requests their allocations
        ResponseEntity<List<ClassAllocationDTO>> response = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Assert: Verify empty list is returned (not error)
        assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "Should return 200 OK even with no allocations");
        assertNotNull(response.getBody(), 
                "Response body should not be null");
        
        List<ClassAllocationDTO> allocations = response.getBody();
        assertTrue(allocations.isEmpty(), 
                "Faculty 3 should see empty allocations list");
        assertEquals(0, allocations.size(), 
                "Allocations list should have size 0");
    }

    /**
     * Test Case 4: Admin creates allocation → faculty refreshes dashboard → new allocation appears
     * 
     * This test verifies the end-to-end flow of admin allocation creation:
     * 1. Admin creates a new class allocation for a faculty member
     * 2. Faculty refreshes their dashboard (requests allocations again)
     * 3. The new allocation appears in the faculty's allocation list
     * 
     * Requirements: 3.1, 3.2
     */
    @Test
    void testAdminCreatesAllocationFacultySeeIt() {
        // Arrange: Generate tokens for admin and faculty
        User adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("hashedpassword");
        adminUser.setRole("ADMIN");
        adminUser.setIsActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);

        String adminToken = generateJwtToken(adminUser, "ADMIN");
        String facultyToken = generateJwtToken(testUser3, "FACULTY");

        // Verify faculty 3 initially has no allocations
        HttpHeaders facultyHeaders = createAuthHeaders(facultyToken);
        HttpEntity<String> facultyEntity = new HttpEntity<>(facultyHeaders);
        ResponseEntity<List<ClassAllocationDTO>> initialResponse = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                facultyEntity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );
        assertEquals(0, initialResponse.getBody().size(), 
                "Faculty 3 should initially have no allocations");

        // Act: Admin creates a new allocation for faculty 3
        ClassAllocationDTO newAllocation = new ClassAllocationDTO();
        newAllocation.setFacultyId(testFaculty3.getId());
        newAllocation.setSubjectId(testSubject1.getId());
        newAllocation.setCourseId(testCourse.getId());
        newAllocation.setYear(2);
        newAllocation.setSection("D");
        newAllocation.setAcademicYear("2024-2025");
        newAllocation.setSemester("Fall");
        newAllocation.setIsActive(true);

        HttpHeaders adminHeaders = createAuthHeaders(adminToken);
        HttpEntity<ClassAllocationDTO> adminEntity = new HttpEntity<>(newAllocation, adminHeaders);
        
        String adminBaseUrl = "http://localhost:" + port + "/api/admin";
        ResponseEntity<ClassAllocationDTO> createResponse = restTemplate.exchange(
                adminBaseUrl + "/allocations",
                HttpMethod.POST,
                adminEntity,
                ClassAllocationDTO.class
        );

        // Assert: Verify allocation was created successfully
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode(), 
                "Admin should successfully create allocation");
        assertNotNull(createResponse.getBody(), 
                "Created allocation should not be null");
        assertNotNull(createResponse.getBody().getId(), 
                "Created allocation should have an ID");

        // Act: Faculty refreshes dashboard (requests allocations again)
        ResponseEntity<List<ClassAllocationDTO>> refreshedResponse = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                facultyEntity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Assert: Verify new allocation appears in faculty dashboard
        assertEquals(HttpStatus.OK, refreshedResponse.getStatusCode(), 
                "Faculty should successfully retrieve allocations");
        assertNotNull(refreshedResponse.getBody(), 
                "Response body should not be null");
        
        List<ClassAllocationDTO> allocations = refreshedResponse.getBody();
        assertEquals(1, allocations.size(), 
                "Faculty 3 should now see 1 allocation");

        ClassAllocationDTO allocation = allocations.get(0);
        assertEquals(testFaculty3.getId(), allocation.getFacultyId(), 
                "Allocation should belong to faculty 3");
        assertEquals(testSubject1.getId(), allocation.getSubjectId(), 
                "Allocation should be for Data Structures");
        assertEquals("D", allocation.getSection(), 
                "Allocation should be for section D");
        assertEquals(2, allocation.getYear(), 
                "Allocation should be for year 2");
    }

    /**
     * Test Case 5: Admin updates allocation → faculty sees updated details
     * 
     * This test verifies the end-to-end flow of admin allocation updates:
     * 1. Admin updates an existing class allocation (changes section)
     * 2. Faculty refreshes their dashboard
     * 3. The updated allocation details appear correctly
     * 
     * Requirements: 3.1, 3.2
     */
    @Test
    void testAdminUpdatesAllocationFacultySeesUpdates() {
        // Arrange: Generate tokens for admin and faculty
        User adminUser = new User();
        adminUser.setEmail("admin2@test.com");
        adminUser.setPassword("hashedpassword");
        adminUser.setRole("ADMIN");
        adminUser.setIsActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);

        String adminToken = generateJwtToken(adminUser, "ADMIN");
        String facultyToken = generateJwtToken(testUser1, "FACULTY");

        // Verify faculty 1 initially has 2 allocations
        HttpHeaders facultyHeaders = createAuthHeaders(facultyToken);
        HttpEntity<String> facultyEntity = new HttpEntity<>(facultyHeaders);
        ResponseEntity<List<ClassAllocationDTO>> initialResponse = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                facultyEntity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );
        assertEquals(2, initialResponse.getBody().size(), 
                "Faculty 1 should initially have 2 allocations");

        // Find the allocation to update (Data Structures section A)
        ClassAllocationDTO allocationToUpdate = initialResponse.getBody().stream()
                .filter(a -> testSubject1.getId().equals(a.getSubjectId()) && "A".equals(a.getSection()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Should find Data Structures section A"));

        // Act: Admin updates the allocation (change section from A to Z)
        allocationToUpdate.setSection("Z");
        allocationToUpdate.setYear(3); // Also change year

        HttpHeaders adminHeaders = createAuthHeaders(adminToken);
        HttpEntity<ClassAllocationDTO> adminEntity = new HttpEntity<>(allocationToUpdate, adminHeaders);
        
        String adminBaseUrl = "http://localhost:" + port + "/api/admin";
        ResponseEntity<ClassAllocationDTO> updateResponse = restTemplate.exchange(
                adminBaseUrl + "/allocations/" + allocationToUpdate.getId(),
                HttpMethod.PUT,
                adminEntity,
                ClassAllocationDTO.class
        );

        // Assert: Verify allocation was updated successfully
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode(), 
                "Admin should successfully update allocation");
        assertNotNull(updateResponse.getBody(), 
                "Updated allocation should not be null");

        // Act: Faculty refreshes dashboard
        ResponseEntity<List<ClassAllocationDTO>> refreshedResponse = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                facultyEntity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Assert: Verify updated allocation appears with new details
        assertEquals(HttpStatus.OK, refreshedResponse.getStatusCode(), 
                "Faculty should successfully retrieve allocations");
        assertNotNull(refreshedResponse.getBody(), 
                "Response body should not be null");
        
        List<ClassAllocationDTO> allocations = refreshedResponse.getBody();
        assertEquals(2, allocations.size(), 
                "Faculty 1 should still have 2 allocations");

        // Find the updated allocation
        ClassAllocationDTO updatedAllocation = allocations.stream()
                .filter(a -> a.getId().equals(allocationToUpdate.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Should find updated allocation"));

        assertEquals("Z", updatedAllocation.getSection(), 
                "Section should be updated to Z");
        assertEquals(3, updatedAllocation.getYear(), 
                "Year should be updated to 3");
        assertEquals(testSubject1.getId(), updatedAllocation.getSubjectId(), 
                "Subject should remain Data Structures");
    }

    /**
     * Test Case 6: Admin deletes allocation → faculty no longer sees it
     * 
     * This test verifies the end-to-end flow of admin allocation deletion:
     * 1. Admin deletes an existing class allocation
     * 2. Faculty refreshes their dashboard
     * 3. The deleted allocation no longer appears in the list
     * 
     * Requirements: 3.1, 3.2
     */
    @Test
    void testAdminDeletesAllocationFacultyNoLongerSeesIt() {
        // Arrange: Generate tokens for admin and faculty
        User adminUser = new User();
        adminUser.setEmail("admin3@test.com");
        adminUser.setPassword("hashedpassword");
        adminUser.setRole("ADMIN");
        adminUser.setIsActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);

        String adminToken = generateJwtToken(adminUser, "ADMIN");
        String facultyToken = generateJwtToken(testUser2, "FACULTY");

        // Verify faculty 2 initially has 3 allocations
        HttpHeaders facultyHeaders = createAuthHeaders(facultyToken);
        HttpEntity<String> facultyEntity = new HttpEntity<>(facultyHeaders);
        ResponseEntity<List<ClassAllocationDTO>> initialResponse = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                facultyEntity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );
        assertEquals(3, initialResponse.getBody().size(), 
                "Faculty 2 should initially have 3 allocations");

        // Find the allocation to delete (Database Systems section B)
        ClassAllocationDTO allocationToDelete = initialResponse.getBody().stream()
                .filter(a -> testSubject3.getId().equals(a.getSubjectId()) && "B".equals(a.getSection()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Should find Database Systems section B"));

        String allocationIdToDelete = allocationToDelete.getId();

        // Act: Admin deletes the allocation
        HttpHeaders adminHeaders = createAuthHeaders(adminToken);
        HttpEntity<String> adminEntity = new HttpEntity<>(adminHeaders);
        
        String adminBaseUrl = "http://localhost:" + port + "/api/admin";
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                adminBaseUrl + "/allocations/" + allocationIdToDelete,
                HttpMethod.DELETE,
                adminEntity,
                Void.class
        );

        // Assert: Verify allocation was deleted successfully
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode(), 
                "Admin should successfully delete allocation");

        // Act: Faculty refreshes dashboard
        ResponseEntity<List<ClassAllocationDTO>> refreshedResponse = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                facultyEntity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Assert: Verify deleted allocation no longer appears
        assertEquals(HttpStatus.OK, refreshedResponse.getStatusCode(), 
                "Faculty should successfully retrieve allocations");
        assertNotNull(refreshedResponse.getBody(), 
                "Response body should not be null");
        
        List<ClassAllocationDTO> allocations = refreshedResponse.getBody();
        assertEquals(2, allocations.size(), 
                "Faculty 2 should now have 2 allocations (one deleted)");

        // Verify the deleted allocation is not in the list
        boolean deletedAllocationPresent = allocations.stream()
                .anyMatch(a -> allocationIdToDelete.equals(a.getId()));
        assertFalse(deletedAllocationPresent, 
                "Deleted allocation should not appear in faculty dashboard");

        // Verify the remaining allocations are correct
        assertTrue(allocations.stream().anyMatch(a -> 
                testSubject1.getId().equals(a.getSubjectId()) && "C".equals(a.getSection())),
                "Should still include Data Structures section C");
        assertTrue(allocations.stream().anyMatch(a -> 
                testSubject2.getId().equals(a.getSubjectId()) && "A".equals(a.getSection())),
                "Should still include Algorithms section A");
    }

    /**
     * Test Case 7: Faculty A's allocations don't appear in Faculty B's dashboard
     * 
     * This test verifies cross-faculty isolation - ensuring that when Faculty A
     * logs in and requests their allocations, they only see their own allocations
     * and not those belonging to Faculty B or any other faculty member.
     * 
     * Requirements: 2.3
     */
    @Test
    void testFacultyAAllocationsDoNotAppearInFacultyBDashboard() {
        // Arrange: Generate JWT tokens for both faculty members
        String faculty1Token = generateJwtToken(testUser1, "FACULTY");
        String faculty2Token = generateJwtToken(testUser2, "FACULTY");

        HttpHeaders faculty1Headers = createAuthHeaders(faculty1Token);
        HttpHeaders faculty2Headers = createAuthHeaders(faculty2Token);
        HttpEntity<String> faculty1Entity = new HttpEntity<>(faculty1Headers);
        HttpEntity<String> faculty2Entity = new HttpEntity<>(faculty2Headers);

        // Act: Faculty 1 requests their allocations
        ResponseEntity<List<ClassAllocationDTO>> faculty1Response = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                faculty1Entity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Act: Faculty 2 requests their allocations
        ResponseEntity<List<ClassAllocationDTO>> faculty2Response = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                faculty2Entity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Assert: Both requests should succeed
        assertEquals(HttpStatus.OK, faculty1Response.getStatusCode(), 
                "Faculty 1 should successfully retrieve allocations");
        assertEquals(HttpStatus.OK, faculty2Response.getStatusCode(), 
                "Faculty 2 should successfully retrieve allocations");

        List<ClassAllocationDTO> faculty1Allocations = faculty1Response.getBody();
        List<ClassAllocationDTO> faculty2Allocations = faculty2Response.getBody();

        assertNotNull(faculty1Allocations, "Faculty 1 allocations should not be null");
        assertNotNull(faculty2Allocations, "Faculty 2 allocations should not be null");

        // Assert: Faculty 1 should see only their own allocations (2 allocations)
        assertEquals(2, faculty1Allocations.size(), 
                "Faculty 1 should see exactly 2 allocations");
        assertTrue(faculty1Allocations.stream().allMatch(a -> 
                testFaculty1.getId().equals(a.getFacultyId())),
                "All of Faculty 1's allocations should belong to Faculty 1");

        // Assert: Faculty 2 should see only their own allocations (3 allocations)
        assertEquals(3, faculty2Allocations.size(), 
                "Faculty 2 should see exactly 3 allocations");
        assertTrue(faculty2Allocations.stream().allMatch(a -> 
                testFaculty2.getId().equals(a.getFacultyId())),
                "All of Faculty 2's allocations should belong to Faculty 2");

        // Assert: Faculty 1 should NOT see any of Faculty 2's allocations
        boolean faculty1HasFaculty2Allocations = faculty1Allocations.stream()
                .anyMatch(a -> testFaculty2.getId().equals(a.getFacultyId()));
        assertFalse(faculty1HasFaculty2Allocations, 
                "Faculty 1 should not see any of Faculty 2's allocations");

        // Assert: Faculty 2 should NOT see any of Faculty 1's allocations
        boolean faculty2HasFaculty1Allocations = faculty2Allocations.stream()
                .anyMatch(a -> testFaculty1.getId().equals(a.getFacultyId()));
        assertFalse(faculty2HasFaculty1Allocations, 
                "Faculty 2 should not see any of Faculty 1's allocations");

        // Assert: Verify specific sections are isolated
        // Faculty 1 has sections A and B
        assertTrue(faculty1Allocations.stream().anyMatch(a -> "A".equals(a.getSection())),
                "Faculty 1 should see section A");
        assertTrue(faculty1Allocations.stream().anyMatch(a -> "B".equals(a.getSection())),
                "Faculty 1 should see section B");
        assertFalse(faculty1Allocations.stream().anyMatch(a -> "C".equals(a.getSection())),
                "Faculty 1 should NOT see section C (belongs to Faculty 2)");

        // Faculty 2 has section C
        assertTrue(faculty2Allocations.stream().anyMatch(a -> "C".equals(a.getSection())),
                "Faculty 2 should see section C");
    }

    /**
     * Test Case 8: Multiple faculty members can retrieve allocations concurrently
     * 
     * This test verifies that multiple faculty members can request their allocations
     * simultaneously without interference. Each faculty member should receive their
     * own allocations correctly, even when requests are made concurrently.
     * 
     * Requirements: 2.3
     */
    @Test
    void testMultipleFacultyMembersRetrieveAllocationsConcurrently() throws InterruptedException {
        // Arrange: Generate JWT tokens for all three faculty members
        String faculty1Token = generateJwtToken(testUser1, "FACULTY");
        String faculty2Token = generateJwtToken(testUser2, "FACULTY");
        String faculty3Token = generateJwtToken(testUser3, "FACULTY");

        HttpHeaders faculty1Headers = createAuthHeaders(faculty1Token);
        HttpHeaders faculty2Headers = createAuthHeaders(faculty2Token);
        HttpHeaders faculty3Headers = createAuthHeaders(faculty3Token);

        // Create a thread-safe list to store results
        java.util.concurrent.CopyOnWriteArrayList<ResponseEntity<List<ClassAllocationDTO>>> responses = 
                new java.util.concurrent.CopyOnWriteArrayList<>();
        java.util.concurrent.CopyOnWriteArrayList<String> facultyIds = 
                new java.util.concurrent.CopyOnWriteArrayList<>();

        // Act: Create threads to make concurrent requests
        Thread thread1 = new Thread(() -> {
            HttpEntity<String> entity = new HttpEntity<>(faculty1Headers);
            ResponseEntity<List<ClassAllocationDTO>> response = restTemplate.exchange(
                    baseUrl + "/allocations",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
            );
            responses.add(response);
            facultyIds.add(testFaculty1.getId());
        });

        Thread thread2 = new Thread(() -> {
            HttpEntity<String> entity = new HttpEntity<>(faculty2Headers);
            ResponseEntity<List<ClassAllocationDTO>> response = restTemplate.exchange(
                    baseUrl + "/allocations",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
            );
            responses.add(response);
            facultyIds.add(testFaculty2.getId());
        });

        Thread thread3 = new Thread(() -> {
            HttpEntity<String> entity = new HttpEntity<>(faculty3Headers);
            ResponseEntity<List<ClassAllocationDTO>> response = restTemplate.exchange(
                    baseUrl + "/allocations",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
            );
            responses.add(response);
            facultyIds.add(testFaculty3.getId());
        });

        // Start all threads simultaneously
        thread1.start();
        thread2.start();
        thread3.start();

        // Wait for all threads to complete
        thread1.join();
        thread2.join();
        thread3.join();

        // Assert: All requests should succeed
        assertEquals(3, responses.size(), 
                "Should have 3 responses from concurrent requests");
        assertTrue(responses.stream().allMatch(r -> HttpStatus.OK.equals(r.getStatusCode())),
                "All concurrent requests should return 200 OK");

        // Assert: Each faculty should receive the correct number of allocations
        // We need to match responses to faculty members
        List<ClassAllocationDTO> allAllocations = new java.util.ArrayList<>();
        for (ResponseEntity<List<ClassAllocationDTO>> response : responses) {
            assertNotNull(response.getBody(), "Response body should not be null");
            allAllocations.addAll(response.getBody());
        }

        // Count allocations by faculty ID
        long faculty1Count = allAllocations.stream()
                .filter(a -> testFaculty1.getId().equals(a.getFacultyId()))
                .count();
        long faculty2Count = allAllocations.stream()
                .filter(a -> testFaculty2.getId().equals(a.getFacultyId()))
                .count();
        long faculty3Count = allAllocations.stream()
                .filter(a -> testFaculty3.getId().equals(a.getFacultyId()))
                .count();

        // Assert: Verify correct allocation counts
        assertEquals(2, faculty1Count, 
                "Faculty 1 should have 2 allocations in concurrent results");
        assertEquals(3, faculty2Count, 
                "Faculty 2 should have 3 allocations in concurrent results");
        assertEquals(0, faculty3Count, 
                "Faculty 3 should have 0 allocations in concurrent results");

        // Assert: Total allocations should be 5 (2 + 3 + 0)
        assertEquals(5, allAllocations.size(), 
                "Total allocations from concurrent requests should be 5");

        // Assert: No duplicate allocations (each allocation should appear exactly once)
        long distinctAllocationIds = allAllocations.stream()
                .map(ClassAllocationDTO::getId)
                .distinct()
                .count();
        assertEquals(5, distinctAllocationIds, 
                "All allocations should be distinct (no duplicates from concurrent access)");
    }

    /**
     * Test Case 9: User ID from Faculty A doesn't accidentally match Faculty B's Faculty ID
     * 
     * This test verifies that the ID resolution logic correctly distinguishes between
     * User IDs and Faculty IDs. It ensures that if Faculty A's User ID happens to match
     * Faculty B's Faculty ID (edge case), the system still correctly resolves to Faculty A's
     * allocations and not Faculty B's allocations.
     * 
     * Requirements: 2.3
     */
    @Test
    void testUserIdDoesNotAccidentallyMatchOtherFacultyId() {
        // Arrange: Create a special scenario where Faculty A's User ID could potentially
        // match Faculty B's Faculty ID (simulating an edge case)
        
        // First, get Faculty 2's Faculty ID
        String faculty2FacultyId = testFaculty2.getId();

        // Create a new user with User ID that matches Faculty 2's Faculty ID
        User specialUser = new User();
        specialUser.setId(faculty2FacultyId); // Set User ID to match Faculty 2's Faculty ID
        specialUser.setEmail("special@test.com");
        specialUser.setPassword("hashedpassword");
        specialUser.setRole("FACULTY");
        specialUser.setIsActive(true);
        specialUser.setCreatedAt(LocalDateTime.now());
        specialUser = userRepository.save(specialUser);

        // Create a new faculty member linked to this special user
        Faculty specialFaculty = new Faculty();
        specialFaculty.setUserId(specialUser.getId()); // User ID matches Faculty 2's Faculty ID
        specialFaculty.setEmployeeId("FAC999");
        specialFaculty.setFirstName("Special");
        specialFaculty.setLastName("Case");
        specialFaculty.setEmail("special@test.com");
        specialFaculty.setPhone("9999999999");
        specialFaculty.setDepartment("Computer Science");
        specialFaculty.setDesignation("Professor");
        specialFaculty.setIsActive(true);
        specialFaculty.setCreatedAt(LocalDateTime.now());
        specialFaculty = facultyRepository.save(specialFaculty);

        // Create an allocation for the special faculty
        ClassAllocation specialAllocation = new ClassAllocation();
        specialAllocation.setFacultyId(specialFaculty.getId());
        specialAllocation.setSubjectId(testSubject3.getId());
        specialAllocation.setCourseId(testCourse.getId());
        specialAllocation.setYear(4);
        specialAllocation.setSection("SPECIAL");
        specialAllocation.setAcademicYear("2024-2025");
        specialAllocation.setSemester("Fall");
        specialAllocation.setIsActive(true);
        specialAllocation.setCreatedAt(LocalDateTime.now());
        classAllocationRepository.save(specialAllocation);

        // Act: Special faculty logs in and requests allocations
        String specialToken = generateJwtToken(specialUser, "FACULTY");
        HttpHeaders specialHeaders = createAuthHeaders(specialToken);
        HttpEntity<String> specialEntity = new HttpEntity<>(specialHeaders);

        ResponseEntity<List<ClassAllocationDTO>> specialResponse = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                specialEntity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Assert: Special faculty should see only their own allocation
        assertEquals(HttpStatus.OK, specialResponse.getStatusCode(), 
                "Special faculty should successfully retrieve allocations");
        assertNotNull(specialResponse.getBody(), 
                "Response body should not be null");

        List<ClassAllocationDTO> specialAllocations = specialResponse.getBody();
        assertEquals(1, specialAllocations.size(), 
                "Special faculty should see exactly 1 allocation");

        ClassAllocationDTO allocation = specialAllocations.get(0);
        assertEquals(specialFaculty.getId(), allocation.getFacultyId(), 
                "Allocation should belong to special faculty, not Faculty 2");
        assertEquals("SPECIAL", allocation.getSection(), 
                "Allocation should be the special section");
        assertEquals(4, allocation.getYear(), 
                "Allocation should be for year 4");

        // Assert: Special faculty should NOT see Faculty 2's allocations
        // even though their User ID matches Faculty 2's Faculty ID
        assertFalse(specialAllocations.stream().anyMatch(a -> 
                testFaculty2.getId().equals(a.getFacultyId())),
                "Special faculty should not see Faculty 2's allocations");
        assertFalse(specialAllocations.stream().anyMatch(a -> 
                "C".equals(a.getSection()) || "A".equals(a.getSection()) || "B".equals(a.getSection())),
                "Special faculty should not see any of Faculty 2's sections");

        // Act: Verify Faculty 2 still sees their own allocations correctly
        String faculty2Token = generateJwtToken(testUser2, "FACULTY");
        HttpHeaders faculty2Headers = createAuthHeaders(faculty2Token);
        HttpEntity<String> faculty2Entity = new HttpEntity<>(faculty2Headers);

        ResponseEntity<List<ClassAllocationDTO>> faculty2Response = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                faculty2Entity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );

        // Assert: Faculty 2 should still see their 3 allocations correctly
        assertEquals(HttpStatus.OK, faculty2Response.getStatusCode(), 
                "Faculty 2 should successfully retrieve allocations");
        List<ClassAllocationDTO> faculty2Allocations = faculty2Response.getBody();
        assertNotNull(faculty2Allocations, "Faculty 2 allocations should not be null");
        assertEquals(3, faculty2Allocations.size(), 
                "Faculty 2 should still see exactly 3 allocations");
        assertTrue(faculty2Allocations.stream().allMatch(a -> 
                testFaculty2.getId().equals(a.getFacultyId())),
                "All of Faculty 2's allocations should belong to Faculty 2");
        assertFalse(faculty2Allocations.stream().anyMatch(a -> 
                "SPECIAL".equals(a.getSection())),
                "Faculty 2 should not see the special faculty's allocation");
    }

    /**
     * Test Case 10: Allocation retrieval with 1 allocation completes in < 100ms
     * 
     * This test verifies that retrieving a single allocation is performant
     * and completes within 100ms. This ensures the fix doesn't introduce
     * performance regressions for simple queries.
     * 
     * Requirements: 2.2, 2.3
     */
    @Test
    void testAllocationRetrievalWith1AllocationPerformance() {
        // Arrange: Use faculty 3 who has no allocations, create exactly 1
        ClassAllocation singleAllocation = new ClassAllocation();
        singleAllocation.setFacultyId(testFaculty3.getId());
        singleAllocation.setSubjectId(testSubject1.getId());
        singleAllocation.setCourseId(testCourse.getId());
        singleAllocation.setYear(1);
        singleAllocation.setSection("PERF1");
        singleAllocation.setAcademicYear("2024-2025");
        singleAllocation.setSemester("Fall");
        singleAllocation.setIsActive(true);
        singleAllocation.setCreatedAt(LocalDateTime.now());
        classAllocationRepository.save(singleAllocation);

        String token = generateJwtToken(testUser3, "FACULTY");
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Act: Measure time to retrieve allocations
        long startTime = System.currentTimeMillis();
        ResponseEntity<List<ClassAllocationDTO>> response = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert: Verify response is correct
        assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "Should return 200 OK");
        assertNotNull(response.getBody(), 
                "Response body should not be null");
        assertEquals(1, response.getBody().size(), 
                "Should return exactly 1 allocation");

        // Assert: Verify performance requirement
        assertTrue(duration < 100, 
                String.format("Allocation retrieval with 1 allocation should complete in < 100ms, took %dms", duration));
        
        System.out.println("Performance Test 1: Retrieved 1 allocation in " + duration + "ms");
    }

    /**
     * Test Case 11: Allocation retrieval with 50 allocations completes in < 500ms
     * 
     * This test verifies that retrieving a large number of allocations (50)
     * is still performant and completes within 500ms. This ensures the fix
     * scales well with larger datasets.
     * 
     * Requirements: 2.2, 2.3
     */
    @Test
    void testAllocationRetrievalWith50AllocationsPerformance() {
        // Arrange: Create a faculty member with 50 allocations
        User perfUser = new User();
        perfUser.setEmail("perftest@test.com");
        perfUser.setPassword("hashedpassword");
        perfUser.setRole("FACULTY");
        perfUser.setIsActive(true);
        perfUser.setCreatedAt(LocalDateTime.now());
        perfUser = userRepository.save(perfUser);

        Faculty perfFaculty = new Faculty();
        perfFaculty.setUserId(perfUser.getId());
        perfFaculty.setEmployeeId("PERFTEST");
        perfFaculty.setFirstName("Performance");
        perfFaculty.setLastName("Test");
        perfFaculty.setEmail("perftest@test.com");
        perfFaculty.setPhone("1111111111");
        perfFaculty.setDepartment("Computer Science");
        perfFaculty.setDesignation("Professor");
        perfFaculty.setIsActive(true);
        perfFaculty.setCreatedAt(LocalDateTime.now());
        perfFaculty = facultyRepository.save(perfFaculty);

        // Create 50 allocations
        for (int i = 0; i < 50; i++) {
            ClassAllocation allocation = new ClassAllocation();
            allocation.setFacultyId(perfFaculty.getId());
            // Rotate through the 3 test subjects
            if (i % 3 == 0) {
                allocation.setSubjectId(testSubject1.getId());
            } else if (i % 3 == 1) {
                allocation.setSubjectId(testSubject2.getId());
            } else {
                allocation.setSubjectId(testSubject3.getId());
            }
            allocation.setCourseId(testCourse.getId());
            allocation.setYear((i % 4) + 1); // Years 1-4
            allocation.setSection("PERF" + i);
            allocation.setAcademicYear("2024-2025");
            allocation.setSemester("Fall");
            allocation.setIsActive(true);
            allocation.setCreatedAt(LocalDateTime.now());
            classAllocationRepository.save(allocation);
        }

        String token = generateJwtToken(perfUser, "FACULTY");
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Act: Measure time to retrieve allocations
        long startTime = System.currentTimeMillis();
        ResponseEntity<List<ClassAllocationDTO>> response = restTemplate.exchange(
                baseUrl + "/allocations",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
        );
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert: Verify response is correct
        assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "Should return 200 OK");
        assertNotNull(response.getBody(), 
                "Response body should not be null");
        assertEquals(50, response.getBody().size(), 
                "Should return exactly 50 allocations");

        // Verify all allocations belong to the performance test faculty
        assertTrue(response.getBody().stream().allMatch(a -> 
                perfFaculty.getId().equals(a.getFacultyId())),
                "All allocations should belong to performance test faculty");

        // Assert: Verify performance requirement
        assertTrue(duration < 500, 
                String.format("Allocation retrieval with 50 allocations should complete in < 500ms, took %dms", duration));
        
        System.out.println("Performance Test 2: Retrieved 50 allocations in " + duration + "ms");
    }

    /**
     * Test Case 12: Concurrent requests from 10 faculty members complete successfully
     * 
     * This test verifies that the system can handle concurrent load from multiple
     * faculty members requesting their allocations simultaneously. All requests
     * should complete successfully without errors or timeouts.
     * 
     * Requirements: 2.2, 2.3
     */
    @Test
    void testConcurrentRequestsFrom10FacultyMembers() throws InterruptedException {
        // Arrange: Create 10 faculty members, each with varying numbers of allocations
        List<User> concurrentUsers = new java.util.ArrayList<>();
        List<Faculty> concurrentFaculty = new java.util.ArrayList<>();
        List<Integer> expectedAllocationCounts = new java.util.ArrayList<>();

        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail("concurrent" + i + "@test.com");
            user.setPassword("hashedpassword");
            user.setRole("FACULTY");
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user = userRepository.save(user);
            concurrentUsers.add(user);

            Faculty faculty = new Faculty();
            faculty.setUserId(user.getId());
            faculty.setEmployeeId("CONC" + i);
            faculty.setFirstName("Concurrent" + i);
            faculty.setLastName("Faculty");
            faculty.setEmail("concurrent" + i + "@test.com");
            faculty.setPhone("222222222" + i);
            faculty.setDepartment("Computer Science");
            faculty.setDesignation("Professor");
            faculty.setIsActive(true);
            faculty.setCreatedAt(LocalDateTime.now());
            faculty = facultyRepository.save(faculty);
            concurrentFaculty.add(faculty);

            // Create varying numbers of allocations (i+1 allocations for faculty i)
            int allocationCount = i + 1;
            expectedAllocationCounts.add(allocationCount);
            
            for (int j = 0; j < allocationCount; j++) {
                ClassAllocation allocation = new ClassAllocation();
                allocation.setFacultyId(faculty.getId());
                allocation.setSubjectId(testSubject1.getId());
                allocation.setCourseId(testCourse.getId());
                allocation.setYear((j % 4) + 1);
                allocation.setSection("CONC" + i + "_" + j);
                allocation.setAcademicYear("2024-2025");
                allocation.setSemester("Fall");
                allocation.setIsActive(true);
                allocation.setCreatedAt(LocalDateTime.now());
                classAllocationRepository.save(allocation);
            }
        }

        // Create thread-safe collections to store results
        java.util.concurrent.ConcurrentHashMap<Integer, ResponseEntity<List<ClassAllocationDTO>>> responses = 
                new java.util.concurrent.ConcurrentHashMap<>();
        java.util.concurrent.ConcurrentHashMap<Integer, Long> durations = 
                new java.util.concurrent.ConcurrentHashMap<>();
        java.util.concurrent.atomic.AtomicInteger successCount = 
                new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger errorCount = 
                new java.util.concurrent.atomic.AtomicInteger(0);

        // Act: Create 10 threads to make concurrent requests
        List<Thread> threads = new java.util.ArrayList<>();
        long overallStartTime = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            final int index = i;
            Thread thread = new Thread(() -> {
                try {
                    String token = generateJwtToken(concurrentUsers.get(index), "FACULTY");
                    HttpHeaders headers = createAuthHeaders(token);
                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    long startTime = System.currentTimeMillis();
                    ResponseEntity<List<ClassAllocationDTO>> response = restTemplate.exchange(
                            baseUrl + "/allocations",
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<List<ClassAllocationDTO>>() {}
                    );
                    long endTime = System.currentTimeMillis();
                    
                    responses.put(index, response);
                    durations.put(index, endTime - startTime);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Error in concurrent request " + index + ": " + e.getMessage());
                }
            });
            threads.add(thread);
        }

        // Start all threads simultaneously
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        long overallEndTime = System.currentTimeMillis();
        long overallDuration = overallEndTime - overallStartTime;

        // Assert: All requests should succeed
        assertEquals(10, successCount.get(), 
                "All 10 concurrent requests should succeed");
        assertEquals(0, errorCount.get(), 
                "No concurrent requests should fail");
        assertEquals(10, responses.size(), 
                "Should have 10 responses from concurrent requests");

        // Assert: Each faculty should receive the correct number of allocations
        for (int i = 0; i < 10; i++) {
            ResponseEntity<List<ClassAllocationDTO>> response = responses.get(i);
            assertNotNull(response, "Response " + i + " should not be null");
            assertEquals(HttpStatus.OK, response.getStatusCode(), 
                    "Response " + i + " should return 200 OK");
            assertNotNull(response.getBody(), 
                    "Response body " + i + " should not be null");
            
            int expectedCount = expectedAllocationCounts.get(i);
            assertEquals(expectedCount, response.getBody().size(), 
                    String.format("Faculty %d should see exactly %d allocations", i, expectedCount));

            // Verify all allocations belong to the correct faculty
            String facultyId = concurrentFaculty.get(i).getId();
            assertTrue(response.getBody().stream().allMatch(a -> 
                    facultyId.equals(a.getFacultyId())),
                    "All allocations for faculty " + i + " should belong to that faculty");
        }

        // Assert: Verify individual request durations are reasonable
        for (int i = 0; i < 10; i++) {
            Long duration = durations.get(i);
            assertNotNull(duration, "Duration " + i + " should not be null");
            assertTrue(duration < 1000, 
                    String.format("Concurrent request %d should complete in < 1000ms, took %dms", i, duration));
        }

        // Print performance summary
        System.out.println("Performance Test 3: Concurrent Requests Summary");
        System.out.println("  Total time for 10 concurrent requests: " + overallDuration + "ms");
        System.out.println("  Successful requests: " + successCount.get());
        System.out.println("  Failed requests: " + errorCount.get());
        
        double avgDuration = durations.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        System.out.println("  Average request duration: " + String.format("%.2f", avgDuration) + "ms");
        
        long maxDuration = durations.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        System.out.println("  Maximum request duration: " + maxDuration + "ms");
        
        long minDuration = durations.values().stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0L);
        System.out.println("  Minimum request duration: " + minDuration + "ms");
    }
}
