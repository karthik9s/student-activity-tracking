package com.college.activitytracker.service;

import com.college.activitytracker.dto.StudentDTO;
import com.college.activitytracker.model.AuditLog;
import com.college.activitytracker.model.Course;
import com.college.activitytracker.model.Student;
import com.college.activitytracker.repository.AuditLogRepository;
import com.college.activitytracker.repository.CourseRepository;
import com.college.activitytracker.repository.StudentRepository;
import com.college.activitytracker.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test_activity_tracker"
})
class StudentAuditLogTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        
        // Create a test course
        testCourse = Course.builder()
            .courseCode("CS101")
            .courseName("Computer Science")
            .duration(4)
            .isActive(true)
            .build();
        testCourse = courseRepository.save(testCourse);
        
        // Set up security context with a test user
        UserPrincipal userPrincipal = new UserPrincipal(
            "test-admin-id",
            "admin@test.com",
            "password",
            List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())
        );
    }

    @Test
    void testCreateStudentLogsAuditEntry() {
        // Given
        StudentDTO dto = StudentDTO.builder()
            .rollNumber("STU001")
            .firstName("Alice")
            .lastName("Johnson")
            .email("alice.johnson@test.com")
            .phone("1234567890")
            .dateOfBirth(LocalDate.of(2000, 1, 15))
            .courseId(testCourse.getId())
            .year(1)
            .section("A")
            .isActive(true)
            .build();

        // When
        StudentDTO created = studentService.createStudent(dto);

        // Then
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        
        AuditLog log = logs.get(0);
        assertThat(log.getUserId()).isEqualTo("test-admin-id");
        assertThat(log.getAction()).isEqualTo("CREATE");
        assertThat(log.getEntityType()).isEqualTo("STUDENT");
        assertThat(log.getEntityId()).isEqualTo(created.getId());
        assertThat(log.getOldValue()).isNull();
        assertThat(log.getNewValue()).isNotNull();
        assertThat(log.getNewValue().get("rollNumber")).isEqualTo("STU001");
        assertThat(log.getTimestamp()).isNotNull();
    }

    @Test
    void testUpdateStudentLogsAuditEntry() {
        // Given
        Student student = Student.builder()
            .rollNumber("STU002")
            .firstName("Bob")
            .lastName("Smith")
            .email("bob.smith@test.com")
            .phone("9876543210")
            .dateOfBirth(LocalDate.of(2001, 5, 20))
            .courseId(testCourse.getId())
            .year(2)
            .section("B")
            .isActive(true)
            .build();
        student = studentRepository.save(student);
        auditLogRepository.deleteAll(); // Clear create log

        StudentDTO updateDto = StudentDTO.builder()
            .rollNumber("STU002")
            .firstName("Bob")
            .lastName("Smith-Updated")
            .email("bob.smith@test.com")
            .phone("9876543210")
            .dateOfBirth(LocalDate.of(2001, 5, 20))
            .courseId(testCourse.getId())
            .year(3)
            .section("B")
            .isActive(true)
            .build();

        // When
        studentService.updateStudent(student.getId(), updateDto);

        // Then
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        
        AuditLog log = logs.get(0);
        assertThat(log.getUserId()).isEqualTo("test-admin-id");
        assertThat(log.getAction()).isEqualTo("UPDATE");
        assertThat(log.getEntityType()).isEqualTo("STUDENT");
        assertThat(log.getEntityId()).isEqualTo(student.getId());
        assertThat(log.getOldValue()).isNotNull();
        assertThat(log.getNewValue()).isNotNull();
        assertThat(log.getOldValue().get("lastName")).isEqualTo("Smith");
        assertThat(log.getNewValue().get("lastName")).isEqualTo("Smith-Updated");
        assertThat(log.getOldValue().get("year")).isEqualTo(2);
        assertThat(log.getNewValue().get("year")).isEqualTo(3);
    }

    @Test
    void testDeleteStudentLogsAuditEntry() {
        // Given
        Student student = Student.builder()
            .rollNumber("STU003")
            .firstName("Charlie")
            .lastName("Brown")
            .email("charlie.brown@test.com")
            .phone("5555555555")
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .courseId(testCourse.getId())
            .year(4)
            .section("C")
            .isActive(true)
            .build();
        student = studentRepository.save(student);
        auditLogRepository.deleteAll(); // Clear create log

        // When
        studentService.deleteStudent(student.getId());

        // Then
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        
        AuditLog log = logs.get(0);
        assertThat(log.getUserId()).isEqualTo("test-admin-id");
        assertThat(log.getAction()).isEqualTo("DELETE");
        assertThat(log.getEntityType()).isEqualTo("STUDENT");
        assertThat(log.getEntityId()).isEqualTo(student.getId());
        assertThat(log.getOldValue()).isNotNull();
        assertThat(log.getOldValue().get("rollNumber")).isEqualTo("STU003");
        assertThat(log.getNewValue()).isNull();
    }
}
