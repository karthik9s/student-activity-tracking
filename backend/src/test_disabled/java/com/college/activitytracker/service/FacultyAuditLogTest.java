package com.college.activitytracker.service;

import com.college.activitytracker.dto.FacultyDTO;
import com.college.activitytracker.model.AuditLog;
import com.college.activitytracker.model.Faculty;
import com.college.activitytracker.repository.AuditLogRepository;
import com.college.activitytracker.repository.FacultyRepository;
import com.college.activitytracker.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test_activity_tracker"
})
class FacultyAuditLogTest {

    @Autowired
    private FacultyService facultyService;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        facultyRepository.deleteAll();
        
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
    void testCreateFacultyLogsAuditEntry() {
        // Given
        FacultyDTO dto = FacultyDTO.builder()
            .employeeId("EMP001")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@test.com")
            .phone("1234567890")
            .department("Computer Science")
            .designation("Professor")
            .isActive(true)
            .build();

        // When
        FacultyDTO created = facultyService.createFaculty(dto);

        // Then
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        
        AuditLog log = logs.get(0);
        assertThat(log.getUserId()).isEqualTo("test-admin-id");
        assertThat(log.getAction()).isEqualTo("CREATE");
        assertThat(log.getEntityType()).isEqualTo("FACULTY");
        assertThat(log.getEntityId()).isEqualTo(created.getId());
        assertThat(log.getOldValue()).isNull();
        assertThat(log.getNewValue()).isNotNull();
        assertThat(log.getNewValue().get("employeeId")).isEqualTo("EMP001");
        assertThat(log.getTimestamp()).isNotNull();
    }

    @Test
    void testUpdateFacultyLogsAuditEntry() {
        // Given
        Faculty faculty = Faculty.builder()
            .employeeId("EMP002")
            .firstName("Jane")
            .lastName("Smith")
            .email("jane.smith@test.com")
            .phone("9876543210")
            .department("Mathematics")
            .designation("Associate Professor")
            .isActive(true)
            .build();
        faculty = facultyRepository.save(faculty);
        auditLogRepository.deleteAll(); // Clear create log

        FacultyDTO updateDto = FacultyDTO.builder()
            .employeeId("EMP002")
            .firstName("Jane")
            .lastName("Smith-Updated")
            .email("jane.smith@test.com")
            .phone("9876543210")
            .department("Mathematics")
            .designation("Professor")
            .isActive(true)
            .build();

        // When
        facultyService.updateFaculty(faculty.getId(), updateDto);

        // Then
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        
        AuditLog log = logs.get(0);
        assertThat(log.getUserId()).isEqualTo("test-admin-id");
        assertThat(log.getAction()).isEqualTo("UPDATE");
        assertThat(log.getEntityType()).isEqualTo("FACULTY");
        assertThat(log.getEntityId()).isEqualTo(faculty.getId());
        assertThat(log.getOldValue()).isNotNull();
        assertThat(log.getNewValue()).isNotNull();
        assertThat(log.getOldValue().get("lastName")).isEqualTo("Smith");
        assertThat(log.getNewValue().get("lastName")).isEqualTo("Smith-Updated");
    }

    @Test
    void testDeleteFacultyLogsAuditEntry() {
        // Given
        Faculty faculty = Faculty.builder()
            .employeeId("EMP003")
            .firstName("Bob")
            .lastName("Johnson")
            .email("bob.johnson@test.com")
            .phone("5555555555")
            .department("Physics")
            .designation("Lecturer")
            .isActive(true)
            .build();
        faculty = facultyRepository.save(faculty);
        auditLogRepository.deleteAll(); // Clear create log

        // When
        facultyService.deleteFaculty(faculty.getId());

        // Then
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        
        AuditLog log = logs.get(0);
        assertThat(log.getUserId()).isEqualTo("test-admin-id");
        assertThat(log.getAction()).isEqualTo("DELETE");
        assertThat(log.getEntityType()).isEqualTo("FACULTY");
        assertThat(log.getEntityId()).isEqualTo(faculty.getId());
        assertThat(log.getOldValue()).isNotNull();
        assertThat(log.getOldValue().get("employeeId")).isEqualTo("EMP003");
        assertThat(log.getNewValue()).isNull();
    }
}
