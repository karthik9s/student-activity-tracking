package com.college.activitytracker.service;

import com.college.activitytracker.model.*;
import com.college.activitytracker.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for service layer methods
 * Demonstrates testing approach for all major services
 */
@ExtendWith(MockitoExtension.class)
public class ServiceLayerTestSuite {

    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private FacultyRepository facultyRepository;
    
    @Mock
    private CourseRepository courseRepository;
    
    @Mock
    private AttendanceRepository attendanceRepository;
    
    @Mock
    private PerformanceRepository performanceRepository;
    
    @InjectMocks
    private StudentService studentService;
    
    @InjectMocks
    private FacultyService facultyService;
    
    @InjectMocks
    private CourseService courseService;
    
    @InjectMocks
    private AttendanceService attendanceService;
    
    @InjectMocks
    private PerformanceService performanceService;

    // ==================== Student Service Tests ====================
    
    @Test
    public void testGetAllStudents_ReturnsPagedResults() {
        // Arrange
        List<Student> students = Arrays.asList(
            createTestStudent("S001", "John Doe"),
            createTestStudent("S002", "Jane Smith")
        );
        Page<Student> page = new PageImpl<>(students);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(studentRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);
        
        // Act
        Page<Student> result = studentService.getAllStudents(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(studentRepository, times(1)).findByDeletedAtIsNull(pageable);
    }
    
    @Test
    public void testCreateStudent_SavesSuccessfully() {
        // Arrange
        Student student = createTestStudent("S003", "Bob Johnson");
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        
        // Act
        Student result = studentService.createStudent(student);
        
        // Assert
        assertNotNull(result);
        assertEquals("S003", result.getRollNumber());
        verify(studentRepository, times(1)).save(any(Student.class));
    }
    
    @Test
    public void testSoftDeleteStudent_SetsDeletedAt() {
        // Arrange
        Student student = createTestStudent("S004", "Alice Brown");
        when(studentRepository.findById(anyString())).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        
        // Act
        studentService.deleteStudent("student-id");
        
        // Assert
        assertNotNull(student.getDeletedAt());
        verify(studentRepository, times(1)).save(student);
    }

    // ==================== Faculty Service Tests ====================
    
    @Test
    public void testGetAllFaculty_ReturnsPagedResults() {
        // Arrange
        List<Faculty> faculty = Arrays.asList(
            createTestFaculty("F001", "Dr. Smith"),
            createTestFaculty("F002", "Prof. Johnson")
        );
        Page<Faculty> page = new PageImpl<>(faculty);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(facultyRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);
        
        // Act
        Page<Faculty> result = facultyService.getAllFaculty(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(facultyRepository, times(1)).findByDeletedAtIsNull(pageable);
    }

    // ==================== Course Service Tests ====================
    
    @Test
    public void testGetAllCourses_ReturnsAllCourses() {
        // Arrange
        List<Course> courses = Arrays.asList(
            createTestCourse("CS101", "Computer Science"),
            createTestCourse("EE101", "Electrical Engineering")
        );
        when(courseRepository.findAll()).thenReturn(courses);
        
        // Act
        List<Course> result = courseService.getAllCourses();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(courseRepository, times(1)).findAll();
    }

    // ==================== Attendance Service Tests ====================
    
    @Test
    public void testMarkAttendance_SavesSuccessfully() {
        // Arrange
        Attendance attendance = createTestAttendance("student-id", "subject-id", true);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        
        // Act
        Attendance result = attendanceService.markAttendance(attendance);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isPresent());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }
    
    @Test
    public void testCalculateAttendancePercentage_ReturnsCorrectValue() {
        // Arrange
        String studentId = "student-id";
        String subjectId = "subject-id";
        List<Attendance> records = Arrays.asList(
            createTestAttendance(studentId, subjectId, true),
            createTestAttendance(studentId, subjectId, true),
            createTestAttendance(studentId, subjectId, false),
            createTestAttendance(studentId, subjectId, true)
        );
        when(attendanceRepository.findByStudentIdAndSubjectId(studentId, subjectId))
            .thenReturn(records);
        
        // Act
        double percentage = attendanceService.calculateAttendancePercentage(studentId, subjectId);
        
        // Assert
        assertEquals(75.0, percentage, 0.01);
    }

    // ==================== Performance Service Tests ====================
    
    @Test
    public void testCreatePerformance_SavesSuccessfully() {
        // Arrange
        Performance performance = createTestPerformance("student-id", "subject-id", 85.0);
        when(performanceRepository.save(any(Performance.class))).thenReturn(performance);
        
        // Act
        Performance result = performanceService.createPerformance(performance);
        
        // Assert
        assertNotNull(result);
        assertEquals(85.0, result.getObtainedMarks());
        verify(performanceRepository, times(1)).save(any(Performance.class));
    }
    
    @Test
    public void testCalculateGrade_ReturnsCorrectGrade() {
        // Arrange
        double percentage = 85.0;
        
        // Act
        String grade = performanceService.calculateGrade(percentage);
        
        // Assert
        assertEquals("A", grade);
    }
    
    @Test
    public void testCalculateGPA_ReturnsCorrectValue() {
        // Arrange
        String studentId = "student-id";
        List<Performance> records = Arrays.asList(
            createTestPerformanceWithGrade("student-id", "sub1", "A", 4),
            createTestPerformanceWithGrade("student-id", "sub2", "B", 3),
            createTestPerformanceWithGrade("student-id", "sub3", "A", 4)
        );
        when(performanceRepository.findByStudentId(studentId)).thenReturn(records);
        
        // Act
        double gpa = performanceService.calculateGPA(studentId);
        
        // Assert
        assertTrue(gpa >= 3.6 && gpa <= 3.7); // Weighted average
    }

    // ==================== Helper Methods ====================
    
    private Student createTestStudent(String rollNumber, String name) {
        Student student = new Student();
        student.setId(UUID.randomUUID().toString());
        student.setRollNumber(rollNumber);
        student.setName(name);
        student.setEmail(rollNumber.toLowerCase() + "@test.com");
        student.setCreatedAt(LocalDateTime.now());
        return student;
    }
    
    private Faculty createTestFaculty(String employeeId, String name) {
        Faculty faculty = new Faculty();
        faculty.setId(UUID.randomUUID().toString());
        faculty.setEmployeeId(employeeId);
        faculty.setName(name);
        faculty.setEmail(employeeId.toLowerCase() + "@test.com");
        faculty.setCreatedAt(LocalDateTime.now());
        return faculty;
    }
    
    private Course createTestCourse(String code, String name) {
        Course course = new Course();
        course.setId(UUID.randomUUID().toString());
        course.setCode(code);
        course.setName(name);
        course.setDuration(4);
        return course;
    }
    
    private Attendance createTestAttendance(String studentId, String subjectId, boolean present) {
        Attendance attendance = new Attendance();
        attendance.setId(UUID.randomUUID().toString());
        attendance.setStudentId(studentId);
        attendance.setSubjectId(subjectId);
        attendance.setDate(LocalDate.now());
        attendance.setPresent(present);
        return attendance;
    }
    
    private Performance createTestPerformance(String studentId, String subjectId, double marks) {
        Performance performance = new Performance();
        performance.setId(UUID.randomUUID().toString());
        performance.setStudentId(studentId);
        performance.setSubjectId(subjectId);
        performance.setObtainedMarks(marks);
        performance.setMaxMarks(100.0);
        performance.setAssessmentType("MIDTERM");
        return performance;
    }
    
    private Performance createTestPerformanceWithGrade(String studentId, String subjectId, 
                                                       String grade, int credits) {
        Performance performance = createTestPerformance(studentId, subjectId, 85.0);
        performance.setGrade(grade);
        // Note: Credits would typically come from Subject entity
        return performance;
    }
}
