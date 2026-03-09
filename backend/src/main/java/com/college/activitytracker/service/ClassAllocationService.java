package com.college.activitytracker.service;

import com.college.activitytracker.dto.ClassAllocationDTO;
import com.college.activitytracker.dto.SubjectDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.model.ClassAllocation;
import com.college.activitytracker.model.Course;
import com.college.activitytracker.model.Faculty;
import com.college.activitytracker.model.Subject;
import com.college.activitytracker.repository.ClassAllocationRepository;
import com.college.activitytracker.repository.CourseRepository;
import com.college.activitytracker.repository.FacultyRepository;
import com.college.activitytracker.repository.SubjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassAllocationService {

    private final ClassAllocationRepository allocationRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    public ClassAllocationService(ClassAllocationRepository allocationRepository, FacultyRepository facultyRepository,
                                 SubjectRepository subjectRepository, CourseRepository courseRepository) {
        this.allocationRepository = allocationRepository;
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public ClassAllocationDTO createAllocation(ClassAllocationDTO dto) {
        facultyRepository.findById(dto.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        if (allocationRepository.existsByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                dto.getFacultyId(), dto.getSubjectId(), dto.getCourseId(), dto.getYear(), dto.getSection())) {
            throw new IllegalStateException("Allocation already exists");
        }
        
        ClassAllocation allocation = new ClassAllocation();
        mapDtoToEntity(dto, allocation);
        allocation.setCreatedAt(LocalDateTime.now());
        allocation.setUpdatedAt(LocalDateTime.now());
        allocation.setIsActive(true);
        
        ClassAllocation saved = allocationRepository.save(allocation);
        return mapEntityToDto(saved);
    }

    public Page<ClassAllocationDTO> getAllAllocations(Pageable pageable) {
        return allocationRepository.findAll(pageable)
                .map(this::mapEntityToDto);
    }

    public ClassAllocationDTO getAllocationById(String id) {
        ClassAllocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found"));
        return mapEntityToDto(allocation);
    }

    public List<ClassAllocationDTO> getAllocationsByFaculty(String facultyId) {
        return allocationRepository.findByFacultyId(facultyId).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public List<ClassAllocationDTO> getAllocationsByClass(String courseId, Integer year, String section) {
        return allocationRepository.findByCourseIdAndYearAndSection(courseId, year, section).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassAllocationDTO updateAllocation(String id, ClassAllocationDTO dto) {
        ClassAllocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found"));
        
        mapDtoToEntity(dto, allocation);
        allocation.setUpdatedAt(LocalDateTime.now());
        
        ClassAllocation updated = allocationRepository.save(allocation);
        return mapEntityToDto(updated);
    }

    @Transactional
    public void deleteAllocation(String id) {
        ClassAllocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found"));
        allocationRepository.delete(allocation);
    }

    public List<SubjectDTO> getSubjectsByStudentCourseYearSection(String courseId, Integer year, String section) {
        List<ClassAllocation> allocations = allocationRepository.findByCourseIdAndYearAndSection(courseId, year, section);
        
        return allocations.stream()
                .map(allocation -> {
                    Subject subject = subjectRepository.findById(allocation.getSubjectId())
                            .orElse(null);
                    if (subject == null) return null;
                    
                    SubjectDTO dto = new SubjectDTO();
                    dto.setId(subject.getId());
                    dto.setName(subject.getName());
                    dto.setCode(subject.getCode());
                    dto.setCredits(subject.getCredits());
                    dto.setDescription(subject.getDescription());
                    dto.setCourseId(subject.getCourseId());
                    dto.setSemester(subject.getSemester());
                    dto.setType(subject.getType());
                    
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    private void mapDtoToEntity(ClassAllocationDTO dto, ClassAllocation entity) {
        entity.setFacultyId(dto.getFacultyId());
        entity.setSubjectId(dto.getSubjectId());
        entity.setCourseId(dto.getCourseId());
        entity.setYear(dto.getYear());
        entity.setSection(dto.getSection());
        entity.setAcademicYear(dto.getAcademicYear());
        entity.setSemester(dto.getSemester());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }

    private ClassAllocationDTO mapEntityToDto(ClassAllocation entity) {
        ClassAllocationDTO dto = new ClassAllocationDTO();
        dto.setId(entity.getId());
        dto.setFacultyId(entity.getFacultyId());
        dto.setSubjectId(entity.getSubjectId());
        dto.setCourseId(entity.getCourseId());
        dto.setYear(entity.getYear());
        dto.setSection(entity.getSection());
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setSemester(entity.getSemester());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        facultyRepository.findById(entity.getFacultyId())
                .ifPresent(f -> dto.setFacultyName(f.getFirstName() + " " + f.getLastName()));
        subjectRepository.findById(entity.getSubjectId())
                .ifPresent(s -> dto.setSubjectName(s.getName()));
        courseRepository.findById(entity.getCourseId())
                .ifPresent(c -> dto.setCourseName(c.getName()));
        
        return dto;
    }
}
