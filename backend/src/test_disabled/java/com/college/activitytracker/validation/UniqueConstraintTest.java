package com.college.activitytracker.validation;

import com.college.activitytracker.dto.*;
import com.college.activitytracker.model.*;
import com.college.activitytracker.repository.*;
import com.college.activitytracker.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify unique constraint enforcement across all entities.
 * Tests requirement 14.4: System must enforce unique constraints on fields like 
 * email, roll number, employee ID, course code, subject code, and class allocations.
 */
@SpringBootTest
@ActiveProfiles("test")
public class UniqueConstraintTest {

    @Autowired
    private StudentService studentService;
    
    @Autowired
    private FacultyService facultyService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private SubjectService subjectService;
    
    @Autowired
    private ClassAllocationService classAllocationService;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private ClassAllocationRepository classAllocationRepository;

    @BeforeEach
    public void setUp() {
        // Clean up test data
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
        courseRepository.deleteAll();
        subjectRepository.deleteAll();
        classAllocationRepository.deleteAll();
    }

    @Test
    public void testStudentRollNumberUniqueness() {
        // Create a course first
        CourseDTO courseDTO = createTestCourse("CS101", "Computer Science");
        
        // Create first student
        StudentDTO student1 = createTestStudent("S001", "student1@test.com", courseDTO.getId());
        StudentDTO created1 = studentService.createStudent(student1);
        assertNotNull(created1.getId());
        
        // Try to create second student with same roll number
        StudentDTO student2 = createTestStudent("S001", "student2@test.com", courseDTO.getId());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentService.createStudent(student2);
        });
        assertTrue(exception.getMessage().contains("roll number"));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testStudentEmailUniqueness() {
        // Create a course first
        CourseDTO courseDTO = createTestCourse("CS102", "Computer Science");
        
        // Create first student
        StudentDTO student1 = createTestStudent("S002", "duplicate@test.com", courseDTO.getId());
        StudentDTO created1 = studentService.createStudent(student1);
        assertNotNull(created1.getId());
        
        // Try to create second student with same email
        StudentDTO student2 = createTestStudent("S003", "duplicate@test.com", courseDTO.getId());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentService.createStudent(student2);
        });
        assertTrue(exception.getMessage().contains("email"));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testFacultyEmployeeIdUniqueness() {
        // Create first faculty
        FacultyDTO faculty1 = createTestFaculty("EMP001", "faculty1@test.com");
        FacultyDTO created1 = facultyService.createFaculty(faculty1);
        assertNotNull(created1.getId());
        
        // Try to create second faculty with same employee ID
        FacultyDTO faculty2 = createTestFaculty("EMP001", "faculty2@test.com");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facultyService.createFaculty(faculty2);
        });
        assertTrue(exception.getMessage().contains("employee ID"));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testFacultyEmailUniqueness() {
        // Create first faculty
        FacultyDTO faculty1 = createTestFaculty("EMP002", "duplicate.faculty@test.com");
        FacultyDTO created1 = facultyService.createFaculty(faculty1);
        assertNotNull(created1.getId());
        
        // Try to create second faculty with same email
        FacultyDTO faculty2 = createTestFaculty("EMP003", "duplicate.faculty@test.com");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facultyService.createFaculty(faculty2);
        });
        assertTrue(exception.getMessage().contains("email"));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testCourseCodeUniqueness() {
        // Create first course
        CourseDTO course1 = createTestCourse("CS103", "Computer Science");
        CourseDTO created1 = courseService.createCourse(course1);
        assertNotNull(created1.getId());
        
        // Try to create second course with same code
        CourseDTO course2 = createTestCourse("CS103", "Information Technology");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.createCourse(course2);
        });
        assertTrue(exception.getMessage().contains("code"));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testSubjectCodeUniquenessPerCourse() {
        // Create two courses
        CourseDTO course1 = createTestCourse("CS104", "Computer Science");
        CourseDTO created1 = courseService.createCourse(course1);
        
        CourseDTO course2 = createTestCourse("IT104", "Information Technology");
        CourseDTO created2 = courseService.createCourse(course2);
        
        // Create subject in first course
        SubjectDTO subject1 = createTestSubject("MATH101", "Mathematics", created1.getId());
        SubjectDTO createdSubject1 = subjectService.createSubject(subject1);
        assertNotNull(createdSubject1.getId());
        
        // Try to create subject with same code in same course - should fail
        SubjectDTO subject2 = createTestSubject("MATH101", "Advanced Mathematics", created1.getId());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            subjectService.createSubject(subject2);
        });
        assertTrue(exception.getMessage().contains("code"));
        assertTrue(exception.getMessage().contains("already exists"));
        
        // Create subject with same code in different course - should succeed
        SubjectDTO subject3 = createTestSubject("MATH101", "Mathematics", created2.getId());
        SubjectDTO createdSubject3 = subjectService.createSubject(subject3);
        assertNotNull(createdSubject3.getId());
    }

    @Test
    public void testClassAllocationUniqueness() {
        // Create prerequisites
        CourseDTO course = createTestCourse("CS105", "Computer Science");
        CourseDTO createdCourse = courseService.createCourse(course);
        
        FacultyDTO faculty = createTestFaculty("EMP004", "faculty4@test.com");
        FacultyDTO createdFaculty = facultyService.createFaculty(faculty);
        
        SubjectDTO subject = createTestSubject("PHYS101", "Physics", createdCourse.getId());
        SubjectDTO createdSubject = subjectService.createSubject(subject);
        
        // Create first allocation
        ClassAllocationDTO allocation1 = createTestAllocation(
            createdFaculty.getId(), 
            createdSubject.getId(), 
            createdCourse.getId(), 
            1, 
            "A"
        );
        ClassAllocationDTO created1 = classAllocationService.createAllocation(allocation1);
        assertNotNull(created1.getId());
        
        // Try to create duplicate allocation - should fail
        ClassAllocationDTO allocation2 = createTestAllocation(
            createdFaculty.getId(), 
            createdSubject.getId(), 
            createdCourse.getId(), 
            1, 
            "A"
        );
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            classAllocationService.createAllocation(allocation2);
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testStudentUpdateWithDuplicateRollNumber() {
        // Create a course
        CourseDTO courseDTO = createTestCourse("CS106", "Computer Science");
        
        // Create two students
        StudentDTO student1 = createTestStudent("S004", "student4@test.com", courseDTO.getId());
        StudentDTO created1 = studentService.createStudent(student1);
        
        StudentDTO student2 = createTestStudent("S005", "student5@test.com", courseDTO.getId());
        StudentDTO created2 = studentService.createStudent(student2);
        
        // Try to update student2 with student1's roll number
        created2.setRollNumber("S004");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentService.updateStudent(created2.getId(), created2);
        });
        assertTrue(exception.getMessage().contains("roll number"));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testStudentUpdateWithDuplicateEmail() {
        // Create a course
        CourseDTO courseDTO = createTestCourse("CS107", "Computer Science");
        
        // Create two students
        StudentDTO student1 = createTestStudent("S006", "student6@test.com", courseDTO.getId());
        StudentDTO created1 = studentService.createStudent(student1);
        
        StudentDTO student2 = createTestStudent("S007", "student7@test.com", courseDTO.getId());
        StudentDTO created2 = studentService.createStudent(student2);
        
        // Try to update student2 with student1's email
        created2.setEmail("student6@test.com");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentService.updateStudent(created2.getId(), created2);
        });
        assertTrue(exception.getMessage().contains("email"));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    // Helper methods to create test DTOs
    
    private StudentDTO createTestStudent(String rollNumber, String email, String courseId) {
        return StudentDTO.builder()
                .rollNumber(rollNumber)
                .firstName("Test")
                .lastName("Student")
                .email(email)
                .phone("1234567890")
                .courseId(courseId)
                .year(1)
                .section("A")
                .isActive(true)
                .build();
    }
    
    private FacultyDTO createTestFaculty(String employeeId, String email) {
        return FacultyDTO.builder()
                .employeeId(employeeId)
                .firstName("Test")
                .lastName("Faculty")
                .email(email)
                .phone("9876543210")
                .department("Computer Science")
                .designation("Professor")
                .isActive(true)
                .build();
    }
    
    private CourseDTO createTestCourse(String code, String name) {
        CourseDTO dto = new CourseDTO();
        dto.setCode(code);
        dto.setName(name);
        dto.setDescription("Test course");
        dto.setDuration(4);
        return dto;
    }
    
    private SubjectDTO createTestSubject(String code, String name, String courseId) {
        SubjectDTO dto = new SubjectDTO();
        dto.setCode(code);
        dto.setName(name);
        dto.setDescription("Test subject");
        dto.setCredits(3);
        dto.setCourseId(courseId);
        dto.setSemester(1);
        dto.setType("Theory");
        return dto;
    }
    
    private ClassAllocationDTO createTestAllocation(String facultyId, String subjectId, 
                                                     String courseId, Integer year, String section) {
        ClassAllocationDTO dto = new ClassAllocationDTO();
        dto.setFacultyId(facultyId);
        dto.setSubjectId(subjectId);
        dto.setCourseId(courseId);
        dto.setYear(year);
        dto.setSection(section);
        dto.setAcademicYear("2024-2025");
        dto.setSemester(1);
        dto.setIsActive(true);
        return dto;
    }
}
