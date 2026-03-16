package com.college.activitytracker.service;

import com.college.activitytracker.dto.StudentDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.model.Course;
import com.college.activitytracker.model.Student;
import com.college.activitytracker.repository.CourseRepository;
import com.college.activitytracker.repository.StudentRepository;
import com.college.activitytracker.security.UserPrincipal;
import com.college.activitytracker.util.StudentMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentMapper studentMapper;
    private final AuditLogService auditLogService;

    public StudentService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            StudentMapper studentMapper,
            AuditLogService auditLogService
    ) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.studentMapper = studentMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public StudentDTO createStudent(StudentDTO dto) {

        if (studentRepository.existsByRollNumber(dto.getRollNumber())) {
            throw new RuntimeException("Student with roll number already exists");
        }

        if (studentRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Student with email already exists");
        }

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", dto.getCourseId()));

        Student student = studentMapper.toEntity(dto);
        student = studentRepository.save(student);

        String userId = getCurrentUserId();
        auditLogService.logCreate(userId, "STUDENT", student.getId(), studentToMap(student));

        return studentMapper.toDTO(student, course);
    }

    @Transactional
    public StudentDTO updateStudent(String id, StudentDTO dto) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (student.getDeletedAt() != null) {
            throw new RuntimeException("Student already deleted");
        }

        Map<String, Object> oldValue = studentToMap(student);

        studentMapper.updateEntity(student, dto);
        student = studentRepository.save(student);

        String userId = getCurrentUserId();
        auditLogService.logUpdate(userId, "STUDENT", student.getId(), oldValue, studentToMap(student));

        Course course = courseRepository.findById(student.getCourseId()).orElse(null);
        return studentMapper.toDTO(student, course);
    }

    @Transactional
    public void deleteStudent(String id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        Map<String, Object> oldValue = studentToMap(student);

        student.setDeletedAt(LocalDateTime.now());
        student.setIsActive(false);

        studentRepository.save(student);

        String userId = getCurrentUserId();
        auditLogService.logDelete(userId, "STUDENT", student.getId(), oldValue);
    }

    public StudentDTO getStudentById(String id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (student.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Student", "id", id);
        }

        Course course = courseRepository.findById(student.getCourseId()).orElse(null);

        return studentMapper.toDTO(student, course);
    }

    public StudentDTO getStudentByUserId(String userId) {

        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userId));

        if (student.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Student", "userId", userId);
        }

        Course course = courseRepository.findById(student.getCourseId()).orElse(null);

        return studentMapper.toDTO(student, course);
    }

    public Page<StudentDTO> getAllStudents(Pageable pageable) {

        Page<Student> students = studentRepository.findAllActive(pageable);

        return students.map(student -> {
            Course course = courseRepository.findById(student.getCourseId()).orElse(null);
            return studentMapper.toDTO(student, course);
        });
    }

    public Page<StudentDTO> searchStudents(String searchTerm, Pageable pageable) {

        Page<Student> students = studentRepository.searchStudents(searchTerm, pageable);

        return students.map(student -> {
            Course course = courseRepository.findById(student.getCourseId()).orElse(null);
            return studentMapper.toDTO(student, course);
        });
    }

    public Page<StudentDTO> getStudentsByCourse(String courseId, Pageable pageable) {

        Page<Student> students = studentRepository.findByCourse(courseId, pageable);
        Course course = courseRepository.findById(courseId).orElse(null);

        return students.map(student -> studentMapper.toDTO(student, course));
    }
    public java.util.List<StudentDTO> getStudentsByCourse(String courseId, Integer year, String section) {

        java.util.List<Student> students;

        if (year != null && section != null) {
            students = studentRepository.findByCourseIdAndYearAndSectionAndDeletedAtIsNull(courseId, year, section);
        } else if (year != null) {
            students = studentRepository.findByCourseIdAndYearAndDeletedAtIsNull(courseId, year);
        } else {
            students = studentRepository.findByCourseIdAndDeletedAtIsNull(courseId);
        }

        Course course = courseRepository.findById(courseId).orElse(null);

        return students.stream()
                .map(student -> studentMapper.toDTO(student, course))
                .collect(java.util.stream.Collectors.toList());
    }


    public Page<StudentDTO> getStudentsByStatus(Boolean isActive, Pageable pageable) {

        Page<Student> students = studentRepository.findByIsActive(isActive, pageable);

        return students.map(student -> {
            Course course = courseRepository.findById(student.getCourseId()).orElse(null);
            return studentMapper.toDTO(student, course);
        });
    }

    public long getTotalStudents() {
        return studentRepository.countByDeletedAtIsNull();
    }

    public java.util.List<java.util.Map<String, Object>> searchByRollNumber(String rollNumber) {
        return studentRepository.findByRollNumberContaining(rollNumber).stream()
                .map(s -> {
                    java.util.Map<String, Object> result = new java.util.HashMap<>();
                    result.put("studentId", s.getId());
                    result.put("name", s.getName());
                    result.put("rollNumber", s.getRollNumber());
                    return result;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private String getCurrentUserId() {

        try {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null &&
                    authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {

                return userPrincipal.getId();
            }

        } catch (Exception e) {

            log.warn("Could not get current user ID");
        }

        return "SYSTEM";
    }

    private Map<String, Object> studentToMap(Student student) {

        Map<String, Object> map = new HashMap<>();

        map.put("id", student.getId());
        map.put("userId", student.getUserId());
        map.put("rollNumber", student.getRollNumber());
        map.put("name", student.getName());
        map.put("email", student.getEmail());
        map.put("phone", student.getPhone());
        map.put("courseId", student.getCourseId());
        map.put("year", student.getYear());
        map.put("section", student.getSection());
        map.put("isActive", student.getIsActive());

        return map;
    }
}