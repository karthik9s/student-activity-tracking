package com.college.activitytracker.service;

import com.college.activitytracker.dto.PerformanceDTO;
import com.college.activitytracker.model.ClassAllocation;
import com.college.activitytracker.model.Faculty;
import com.college.activitytracker.model.Performance;
import com.college.activitytracker.model.Student;
import com.college.activitytracker.model.Subject;
import com.college.activitytracker.repository.ClassAllocationRepository;
import com.college.activitytracker.repository.FacultyRepository;
import com.college.activitytracker.repository.PerformanceRepository;
import com.college.activitytracker.repository.StudentRepository;
import com.college.activitytracker.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test_activity_tracker"
})
public class PerformanceAuthorizationTest {

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private ClassAllocationRepository classAllocationRepository;

    private Student testStudent;
    private Subject testSubject;
    private Faculty authorizedFaculty;
    private Faculty unauthorizedFaculty;
    private ClassAllocation allocation;

    @BeforeEach
    public void setup() {
        // Clean up
        performanceRepository.deleteAll();
        classAllocationRepository.deleteAll();
        studentRepository.deleteAll();
        subjectRepository.deleteAll();
        facultyRepository.deleteAll();

        // Create test student
        testStudent = new Student();
        testStudent.setFirstName("Test");
        testStudent.setLastName("Student");
        testStudent.setEmail("test.student@example.com");
        testStudent.setRollNumber("TEST001");
        testStudent.setCourseId("CS101");
        testStudent.setYear(2);
        testStudent.setSection("A");
        testStudent = studentRepository.save(testStudent);

        // Create test subject
        testSubject = new Subject();
        testSubject.setName("Data Structures");
        testSubject.setCode("CS201");
        testSubject.setCourseId("CS101");
        testSubject.setYear(2);
        testSubject.setSemester("3");
        testSubject = subjectRepository.save(testSubject);

        // Create authorized faculty
        authorizedFaculty = new Faculty();
        authorizedFaculty.setFirstName("Authorized");
        authorizedFaculty.setLastName("Faculty");
        authorizedFaculty.setEmail("authorized@example.com");
        authorizedFaculty.setEmployeeId("FAC001");
        authorizedFaculty = facultyRepository.save(authorizedFaculty);

        // Create unauthorized faculty
        unauthorizedFaculty = new Faculty();
        unauthorizedFaculty.setFirstName("Unauthorized");
        unauthorizedFaculty.setLastName("Faculty");
        unauthorizedFaculty.setEmail("unauthorized@example.com");
        unauthorizedFaculty.setEmployeeId("FAC002");
        unauthorizedFaculty = facultyRepository.save(unauthorizedFaculty);

        // Create class allocation for authorized faculty
        allocation = new ClassAllocation();
        allocation.setFacultyId(authorizedFaculty.getId());
        allocation.setSubjectId(testSubject.getId());
        allocation.setCourseId("CS101");
        allocation.setYear(2);
        allocation.setSection("A");
        allocation.setIsActive(true);
        allocation = classAllocationRepository.save(allocation);
    }

    @Test
    public void testAuthorizedFacultyCanAddPerformance() {
        PerformanceDTO dto = new PerformanceDTO();
        dto.setStudentId(testStudent.getId());
        dto.setSubjectId(testSubject.getId());
        dto.setFacultyId(authorizedFaculty.getId());
        dto.setCourseId("CS101");
        dto.setYear(2);
        dto.setSection("A");
        dto.setSemester("3");
        dto.setExamType("INTERNAL");
        dto.setMarksObtained(85.0);
        dto.setTotalMarks(100.0);

        PerformanceDTO result = performanceService.addPerformance(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(authorizedFaculty.getId(), result.getFacultyId());
    }

    @Test
    public void testUnauthorizedFacultyCannotAddPerformance() {
        PerformanceDTO dto = new PerformanceDTO();
        dto.setStudentId(testStudent.getId());
        dto.setSubjectId(testSubject.getId());
        dto.setFacultyId(unauthorizedFaculty.getId());
        dto.setCourseId("CS101");
        dto.setYear(2);
        dto.setSection("A");
        dto.setSemester("3");
        dto.setExamType("INTERNAL");
        dto.setMarksObtained(85.0);
        dto.setTotalMarks(100.0);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            performanceService.addPerformance(dto);
        });

        assertEquals("Faculty is not authorized to add marks for this subject", exception.getMessage());
    }

    @Test
    public void testInactiveAllocationPreventsPerformanceEntry() {
        // Deactivate the allocation
        allocation.setIsActive(false);
        classAllocationRepository.save(allocation);

        PerformanceDTO dto = new PerformanceDTO();
        dto.setStudentId(testStudent.getId());
        dto.setSubjectId(testSubject.getId());
        dto.setFacultyId(authorizedFaculty.getId());
        dto.setCourseId("CS101");
        dto.setYear(2);
        dto.setSection("A");
        dto.setSemester("3");
        dto.setExamType("INTERNAL");
        dto.setMarksObtained(85.0);
        dto.setTotalMarks(100.0);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            performanceService.addPerformance(dto);
        });

        assertEquals("Faculty is not authorized to add marks for this subject", exception.getMessage());
    }

    @Test
    public void testAuthorizedFacultyCanUpdatePerformance() {
        // First create a performance record
        PerformanceDTO createDto = new PerformanceDTO();
        createDto.setStudentId(testStudent.getId());
        createDto.setSubjectId(testSubject.getId());
        createDto.setFacultyId(authorizedFaculty.getId());
        createDto.setCourseId("CS101");
        createDto.setYear(2);
        createDto.setSection("A");
        createDto.setSemester("3");
        createDto.setExamType("INTERNAL");
        createDto.setMarksObtained(85.0);
        createDto.setTotalMarks(100.0);

        PerformanceDTO created = performanceService.addPerformance(createDto);

        // Now update it
        PerformanceDTO updateDto = new PerformanceDTO();
        updateDto.setStudentId(testStudent.getId());
        updateDto.setSubjectId(testSubject.getId());
        updateDto.setFacultyId(authorizedFaculty.getId());
        updateDto.setCourseId("CS101");
        updateDto.setYear(2);
        updateDto.setSection("A");
        updateDto.setSemester("3");
        updateDto.setExamType("INTERNAL");
        updateDto.setMarksObtained(90.0);
        updateDto.setTotalMarks(100.0);

        PerformanceDTO updated = performanceService.updatePerformance(created.getId(), updateDto);

        assertNotNull(updated);
        assertEquals(90.0, updated.getMarksObtained());
    }

    @Test
    public void testUnauthorizedFacultyCannotUpdatePerformance() {
        // First create a performance record with authorized faculty
        PerformanceDTO createDto = new PerformanceDTO();
        createDto.setStudentId(testStudent.getId());
        createDto.setSubjectId(testSubject.getId());
        createDto.setFacultyId(authorizedFaculty.getId());
        createDto.setCourseId("CS101");
        createDto.setYear(2);
        createDto.setSection("A");
        createDto.setSemester("3");
        createDto.setExamType("INTERNAL");
        createDto.setMarksObtained(85.0);
        createDto.setTotalMarks(100.0);

        PerformanceDTO created = performanceService.addPerformance(createDto);

        // Try to update with unauthorized faculty
        PerformanceDTO updateDto = new PerformanceDTO();
        updateDto.setStudentId(testStudent.getId());
        updateDto.setSubjectId(testSubject.getId());
        updateDto.setFacultyId(unauthorizedFaculty.getId());
        updateDto.setCourseId("CS101");
        updateDto.setYear(2);
        updateDto.setSection("A");
        updateDto.setSemester("3");
        updateDto.setExamType("INTERNAL");
        updateDto.setMarksObtained(90.0);
        updateDto.setTotalMarks(100.0);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            performanceService.updatePerformance(created.getId(), updateDto);
        });

        assertEquals("Faculty is not authorized to add marks for this subject", exception.getMessage());
    }
}
