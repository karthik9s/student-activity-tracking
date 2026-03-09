package com.college.activitytracker.config;

import com.college.activitytracker.model.Student;
import com.college.activitytracker.model.User;
import com.college.activitytracker.repository.StudentRepository;
import com.college.activitytracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MongoDB schema validation.
 * These tests verify that schema validation rules are properly enforced.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test_student_activity_tracker"
})
class MongoSchemaValidationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void testValidUserDocument_ShouldSucceed() {
        // Given: A valid user document
        User user = User.builder()
            .email("test@example.com")
            .password("hashedPassword123")
            .role("ROLE_STUDENT")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When: Saving the user
        User savedUser = userRepository.save(user);

        // Then: User should be saved successfully
        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());

        // Cleanup
        userRepository.delete(savedUser);
    }

    @Test
    void testInvalidUserRole_ShouldFail() {
        // Given: A user with invalid role
        User user = User.builder()
            .email("invalid@example.com")
            .password("hashedPassword123")
            .role("INVALID_ROLE")  // Invalid role
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When/Then: Saving should fail with validation error
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }

    @Test
    void testValidStudentDocument_ShouldSucceed() {
        // Given: A valid student document
        Student student = Student.builder()
            .userId("507f1f77bcf86cd799439011")
            .rollNumber("TEST2024001")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("1234567890")
            .dateOfBirth(LocalDate.of(2000, 1, 1))
            .courseId("507f1f77bcf86cd799439012")
            .year(2)
            .section("A")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When: Saving the student
        Student savedStudent = studentRepository.save(student);

        // Then: Student should be saved successfully
        assertNotNull(savedStudent.getId());
        assertEquals("TEST2024001", savedStudent.getRollNumber());
        assertEquals(2, savedStudent.getYear());

        // Cleanup
        studentRepository.delete(savedStudent);
    }

    @Test
    void testInvalidStudentYear_ShouldFail() {
        // Given: A student with invalid year (must be 1-4)
        Student student = Student.builder()
            .userId("507f1f77bcf86cd799439011")
            .rollNumber("TEST2024002")
            .firstName("Jane")
            .lastName("Smith")
            .email("jane.smith@example.com")
            .courseId("507f1f77bcf86cd799439012")
            .year(5)  // Invalid year (must be 1-4)
            .section("A")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When/Then: Saving should fail with validation error
        assertThrows(Exception.class, () -> {
            studentRepository.save(student);
        });
    }

    @Test
    void testMissingRequiredField_ShouldFail() {
        // Given: A student missing required field (firstName)
        Student student = Student.builder()
            .userId("507f1f77bcf86cd799439011")
            .rollNumber("TEST2024003")
            // firstName is missing
            .lastName("Brown")
            .email("brown@example.com")
            .courseId("507f1f77bcf86cd799439012")
            .year(1)
            .section("B")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When/Then: Saving should fail with validation error
        assertThrows(Exception.class, () -> {
            studentRepository.save(student);
        });
    }

    @Test
    void testInvalidEmailFormat_ShouldFail() {
        // Given: A user with invalid email format
        User user = User.builder()
            .email("not-an-email")  // Invalid email format
            .password("hashedPassword123")
            .role("ROLE_STUDENT")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When/Then: Saving should fail with validation error
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
}
