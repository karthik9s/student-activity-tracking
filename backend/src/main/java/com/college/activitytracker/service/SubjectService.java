package com.college.activitytracker.service;

import com.college.activitytracker.dto.SubjectDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.model.Course;
import com.college.activitytracker.model.Subject;
import com.college.activitytracker.repository.CourseRepository;
import com.college.activitytracker.repository.SubjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    public SubjectService(SubjectRepository subjectRepository, CourseRepository courseRepository) {
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public SubjectDTO createSubject(SubjectDTO subjectDTO) {
        courseRepository.findById(subjectDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        if (subjectRepository.existsByCourseIdAndCode(subjectDTO.getCourseId(), subjectDTO.getCode())) {
            throw new RuntimeException("Subject with code " + subjectDTO.getCode() + 
                    " already exists for this course");
        }
        
        Subject subject = new Subject();
        mapDtoToEntity(subjectDTO, subject);
        subject.setCreatedAt(LocalDateTime.now());
        subject.setUpdatedAt(LocalDateTime.now());
        
        Subject saved = subjectRepository.save(subject);
        return mapEntityToDto(saved);
    }

    public Page<SubjectDTO> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable)
                .map(this::mapEntityToDto);
    }

    public SubjectDTO getSubjectById(String id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        return mapEntityToDto(subject);
    }

    public List<SubjectDTO> getSubjectsByCourse(String courseId) {
        return subjectRepository.findByCourseId(courseId).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public List<SubjectDTO> getSubjectsByCourseSemester(String courseId, Integer semester) {
        return subjectRepository.findByCourseIdAndSemester(courseId, semester).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubjectDTO updateSubject(String id, SubjectDTO subjectDTO) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        
        if (!subject.getCourseId().equals(subjectDTO.getCourseId())) {
            courseRepository.findById(subjectDTO.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        }
        
        if ((!subject.getCourseId().equals(subjectDTO.getCourseId()) || 
             !subject.getCode().equals(subjectDTO.getCode())) &&
                subjectRepository.existsByCourseIdAndCode(subjectDTO.getCourseId(), subjectDTO.getCode())) {
            throw new RuntimeException("Subject with code " + subjectDTO.getCode() + 
                    " already exists for this course");
        }
        
        mapDtoToEntity(subjectDTO, subject);
        subject.setUpdatedAt(LocalDateTime.now());
        
        Subject updated = subjectRepository.save(subject);
        return mapEntityToDto(updated);
    }

    @Transactional
    public void deleteSubject(String id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        
        subjectRepository.delete(subject);
    }

    public Page<SubjectDTO> searchSubjects(String search, Pageable pageable) {
        return subjectRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(search, search, pageable)
                .map(this::mapEntityToDto);
    }

    private void mapDtoToEntity(SubjectDTO dto, Subject entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCredits(dto.getCredits());
        entity.setCourseId(dto.getCourseId());
        entity.setSemester(dto.getSemester());
        entity.setType(dto.getType());
    }

    private SubjectDTO mapEntityToDto(Subject entity) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCredits(entity.getCredits());
        dto.setCourseId(entity.getCourseId());
        dto.setSemester(entity.getSemester());
        dto.setType(entity.getType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        courseRepository.findById(entity.getCourseId())
                .ifPresent(course -> dto.setCourseName(course.getName()));
        
        return dto;
    }
}
