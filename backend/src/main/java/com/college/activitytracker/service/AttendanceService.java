package com.college.activitytracker.service;

import com.college.activitytracker.dto.AttendanceDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.model.Attendance;
import com.college.activitytracker.repository.AttendanceRepository;
import com.college.activitytracker.repository.StudentRepository;
import com.college.activitytracker.repository.SubjectRepository;
import com.college.activitytracker.repository.FacultyRepository;
import com.college.activitytracker.repository.ClassAllocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final ClassAllocationRepository classAllocationRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository,
                            SubjectRepository subjectRepository, FacultyRepository facultyRepository,
                            ClassAllocationRepository classAllocationRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
        this.classAllocationRepository = classAllocationRepository;
    }

    @Transactional
    public AttendanceDTO markAttendance(AttendanceDTO dto) {
        studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        facultyRepository.findById(dto.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        // Validate faculty allocation
        validateFacultyAllocation(dto.getFacultyId(), dto.getSubjectId(), dto.getCourseId(), 
                                  dto.getYear(), dto.getSection());
        
        Attendance attendance = attendanceRepository
                .findByStudentIdAndSubjectIdAndDate(dto.getStudentId(), dto.getSubjectId(), dto.getDate())
                .orElse(new Attendance());
        
        mapDtoToEntity(dto, attendance);
        if (attendance.getId() == null) {
            attendance.setCreatedAt(LocalDateTime.now());
        }
        attendance.setUpdatedAt(LocalDateTime.now());
        
        Attendance saved = attendanceRepository.save(attendance);
        return mapEntityToDto(saved);
    }

    @Transactional
    public List<AttendanceDTO> markBulkAttendance(List<AttendanceDTO> dtos) {
        return dtos.stream()
                .map(this::markAttendance)
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getAttendanceByStudent(String studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getAttendanceByStudentAndSubject(String studentId, String subjectId) {
        return attendanceRepository.findByStudentIdAndSubjectId(studentId, subjectId).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getAttendanceBySubjectAndDate(String subjectId, LocalDate date) {
        return attendanceRepository.findBySubjectIdAndDate(subjectId, date).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public AttendanceDTO getAttendanceById(String id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        return mapEntityToDto(attendance);
    }

    @Transactional
    public AttendanceDTO updateAttendance(String id, AttendanceDTO dto) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        
        // Validate faculty allocation
        validateFacultyAllocation(dto.getFacultyId(), dto.getSubjectId(), dto.getCourseId(), 
                                  dto.getYear(), dto.getSection());
        
        mapDtoToEntity(dto, attendance);
        attendance.setUpdatedAt(LocalDateTime.now());
        
        Attendance updated = attendanceRepository.save(attendance);
        return mapEntityToDto(updated);
    }

    @Transactional
    public void deleteAttendance(String id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendanceRepository.delete(attendance);
    }

    public double calculateAttendancePercentage(String studentId, String subjectId) {
        List<Attendance> records = attendanceRepository.findByStudentIdAndSubjectId(studentId, subjectId);
        if (records.isEmpty()) {
            return 0.0;
        }
        
        long presentCount = records.stream()
                .filter(a -> "PRESENT".equals(a.getStatus()))
                .count();
        
        return (presentCount * 100.0) / records.size();
    }

    private void mapDtoToEntity(AttendanceDTO dto, Attendance entity) {
        entity.setStudentId(dto.getStudentId());
        entity.setSubjectId(dto.getSubjectId());
        entity.setFacultyId(dto.getFacultyId());
        entity.setCourseId(dto.getCourseId());
        entity.setYear(dto.getYear());
        entity.setSection(dto.getSection());
        entity.setDate(dto.getDate());
        entity.setStatus(dto.getStatus());
        entity.setRemarks(dto.getRemarks());
        entity.setCreatedBy(dto.getCreatedBy());
    }

    private AttendanceDTO mapEntityToDto(Attendance entity) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudentId());
        dto.setSubjectId(entity.getSubjectId());
        dto.setFacultyId(entity.getFacultyId());
        dto.setCourseId(entity.getCourseId());
        dto.setYear(entity.getYear());
        dto.setSection(entity.getSection());
        dto.setDate(entity.getDate());
        dto.setStatus(entity.getStatus());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        
        studentRepository.findById(entity.getStudentId())
                .ifPresent(s -> dto.setStudentName(s.getFirstName() + " " + s.getLastName()));
        subjectRepository.findById(entity.getSubjectId())
                .ifPresent(s -> dto.setSubjectName(s.getName()));
        facultyRepository.findById(entity.getFacultyId())
                .ifPresent(f -> dto.setFacultyName(f.getFirstName() + " " + f.getLastName()));
        
        return dto;
    }

    public java.util.Map<String, Object> getAttendanceSummary(String studentId) {
        List<Attendance> allRecords = attendanceRepository.findByStudentId(studentId);
        
        if (allRecords.isEmpty()) {
            return java.util.Map.of(
                "overallPercentage", 0.0,
                "totalClasses", 0,
                "attendedClasses", 0,
                "lowAttendanceCount", 0,
                "subjectWise", java.util.List.of()
            );
        }
        
        long totalClasses = allRecords.size();
        long attendedClasses = allRecords.stream()
                .filter(a -> "PRESENT".equals(a.getStatus()))
                .count();
        
        double overallPercentage = (attendedClasses * 100.0) / totalClasses;
        
        java.util.Map<String, java.util.List<Attendance>> subjectWiseMap = allRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(Attendance::getSubjectId));
        
        java.util.List<java.util.Map<String, Object>> subjectWise = new java.util.ArrayList<>();
        int lowAttendanceCount = 0;
        
        for (java.util.Map.Entry<String, java.util.List<Attendance>> entry : subjectWiseMap.entrySet()) {
            String subjectId = entry.getKey();
            java.util.List<Attendance> records = entry.getValue();
            
            long subjectTotal = records.size();
            long subjectAttended = records.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()))
                    .count();
            
            double subjectPercentage = (subjectAttended * 100.0) / subjectTotal;
            
            if (subjectPercentage < 75.0) {
                lowAttendanceCount++;
            }
            
            String subjectName = subjectRepository.findById(subjectId)
                    .map(s -> s.getName())
                    .orElse("Unknown");
            
            subjectWise.add(java.util.Map.of(
                "subjectId", subjectId,
                "subjectName", subjectName,
                "totalClasses", subjectTotal,
                "attendedClasses", subjectAttended,
                "percentage", subjectPercentage
            ));
        }
        
        return java.util.Map.of(
            "overallPercentage", overallPercentage,
            "totalClasses", totalClasses,
            "attendedClasses", attendedClasses,
            "lowAttendanceCount", lowAttendanceCount,
            "subjectWise", subjectWise
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
            throw new IllegalStateException("Faculty is not allocated to this class");
        }
    }
}
