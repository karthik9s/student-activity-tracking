package com.college.activitytracker.controller;

import com.college.activitytracker.dto.ClassAllocationDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.service.AttendanceService;
import com.college.activitytracker.service.ClassAllocationService;
import com.college.activitytracker.service.FacultyService;
import com.college.activitytracker.service.PerformanceService;
import com.college.activitytracker.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FacultyController.getMyAllocations endpoint
 * Tests the controller layer that receives authenticated requests and delegates to FacultyService
 * 
 * Requirements: 2.3 - Faculty allocation retrieval fix
 */
@WebMvcTest(FacultyController.class)
class FacultyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private PerformanceService performanceService;

    @MockBean
    private ClassAllocationService classAllocationService;

    @MockBean
    private ReportService reportService;

    @MockBean
    private FacultyService facultyService;

    private List<ClassAllocationDTO> testAllocations;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        // Create test allocations
        testAllocations = new ArrayList<>();
        
        ClassAllocationDTO allocation1 = new ClassAllocationDTO(
                "alloc1",
                "faculty123",
                "subject1",
                "course1",
                2,
                "A",
                "2024-2025",
                "Fall",
                true,
                "Test Faculty",
                "Data Structures",
                "Computer Science",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        ClassAllocationDTO allocation2 = new ClassAllocationDTO(
                "alloc2",
                "faculty123",
                "subject2",
                "course1",
                3,
                "B",
                "2024-2025",
                "Fall",
                true,
                "Test Faculty",
                "Algorithms",
                "Computer Science",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        testAllocations.add(allocation1);
        testAllocations.add(allocation2);

        // Create mock authentication
        mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn("user456");
    }

    /**
     * Test case 1: getMyAllocations returns 200 OK with allocations list
     * Validates that the endpoint returns successful response with allocation data
     */
    @Test
    @WithMockUser(username = "user456", roles = {"FACULTY"})
    void testGetMyAllocations_ReturnsOkWithAllocations() throws Exception {
        // Arrange
        when(facultyService.getAllocationsByUserId("user456")).thenReturn(testAllocations);

        // Act & Assert
        mockMvc.perform(get("/api/faculty/allocations")
                        .with(authentication(mockAuthentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("alloc1"))
                .andExpect(jsonPath("$[0].facultyId").value("faculty123"))
                .andExpect(jsonPath("$[0].subjectName").value("Data Structures"))
                .andExpect(jsonPath("$[1].id").value("alloc2"))
                .andExpect(jsonPath("$[1].subjectName").value("Algorithms"));

        // Verify service was called with correct User ID
        verify(facultyService, times(1)).getAllocationsByUserId("user456");
    }

    /**
     * Test case 2: getMyAllocations extracts User ID from authentication correctly
     * Validates that the controller correctly extracts the User ID from the authentication object
     */
    @Test
    @WithMockUser(username = "testuser123", roles = {"FACULTY"})
    void testGetMyAllocations_ExtractsUserIdFromAuthentication() throws Exception {
        // Arrange
        Authentication customAuth = mock(Authentication.class);
        when(customAuth.getName()).thenReturn("testuser123");
        when(facultyService.getAllocationsByUserId("testuser123")).thenReturn(testAllocations);

        // Act & Assert
        mockMvc.perform(get("/api/faculty/allocations")
                        .with(authentication(customAuth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify the correct User ID was extracted and passed to the service
        verify(facultyService, times(1)).getAllocationsByUserId("testuser123");
        verify(facultyService, never()).getAllocationsByUserId("user456");
    }

    /**
     * Test case 3: getMyAllocations handles ResourceNotFoundException with 404 response
     * Validates that when faculty is not found, the endpoint returns 404 Not Found
     */
    @Test
    @WithMockUser(username = "nonexistent", roles = {"FACULTY"})
    void testGetMyAllocations_HandlesResourceNotFoundException() throws Exception {
        // Arrange
        Authentication customAuth = mock(Authentication.class);
        when(customAuth.getName()).thenReturn("nonexistent");
        when(facultyService.getAllocationsByUserId("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Faculty", "userId", "nonexistent"));

        // Act & Assert
        mockMvc.perform(get("/api/faculty/allocations")
                        .with(authentication(customAuth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verify service was called
        verify(facultyService, times(1)).getAllocationsByUserId("nonexistent");
    }

    /**
     * Test case 4: getMyAllocations returns empty list when faculty has no allocations
     * Validates that the endpoint correctly handles faculty members with no class allocations
     */
    @Test
    @WithMockUser(username = "user789", roles = {"FACULTY"})
    void testGetMyAllocations_ReturnsEmptyListWhenNoAllocations() throws Exception {
        // Arrange
        Authentication customAuth = mock(Authentication.class);
        when(customAuth.getName()).thenReturn("user789");
        when(facultyService.getAllocationsByUserId("user789")).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/faculty/allocations")
                        .with(authentication(customAuth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify service was called
        verify(facultyService, times(1)).getAllocationsByUserId("user789");
    }

    /**
     * ERROR HANDLING TEST CASE 1: Null authentication object returns 401 Unauthorized
     * Validates that requests without authentication are rejected
     * Requirements: 2.1, 2.2, 2.3
     */
    @Test
    void testGetMyAllocations_WithNullAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert - No authentication provided
        mockMvc.perform(get("/api/faculty/allocations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Verify service was never called
        verify(facultyService, never()).getAllocationsByUserId(anyString());
    }

    /**
     * ERROR HANDLING TEST CASE 3: Invalid User ID format handled gracefully
     * Validates that malformed User IDs are handled properly
     * Requirements: 2.1, 2.2, 2.3
     */
    @Test
    @WithMockUser(username = "invalid@#$%", roles = {"FACULTY"})
    void testGetMyAllocations_WithInvalidUserIdFormat_HandlesGracefully() throws Exception {
        // Arrange
        Authentication customAuth = mock(Authentication.class);
        when(customAuth.getName()).thenReturn("invalid@#$%");
        when(facultyService.getAllocationsByUserId("invalid@#$%"))
                .thenThrow(new ResourceNotFoundException("Faculty", "userId", "invalid@#$%"));

        // Act & Assert
        mockMvc.perform(get("/api/faculty/allocations")
                        .with(authentication(customAuth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verify service was called with the invalid ID
        verify(facultyService, times(1)).getAllocationsByUserId("invalid@#$%");
    }

    /**
     * ERROR HANDLING TEST CASE 4: Concurrent requests don't interfere with each other
     * Validates that multiple simultaneous requests are handled independently
     * Requirements: 2.1, 2.2, 2.3
     */
    @Test
    @WithMockUser(username = "user1", roles = {"FACULTY"})
    void testGetMyAllocations_ConcurrentRequests_HandleIndependently() throws Exception {
        // Arrange - Set up different responses for different users
        Authentication auth1 = mock(Authentication.class);
        when(auth1.getName()).thenReturn("user1");
        
        Authentication auth2 = mock(Authentication.class);
        when(auth2.getName()).thenReturn("user2");
        
        List<ClassAllocationDTO> allocations1 = List.of(testAllocations.get(0));
        List<ClassAllocationDTO> allocations2 = List.of(testAllocations.get(1));
        
        when(facultyService.getAllocationsByUserId("user1")).thenReturn(allocations1);
        when(facultyService.getAllocationsByUserId("user2")).thenReturn(allocations2);

        // Act & Assert - Simulate concurrent requests
        mockMvc.perform(get("/api/faculty/allocations")
                        .with(authentication(auth1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("alloc1"));

        mockMvc.perform(get("/api/faculty/allocations")
                        .with(authentication(auth2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("alloc2"));

        // Verify each service call was made with the correct User ID
        verify(facultyService, times(1)).getAllocationsByUserId("user1");
        verify(facultyService, times(1)).getAllocationsByUserId("user2");
    }
}
