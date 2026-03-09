package com.college.activitytracker.service;

import com.college.activitytracker.dto.CourseDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.model.Course;
import com.college.activitytracker.repository.CourseRepository;
import com.college.activitytracker.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public CourseService(CourseRepository courseRepository, StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        if (courseRepository.existsByCode(courseDTO.getCode())) {
            throw new RuntimeException("Course with code " + courseDTO.getCode() + " already exists");
        }
        
        Course course = new Course();
        mapDtoToEntity(courseDTO, course);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        
        Course saved = courseRepository.save(course);
        return mapEntityToDto(saved);
    }

    public Page<CourseDTO> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(this::mapEntityToDto);
    }

    public CourseDTO getCourseById(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return mapEntityToDto(course);
    }

    @Transactional
    public CourseDTO updateCourse(String id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        if (!course.getCode().equals(courseDTO.getCode()) &&
                courseRepository.existsByCode(courseDTO.getCode())) {
            throw new RuntimeException("Course with code " + courseDTO.getCode() + " already exists");
        }
        
        mapDtoToEntity(courseDTO, course);
        course.setUpdatedAt(LocalDateTime.now());
        
        Course updated = courseRepository.save(course);
        return mapEntityToDto(updated);
    }

    @Transactional
    public void deleteCourse(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        long studentCount = studentRepository.countByCourseId(id);
        if (studentCount > 0) {
            throw new IllegalStateException("Cannot delete course with enrolled students");
        }
        
        courseRepository.delete(course);
    }

    public Page<CourseDTO> searchCourses(String search, Pageable pageable) {
        return courseRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(search, search, pageable)
                .map(this::mapEntityToDto);
    }

    public long getTotalCourses() {
        return courseRepository.count();
    }

    private void mapDtoToEntity(CourseDTO dto, Course entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDuration(dto.getDuration());
        
        // Convert SemesterDTO list to Semester list
        if (dto.getSemesters() != null) {
            List<Course.Semester> semesters = dto.getSemesters().stream()
                    .map(semDto -> {
                        Course.Semester sem = new Course.Semester();
                        sem.setSemesterNumber(semDto.getSemesterNumber());
                        sem.setSubjectIds(semDto.getSubjectIds());
                        return sem;
                    })
                    .collect(java.util.stream.Collectors.toList());
            entity.setSemesters(semesters);
        }
    }

    private CourseDTO mapEntityToDto(Course entity) {
        CourseDTO dto = new CourseDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setDuration(entity.getDuration());
        
        // Convert Semester list to SemesterDTO list
        if (entity.getSemesters() != null) {
            List<CourseDTO.SemesterDTO> semesterDTOs = entity.getSemesters().stream()
                    .map(sem -> {
                        CourseDTO.SemesterDTO semDto = new CourseDTO.SemesterDTO();
                        semDto.setSemesterNumber(sem.getSemesterNumber());
                        semDto.setSubjectIds(sem.getSubjectIds());
                        return semDto;
                    })
                    .collect(java.util.stream.Collectors.toList());
            dto.setSemesters(semesterDTOs);
        }
        
        return dto;
    }
}
