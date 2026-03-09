package com.college.activitytracker.service;

import com.college.activitytracker.dto.AttendanceDTO;
import com.college.activitytracker.model.Attendance;
import com.college.activitytracker.model.ClassAllocation;
import com.college.activitytracker.model.Faculty;
import com.college.activitytracker.model.Student;
import com.college.activitytracker.model.Subject;
import com.college.activitytracker.repository.AttendanceRepository;
import com.college.activitytracker.repository.ClassAllocationRepository;
import com.college.activitytracker.repository.FacultyRepository;
import com.college.activitytracker.repository.StudentRepository;
import com.college.activitytracker.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacultyAllocationValidationTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private ClassAllocationRepository classAllocationRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceDTO attendanceDTO;
    private Student student;
    private Subject subject;
    private Faculty faculty;
    private ClassAllocation activeAllocation;

    @BeforeEach
    void setUp() {
        // Setup test data
        student = new Student();
        student.setId("student1");
        student.setFirstName("John");
        student.setLastName("Doe");

        subject = new Subject();
        subject.setId("subject1");
        subject.setName("Mathematics");

        faculty = new Faculty();
        faculty.setId("faculty1");
        faculty.setFirstName("Jane");
        faculty.setLastName("Smith");

        activeAllocation = new ClassAllocation();
        activeAllocation.setId("allocation1");
        activeAllocation.setFacultyId("faculty1");
        activeAllocation.setSubjectId("subject1");
        activeAllocation.setCourseId("course1");
        activeAllocation.setYear(2);
        activeAllocation.setSection("A");
        activeAllocation.setIsActive(true);

        attendanceDTO = new AttendanceDTO();
        attendanceDTO.setStudentId("student1");
        attendanceDTO.setSubjectId("subject1");
        attendanceDTO.setFacultyId("faculty1");
        attendanceDTO.setCourseId("course1");
        attendanceDTO.setYear(2);
        attendanceDTO.setSection("A");
        attendanceDTO.setDate(LocalDate.now());
        attendanceDTO.setStatus("PRESENT");
    }

    @Test
    void testMarkAttendance_WithValidAllocation_Success() {
        // Arrange
        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));
        when(subjectRepository.findById("subject1")).thenReturn(Optional.of(subject));
        when(facultyRepository.findById("faculty1")).thenReturn(Optional.of(faculty));
        when(classAllocationRepository.findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                "faculty1", "subject1", "course1", 2, "A"))
                .thenReturn(Optional.of(activeAllocation));

        Attendance savedAttendance = new Attendance();
        savedAttendance.setId("attendance1");
        savedAttendance.setStudentId("student1");
        savedAttendance.setSubjectId("subject1");
        savedAttendance.setFacultyId("faculty1");
        savedAttendance.setStatus("PRESENT");
        savedAttendance.setCreatedAt(LocalDateTime.now());
        savedAttendance.setUpdatedAt(LocalDateTime.now());

        when(attendanceRepository.findByStudentIdAndSubjectIdAndDate(anyString(), anyString(), any()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);

        // Act
        AttendanceDTO result = attendanceService.markAttendance(attendanceDTO);

        // Assert
        assertNotNull(result);
        assertEquals("attendance1", result.getId());
        verify(classAllocationRepository).findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                "faculty1", "subject1", "course1", 2, "A");
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void testMarkAttendance_WithoutAllocation_ThrowsException() {
        // Arrange
        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));
        when(subjectRepository.findById("subject1")).thenReturn(Optional.of(subject));
        when(facultyRepository.findById("faculty1")).thenReturn(Optional.of(faculty));
        when(classAllocationRepository.findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                "faculty1", "subject1", "course1", 2, "A"))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.markAttendance(attendanceDTO);
        });

        assertEquals("Faculty is not allocated to this class", exception.getMessage());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testMarkAttendance_WithInactiveAllocation_ThrowsException() {
        // Arrange
        activeAllocation.setIsActive(false);

        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));
        when(subjectRepository.findById("subject1")).thenReturn(Optional.of(subject));
        when(facultyRepository.findById("faculty1")).thenReturn(Optional.of(faculty));
        when(classAllocationRepository.findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                "faculty1", "subject1", "course1", 2, "A"))
                .thenReturn(Optional.of(activeAllocation));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.markAttendance(attendanceDTO);
        });

        assertEquals("Faculty is not allocated to this class", exception.getMessage());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testMarkAttendance_WithNullIsActive_ThrowsException() {
        // Arrange
        activeAllocation.setIsActive(null);

        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));
        when(subjectRepository.findById("subject1")).thenReturn(Optional.of(subject));
        when(facultyRepository.findById("faculty1")).thenReturn(Optional.of(faculty));
        when(classAllocationRepository.findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                "faculty1", "subject1", "course1", 2, "A"))
                .thenReturn(Optional.of(activeAllocation));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.markAttendance(attendanceDTO);
        });

        assertEquals("Faculty is not allocated to this class", exception.getMessage());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testMarkAttendance_DifferentSubject_ThrowsException() {
        // Arrange
        attendanceDTO.setSubjectId("subject2"); // Different subject

        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));
        when(subjectRepository.findById("subject2")).thenReturn(Optional.of(subject));
        when(facultyRepository.findById("faculty1")).thenReturn(Optional.of(faculty));
        when(classAllocationRepository.findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                "faculty1", "subject2", "course1", 2, "A"))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.markAttendance(attendanceDTO);
        });

        assertEquals("Faculty is not allocated to this class", exception.getMessage());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testUpdateAttendance_WithValidAllocation_Success() {
        // Arrange
        Attendance existingAttendance = new Attendance();
        existingAttendance.setId("attendance1");
        existingAttendance.setStudentId("student1");
        existingAttendance.setSubjectId("subject1");
        existingAttendance.setFacultyId("faculty1");
        existingAttendance.setStatus("ABSENT");

        attendanceDTO.setId("attendance1");
        attendanceDTO.setStatus("PRESENT");

        when(attendanceRepository.findById("attendance1")).thenReturn(Optional.of(existingAttendance));
        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));
        when(subjectRepository.findById("subject1")).thenReturn(Optional.of(subject));
        when(facultyRepository.findById("faculty1")).thenReturn(Optional.of(faculty));
        when(classAllocationRepository.findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                "faculty1", "subject1", "course1", 2, "A"))
                .thenReturn(Optional.of(activeAllocation));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(existingAttendance);

        // Act
        AttendanceDTO result = attendanceService.updateAttendance("attendance1", attendanceDTO);

        // Assert
        assertNotNull(result);
        verify(classAllocationRepository).findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                "faculty1", "subject1", "course1", 2, "A");
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void testUpdateAttendance_WithoutAllocation_ThrowsException() {
        // Arrange
        Attendance existingAttendance = new Attendance();
        existingAttendance.setId("attendance1");
        existingAttendance.setStudentId("student1");
        existingAttendance.setSubjectId("subject1");
        existingAttendance.setFacultyId("faculty1");

        when(attendanceRepository.findById("attendance1")).thenReturn(Optional.of(existingAttendance));
        when(classAllocationRepository.findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                "faculty1", "subject1", "course1", 2, "A"))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.updateAttendance("attendance1", attendanceDTO);
        });

        assertEquals("Faculty is not allocated to this class", exception.getMessage());
        verify(attendanceRepository, times(1)).findById("attendance1");
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }
}
