package com.college.activitytracker.service;

import com.college.activitytracker.dto.PerformanceDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.model.Performance;
import com.college.activitytracker.repository.PerformanceRepository;
import com.college.activitytracker.repository.StudentRepository;
import com.college.activitytracker.repository.SubjectRepository;
import com.college.activitytracker.repository.FacultyRepository;
import com.college.activitytracker.repository.ClassAllocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final ClassAllocationRepository classAllocationRepository;

    public PerformanceService(PerformanceRepository performanceRepository, StudentRepository studentRepository,
                             SubjectRepository subjectRepository, FacultyRepository facultyRepository,
                             ClassAllocationRepository classAllocationRepository) {
        this.performanceRepository = performanceRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
        this.classAllocationRepository = classAllocationRepository;
    }

    @Transactional
    public PerformanceDTO addPerformance(PerformanceDTO dto) {
        studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        facultyRepository.findById(dto.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        // Validate faculty allocation
        validateFacultyAllocation(dto.getFacultyId(), dto.getSubjectId(), dto.getCourseId(), 
                                  dto.getYear(), dto.getSection());
        
        Performance performance = new Performance();
        mapDtoToEntity(dto, performance);
        performance.setCreatedAt(LocalDateTime.now());
        performance.setUpdatedAt(LocalDateTime.now());
        
        calculateGrade(performance);
        
        Performance saved = performanceRepository.save(performance);
        return mapEntityToDto(saved);
    }

    public List<PerformanceDTO> getPerformanceByStudent(String studentId) {
        return performanceRepository.findByStudentId(studentId).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public List<PerformanceDTO> getPerformanceByStudentAndSubject(String studentId, String subjectId) {
        return performanceRepository.findByStudentIdAndSubjectId(studentId, subjectId).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public List<PerformanceDTO> getPerformanceBySubject(String subjectId) {
        return performanceRepository.findBySubjectId(subjectId).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public PerformanceDTO getPerformanceById(String id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance not found"));
        return mapEntityToDto(performance);
    }

    @Transactional
    public PerformanceDTO updatePerformance(String id, PerformanceDTO dto) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance not found"));
        
        // Validate faculty allocation
        validateFacultyAllocation(dto.getFacultyId(), dto.getSubjectId(), dto.getCourseId(), 
                                  dto.getYear(), dto.getSection());
        
        mapDtoToEntity(dto, performance);
        performance.setUpdatedAt(LocalDateTime.now());
        
        calculateGrade(performance);
        
        Performance updated = performanceRepository.save(performance);
        return mapEntityToDto(updated);
    }

    @Transactional
    public void deletePerformance(String id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance not found"));
        performanceRepository.delete(performance);
    }

    public double calculateGPA(String studentId, String semester) {
        List<Performance> performances = performanceRepository.findByStudentIdAndSemester(studentId, semester);
        if (performances.isEmpty()) {
            return 0.0;
        }
        
        double totalGradePoints = performances.stream()
                .mapToDouble(this::getGradePoint)
                .sum();
        
        return totalGradePoints / performances.size();
    }

    private void calculateGrade(Performance performance) {
        double percentage = (performance.getMarksObtained() / performance.getTotalMarks()) * 100;
        performance.setPercentage(percentage);
        
        if (percentage >= 90) {
            performance.setGrade("A+");
        } else if (percentage >= 80) {
            performance.setGrade("A");
        } else if (percentage >= 70) {
            performance.setGrade("B+");
        } else if (percentage >= 60) {
            performance.setGrade("B");
        } else if (percentage >= 50) {
            performance.setGrade("C");
        } else if (percentage >= 40) {
            performance.setGrade("D");
        } else {
            performance.setGrade("F");
        }
    }

    private double getGradePoint(Performance performance) {
        String grade = performance.getGrade();
        switch (grade) {
            case "A+": return 10.0;
            case "A": return 9.0;
            case "B+": return 8.0;
            case "B": return 7.0;
            case "C": return 6.0;
            case "D": return 5.0;
            default: return 0.0;
        }
    }

    private void mapDtoToEntity(PerformanceDTO dto, Performance entity) {
        entity.setStudentId(dto.getStudentId());
        entity.setSubjectId(dto.getSubjectId());
        entity.setFacultyId(dto.getFacultyId());
        entity.setCourseId(dto.getCourseId());
        entity.setYear(dto.getYear());
        entity.setSection(dto.getSection());
        entity.setSemester(dto.getSemester());
        entity.setExamType(dto.getExamType());
        entity.setMarksObtained(dto.getMarksObtained());
        entity.setTotalMarks(dto.getTotalMarks());
        entity.setRemarks(dto.getRemarks());
        entity.setCreatedBy(dto.getCreatedBy());
    }

    private PerformanceDTO mapEntityToDto(Performance entity) {
        PerformanceDTO dto = new PerformanceDTO();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudentId());
        dto.setSubjectId(entity.getSubjectId());
        dto.setFacultyId(entity.getFacultyId());
        dto.setCourseId(entity.getCourseId());
        dto.setYear(entity.getYear());
        dto.setSection(entity.getSection());
        dto.setSemester(entity.getSemester());
        dto.setExamType(entity.getExamType());
        dto.setMarksObtained(entity.getMarksObtained());
        dto.setTotalMarks(entity.getTotalMarks());
        dto.setPercentage(entity.getPercentage());
        dto.setGrade(entity.getGrade());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        
        studentRepository.findById(entity.getStudentId())
                .ifPresent(s -> {
                    dto.setStudentName(s.getName());
                    dto.setRollNumber(s.getRollNumber());
                });
        subjectRepository.findById(entity.getSubjectId())
                .ifPresent(s -> dto.setSubjectName(s.getName()));
        
        return dto;
    }

    public java.util.Map<String, Object> getPerformanceSummary(String studentId) {
        List<Performance> allRecords = performanceRepository.findByStudentId(studentId);
        
        if (allRecords.isEmpty()) {
            return java.util.Map.of(
                "gpa", 0.0,
                "totalSubjects", 0,
                "averagePercentage", 0.0,
                "subjectWise", java.util.List.of(),
                "gradeDistribution", java.util.Map.of()
            );
        }
        
        java.util.Map<String, java.util.List<Performance>> subjectWiseMap = allRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(Performance::getSubjectId));
        
        int totalSubjects = subjectWiseMap.size();
        
        double totalGradePoints = allRecords.stream()
                .mapToDouble(this::getGradePoint)
                .average()
                .orElse(0.0);
        
        double averagePercentage = allRecords.stream()
                .mapToDouble(Performance::getPercentage)
                .average()
                .orElse(0.0);
        
        java.util.List<java.util.Map<String, Object>> subjectWise = new java.util.ArrayList<>();
        
        for (java.util.Map.Entry<String, java.util.List<Performance>> entry : subjectWiseMap.entrySet()) {
            String subjectId = entry.getKey();
            java.util.List<Performance> records = entry.getValue();
            
            double subjectAverage = records.stream()
                    .mapToDouble(Performance::getPercentage)
                    .average()
                    .orElse(0.0);
            
            String subjectName = subjectRepository.findById(subjectId)
                    .map(s -> s.getName())
                    .orElse("Unknown");
            
            String bestGrade = records.stream()
                    .max((p1, p2) -> Double.compare(p1.getPercentage(), p2.getPercentage()))
                    .map(Performance::getGrade)
                    .orElse("N/A");
            
            subjectWise.add(java.util.Map.of(
                "subjectId", subjectId,
                "subjectName", subjectName,
                "averagePercentage", subjectAverage,
                "bestGrade", bestGrade,
                "totalAssessments", records.size()
            ));
        }
        
        java.util.Map<String, Long> gradeDistribution = allRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Performance::getGrade,
                    java.util.stream.Collectors.counting()
                ));
        
        return java.util.Map.of(
            "gpa", totalGradePoints,
            "totalSubjects", totalSubjects,
            "averagePercentage", averagePercentage,
            "subjectWise", subjectWise,
            "gradeDistribution", gradeDistribution
        );
    }
    
    private void validateFacultyAllocation(String facultyId, String subjectId, String courseId, 
                                           Integer year, String section) {
        // Check if faculty has an active allocation for this class
        boolean hasAllocation = classAllocationRepository
                .findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
                        facultyId, subjectId, courseId, year, section)
                .map(allocation -> allocation.getIsActive() != null && allocation.getIsActive())
                .orElse(false);
        
        if (!hasAllocation) {
            throw new IllegalStateException("Faculty is not authorized to add marks for this subject");
        }
    }
}
