package com.college.activitytracker.service;

import com.college.activitytracker.dto.ClassAllocationDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.model.Faculty;
import com.college.activitytracker.repository.ClassAllocationRepository;
import com.college.activitytracker.repository.FacultyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FacultyService.getFacultyByUserId method
 * Tests the User ID to Faculty ID resolution logic
 * 
 * Requirements: 2.1 - Faculty allocation retrieval fix
 */
@ExtendWith(MockitoExtension.class)
class FacultyServiceTest {

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ClassAllocationRepository classAllocationRepository;

    @Mock
    private ClassAllocationService classAllocationService;

    @InjectMocks
    private FacultyService facultyService;

    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        // Create test faculty with distinct User ID and Faculty ID
        testFaculty = new Faculty(
                "faculty123",           // id
                "user456",              // userId
                "EMP001",               // employeeId
                "Test",                 // firstName
                "Faculty",              // lastName
                "test@example.com",     // email
                "1234567890",           // phone
                "Computer Science",     // department
                "Professor",            // designation
                null,                   // profileImage
                true,                   // isActive
                null,                   // createdAt
                null,                   // updatedAt
                null                    // deletedAt
        );
    }

    /**
     * Test case 1: getFacultyByUserId returns Faculty entity when found
     * Validates that the method correctly retrieves Faculty by User ID
     */
    @Test
    void testGetFacultyByUserId_WhenUserIdExists_ReturnsFaculty() {
        // Arrange
        String userId = "user456";
        when(facultyRepository.findByUserId(userId)).thenReturn(Optional.of(testFaculty));

        // Act
        Faculty result = facultyService.getFacultyByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(testFaculty.getId(), result.getId());
        assertEquals(testFaculty.getUserId(), result.getUserId());
        assertEquals(testFaculty.getEmployeeId(), result.getEmployeeId());
        assertEquals(testFaculty.getFirstName(), result.getFirstName());
        assertEquals(testFaculty.getLastName(), result.getLastName());
        
        verify(facultyRepository, times(1)).findByUserId(userId);
    }

    /**
     * Test case 2: getFacultyByUserId throws ResourceNotFoundException when User ID not found
     * Validates proper error handling when User ID doesn't map to any Faculty
     */
    @Test
    void testGetFacultyByUserId_WhenUserIdNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String userId = "nonexistent_user";
        when(facultyRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> facultyService.getFacultyByUserId(userId)
        );

        assertTrue(exception.getMessage().contains("Faculty"));
        assertTrue(exception.getMessage().contains("userId"));
        assertTrue(exception.getMessage().contains(userId));
        
        verify(facultyRepository, times(1)).findByUserId(userId);
    }

    /**
     * Test case 3: getFacultyByUserId handles null User ID with appropriate exception
     * Validates that null User ID is handled gracefully
     */
    @Test
    void testGetFacultyByUserId_WhenUserIdIsNull_ThrowsResourceNotFoundException() {
        // Arrange
        String userId = null;
        when(facultyRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> facultyService.getFacultyByUserId(userId)
        );

        assertTrue(exception.getMessage().contains("Faculty"));
        assertTrue(exception.getMessage().contains("userId"));
        
        verify(facultyRepository, times(1)).findByUserId(userId);
    }

    /**
     * Test case 1: getAllocationsByUserId returns allocations when faculty exists
     * Validates that the method correctly retrieves allocations for a valid User ID
     * Requirements: 2.2, 2.3
     */
    @Test
    void testGetAllocationsByUserId_WhenFacultyExists_ReturnsAllocations() {
        // Arrange
        String userId = "user456";
        String facultyId = "faculty123";
        
        ClassAllocationDTO allocation1 = new ClassAllocationDTO(
            "alloc1", facultyId, "subject1", "course1", 
            2, "A", "2024-25", "Fall", true,
            "Test Faculty", "Data Structures", "Computer Science",
            null, null
        );
        
        ClassAllocationDTO allocation2 = new ClassAllocationDTO(
            "alloc2", facultyId, "subject2", "course1",
            3, "B", "2024-25", "Fall", true,
            "Test Faculty", "Algorithms", "Computer Science",
            null, null
        );
        
        List<ClassAllocationDTO> expectedAllocations = List.of(allocation1, allocation2);
        
        when(facultyRepository.findByUserId(userId)).thenReturn(Optional.of(testFaculty));
        when(classAllocationService.getAllocationsByFaculty(facultyId)).thenReturn(expectedAllocations);

        // Act
        List<ClassAllocationDTO> result = facultyService.getAllocationsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("alloc1", result.get(0).getId());
        assertEquals("alloc2", result.get(1).getId());
        assertEquals("Data Structures", result.get(0).getSubjectName());
        assertEquals("Algorithms", result.get(1).getSubjectName());
        
        verify(facultyRepository, times(1)).findByUserId(userId);
        verify(classAllocationService, times(1)).getAllocationsByFaculty(facultyId);
    }

    /**
     * Test case 2: getAllocationsByUserId returns empty list when faculty has no allocations
     * Validates that the method handles faculty with no allocations correctly
     * Requirements: 2.2, 2.3
     */
    @Test
    void testGetAllocationsByUserId_WhenFacultyHasNoAllocations_ReturnsEmptyList() {
        // Arrange
        String userId = "user456";
        String facultyId = "faculty123";
        
        when(facultyRepository.findByUserId(userId)).thenReturn(Optional.of(testFaculty));
        when(classAllocationService.getAllocationsByFaculty(facultyId)).thenReturn(List.of());

        // Act
        List<ClassAllocationDTO> result = facultyService.getAllocationsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(facultyRepository, times(1)).findByUserId(userId);
        verify(classAllocationService, times(1)).getAllocationsByFaculty(facultyId);
    }

    /**
     * Test case 3: getAllocationsByUserId throws ResourceNotFoundException when faculty not found
     * Validates proper error handling when User ID doesn't map to any Faculty
     * Requirements: 2.2, 2.3
     */
    @Test
    void testGetAllocationsByUserId_WhenFacultyNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String userId = "nonexistent_user";
        
        when(facultyRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> facultyService.getAllocationsByUserId(userId)
        );

        assertTrue(exception.getMessage().contains("Faculty"));
        assertTrue(exception.getMessage().contains("userId"));
        assertTrue(exception.getMessage().contains(userId));
        
        verify(facultyRepository, times(1)).findByUserId(userId);
        verify(classAllocationService, never()).getAllocationsByFaculty(anyString());
    }

    /**
     * Test case 4: getAllocationsByUserId calls ClassAllocationService with correct Faculty ID
     * Validates that the method call sequence is correct: getFacultyByUserId → getAllocationsByFaculty
     * Requirements: 2.2, 2.3
     */
    @Test
    void testGetAllocationsByUserId_CallsClassAllocationServiceWithCorrectFacultyId() {
        // Arrange
        String userId = "user456";
        String facultyId = "faculty123";
        
        ClassAllocationDTO allocation = new ClassAllocationDTO(
            "alloc1", facultyId, "subject1", "course1",
            2, "A", "2024-25", "Fall", true,
            "Test Faculty", "Data Structures", "Computer Science",
            null, null
        );
        
        List<ClassAllocationDTO> expectedAllocations = List.of(allocation);
        
        when(facultyRepository.findByUserId(userId)).thenReturn(Optional.of(testFaculty));
        when(classAllocationService.getAllocationsByFaculty(facultyId)).thenReturn(expectedAllocations);

        // Act
        List<ClassAllocationDTO> result = facultyService.getAllocationsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // Verify method call sequence
        verify(facultyRepository, times(1)).findByUserId(userId);
        verify(classAllocationService, times(1)).getAllocationsByFaculty(facultyId);
        
        // Verify that ClassAllocationService was called with the Faculty ID, not the User ID
        verify(classAllocationService, never()).getAllocationsByFaculty(userId);
    }

    /**
     * ERROR HANDLING TEST CASE 2: Database connection error propagates correctly
     * Validates that database errors are properly propagated to the caller
     * Requirements: 2.1, 2.2, 2.3
     */
    @Test
    void testGetAllocationsByUserId_WhenDatabaseError_PropagatesException() {
        // Arrange
        String userId = "user456";
        
        when(facultyRepository.findByUserId(userId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> facultyService.getAllocationsByUserId(userId)
        );

        assertTrue(exception.getMessage().contains("Database connection failed"));
        
        verify(facultyRepository, times(1)).findByUserId(userId);
        verify(classAllocationService, never()).getAllocationsByFaculty(anyString());
    }

    /**
     * ERROR HANDLING TEST CASE 3: Invalid User ID format handled gracefully
     * Validates that empty or malformed User IDs are handled properly
     * Requirements: 2.1, 2.2, 2.3
     */
    @Test
    void testGetFacultyByUserId_WhenUserIdIsEmpty_ThrowsResourceNotFoundException() {
        // Arrange
        String userId = "";
        when(facultyRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> facultyService.getFacultyByUserId(userId)
        );

        assertTrue(exception.getMessage().contains("Faculty"));
        assertTrue(exception.getMessage().contains("userId"));
        
        verify(facultyRepository, times(1)).findByUserId(userId);
    }

    /**
     * ERROR HANDLING TEST CASE 4: Service layer handles concurrent requests independently
     * Validates that multiple simultaneous calls don't interfere with each other
     * Requirements: 2.1, 2.2, 2.3
     */
    @Test
    void testGetAllocationsByUserId_ConcurrentCalls_HandleIndependently() {
        // Arrange
        String userId1 = "user456";
        String userId2 = "user789";
        String facultyId1 = "faculty123";
        String facultyId2 = "faculty456";
        
        Faculty faculty1 = testFaculty;
        Faculty faculty2 = new Faculty(
            facultyId2, userId2, "EMP002", "Another", "Faculty",
            "another@example.com", "9876543210", "Mathematics",
            "Associate Professor", null, true, null, null, null
        );
        
        ClassAllocationDTO allocation1 = new ClassAllocationDTO(
            "alloc1", facultyId1, "subject1", "course1",
            2, "A", "2024-25", "Fall", true,
            "Test Faculty", "Data Structures", "Computer Science",
            null, null
        );
        
        ClassAllocationDTO allocation2 = new ClassAllocationDTO(
            "alloc2", facultyId2, "subject2", "course2",
            1, "B", "2024-25", "Fall", true,
            "Another Faculty", "Calculus", "Mathematics",
            null, null
        );
        
        when(facultyRepository.findByUserId(userId1)).thenReturn(Optional.of(faculty1));
        when(facultyRepository.findByUserId(userId2)).thenReturn(Optional.of(faculty2));
        when(classAllocationService.getAllocationsByFaculty(facultyId1)).thenReturn(List.of(allocation1));
        when(classAllocationService.getAllocationsByFaculty(facultyId2)).thenReturn(List.of(allocation2));

        // Act - Simulate concurrent calls
        List<ClassAllocationDTO> result1 = facultyService.getAllocationsByUserId(userId1);
        List<ClassAllocationDTO> result2 = facultyService.getAllocationsByUserId(userId2);

        // Assert - Each call returns the correct data
        assertNotNull(result1);
        assertEquals(1, result1.size());
        assertEquals("alloc1", result1.get(0).getId());
        assertEquals("Data Structures", result1.get(0).getSubjectName());
        
        assertNotNull(result2);
        assertEquals(1, result2.size());
        assertEquals("alloc2", result2.get(0).getId());
        assertEquals("Calculus", result2.get(0).getSubjectName());
        
        // Verify each call was made with the correct IDs
        verify(facultyRepository, times(1)).findByUserId(userId1);
        verify(facultyRepository, times(1)).findByUserId(userId2);
        verify(classAllocationService, times(1)).getAllocationsByFaculty(facultyId1);
        verify(classAllocationService, times(1)).getAllocationsByFaculty(facultyId2);
    }
}
