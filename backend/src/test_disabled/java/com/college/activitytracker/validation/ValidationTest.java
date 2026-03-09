package com.college.activitytracker.validation;

import com.college.activitytracker.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify Bean Validation annotations on DTOs
 */
public class ValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testStudentDTO_ValidData_NoViolations() {
        StudentDTO student = StudentDTO.builder()
                .rollNumber("CS2021001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("9876543210")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .courseId("course123")
                .year(2)
                .section("A")
                .build();

        Set<ConstraintViolation<StudentDTO>> violations = validator.validate(student);
        assertTrue(violations.isEmpty(), "Valid student should have no violations");
    }

    @Test
    public void testStudentDTO_InvalidEmail_HasViolation() {
        StudentDTO student = StudentDTO.builder()
                .rollNumber("CS2021001")
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")
                .phone("9876543210")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .courseId("course123")
                .year(2)
                .section("A")
                .build();

        Set<ConstraintViolation<StudentDTO>> violations = validator.validate(student);
        assertFalse(violations.isEmpty(), "Invalid email should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    public void testStudentDTO_InvalidPhone_HasViolation() {
        StudentDTO student = StudentDTO.builder()
                .rollNumber("CS2021001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("123") // Invalid: not 10 digits
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .courseId("course123")
                .year(2)
                .section("A")
                .build();

        Set<ConstraintViolation<StudentDTO>> violations = validator.validate(student);
        assertFalse(violations.isEmpty(), "Invalid phone should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    public void testStudentDTO_InvalidYear_HasViolation() {
        StudentDTO student = StudentDTO.builder()
                .rollNumber("CS2021001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("9876543210")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .courseId("course123")
                .year(5) // Invalid: must be 1-4
                .section("A")
                .build();

        Set<ConstraintViolation<StudentDTO>> violations = validator.validate(student);
        assertFalse(violations.isEmpty(), "Invalid year should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("year")));
    }

    @Test
    public void testStudentDTO_InvalidSection_HasViolation() {
        StudentDTO student = StudentDTO.builder()
                .rollNumber("CS2021001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("9876543210")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .courseId("course123")
                .year(2)
                .section("AB") // Invalid: must be single letter
                .build();

        Set<ConstraintViolation<StudentDTO>> violations = validator.validate(student);
        assertFalse(violations.isEmpty(), "Invalid section should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("section")));
    }

    @Test
    public void testFacultyDTO_ValidData_NoViolations() {
        FacultyDTO faculty = FacultyDTO.builder()
                .employeeId("EMP001")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("9876543210")
                .department("Computer Science")
                .designation("Professor")
                .build();

        Set<ConstraintViolation<FacultyDTO>> violations = validator.validate(faculty);
        assertTrue(violations.isEmpty(), "Valid faculty should have no violations");
    }

    @Test
    public void testAttendanceDTO_ValidData_NoViolations() {
        AttendanceDTO attendance = AttendanceDTO.builder()
                .studentId("student123")
                .subjectId("subject123")
                .facultyId("faculty123")
                .date(LocalDate.now())
                .status("PRESENT")
                .build();

        Set<ConstraintViolation<AttendanceDTO>> violations = validator.validate(attendance);
        assertTrue(violations.isEmpty(), "Valid attendance should have no violations");
    }

    @Test
    public void testAttendanceDTO_InvalidStatus_HasViolation() {
        AttendanceDTO attendance = AttendanceDTO.builder()
                .studentId("student123")
                .subjectId("subject123")
                .facultyId("faculty123")
                .date(LocalDate.now())
                .status("MAYBE") // Invalid status
                .build();

        Set<ConstraintViolation<AttendanceDTO>> violations = validator.validate(attendance);
        assertFalse(violations.isEmpty(), "Invalid status should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    public void testPerformanceDTO_ValidData_NoViolations() {
        PerformanceDTO performance = PerformanceDTO.builder()
                .studentId("student123")
                .subjectId("subject123")
                .examType("INTERNAL")
                .marksObtained(85.0)
                .totalMarks(100.0)
                .build();

        Set<ConstraintViolation<PerformanceDTO>> violations = validator.validate(performance);
        assertTrue(violations.isEmpty(), "Valid performance should have no violations");
    }

    @Test
    public void testPerformanceDTO_InvalidExamType_HasViolation() {
        PerformanceDTO performance = PerformanceDTO.builder()
                .studentId("student123")
                .subjectId("subject123")
                .examType("QUIZ") // Invalid exam type
                .marksObtained(85.0)
                .totalMarks(100.0)
                .build();

        Set<ConstraintViolation<PerformanceDTO>> violations = validator.validate(performance);
        assertFalse(violations.isEmpty(), "Invalid exam type should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("examType")));
    }

    @Test
    public void testPerformanceDTO_NegativeMarks_HasViolation() {
        PerformanceDTO performance = PerformanceDTO.builder()
                .studentId("student123")
                .subjectId("subject123")
                .examType("INTERNAL")
                .marksObtained(-10.0) // Invalid: negative marks
                .totalMarks(100.0)
                .build();

        Set<ConstraintViolation<PerformanceDTO>> violations = validator.validate(performance);
        assertFalse(violations.isEmpty(), "Negative marks should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("marksObtained")));
    }

    @Test
    public void testLoginRequest_ValidData_NoViolations() {
        LoginRequest login = new LoginRequest();
        login.setEmail("user@example.com");
        login.setPassword("password123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(login);
        assertTrue(violations.isEmpty(), "Valid login should have no violations");
    }

    @Test
    public void testLoginRequest_MissingEmail_HasViolation() {
        LoginRequest login = new LoginRequest();
        login.setPassword("password123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(login);
        assertFalse(violations.isEmpty(), "Missing email should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    public void testRegisterRequest_ValidData_NoViolations() {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("user@example.com");
        register.setPassword("Password@123");
        register.setRole("ROLE_STUDENT");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(register);
        assertTrue(violations.isEmpty(), "Valid register should have no violations");
    }

    @Test
    public void testRegisterRequest_WeakPassword_HasViolation() {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("user@example.com");
        register.setPassword("weak"); // Invalid: doesn't meet complexity requirements
        register.setRole("ROLE_STUDENT");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(register);
        assertFalse(violations.isEmpty(), "Weak password should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    public void testCourseDTO_ValidData_NoViolations() {
        CourseDTO course = CourseDTO.builder()
                .code("BTECH-CSE")
                .name("Bachelor of Technology in Computer Science")
                .duration(4)
                .build();

        Set<ConstraintViolation<CourseDTO>> violations = validator.validate(course);
        assertTrue(violations.isEmpty(), "Valid course should have no violations");
    }

    @Test
    public void testCourseDTO_InvalidCode_HasViolation() {
        CourseDTO course = CourseDTO.builder()
                .code("btech-cse") // Invalid: must be uppercase
                .name("Bachelor of Technology in Computer Science")
                .duration(4)
                .build();

        Set<ConstraintViolation<CourseDTO>> violations = validator.validate(course);
        assertFalse(violations.isEmpty(), "Invalid course code should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")));
    }

    @Test
    public void testSubjectDTO_ValidData_NoViolations() {
        SubjectDTO subject = SubjectDTO.builder()
                .code("CS101")
                .name("Introduction to Computer Science")
                .credits(4)
                .courseId("course123")
                .semester(1)
                .build();

        Set<ConstraintViolation<SubjectDTO>> violations = validator.validate(subject);
        assertTrue(violations.isEmpty(), "Valid subject should have no violations");
    }

    @Test
    public void testSubjectDTO_InvalidCredits_HasViolation() {
        SubjectDTO subject = SubjectDTO.builder()
                .code("CS101")
                .name("Introduction to Computer Science")
                .credits(15) // Invalid: exceeds max of 10
                .courseId("course123")
                .semester(1)
                .build();

        Set<ConstraintViolation<SubjectDTO>> violations = validator.validate(subject);
        assertFalse(violations.isEmpty(), "Invalid credits should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("credits")));
    }

    @Test
    public void testClassAllocationDTO_ValidData_NoViolations() {
        ClassAllocationDTO allocation = ClassAllocationDTO.builder()
                .facultyId("faculty123")
                .subjectId("subject123")
                .courseId("course123")
                .year(2)
                .section("A")
                .build();

        Set<ConstraintViolation<ClassAllocationDTO>> violations = validator.validate(allocation);
        assertTrue(violations.isEmpty(), "Valid allocation should have no violations");
    }

    @Test
    public void testClassAllocationDTO_InvalidYear_HasViolation() {
        ClassAllocationDTO allocation = ClassAllocationDTO.builder()
                .facultyId("faculty123")
                .subjectId("subject123")
                .courseId("course123")
                .year(0) // Invalid: must be 1-4
                .section("A")
                .build();

        Set<ConstraintViolation<ClassAllocationDTO>> violations = validator.validate(allocation);
        assertFalse(violations.isEmpty(), "Invalid year should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("year")));
    }
}
