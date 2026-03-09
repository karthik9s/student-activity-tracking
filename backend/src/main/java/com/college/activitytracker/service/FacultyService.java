package com.college.activitytracker.service;

import com.college.activitytracker.dto.ClassAllocationDTO;
import com.college.activitytracker.dto.FacultyDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.model.Faculty;
import com.college.activitytracker.repository.ClassAllocationRepository;
import com.college.activitytracker.repository.FacultyRepository;
import com.college.activitytracker.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final AuditLogService auditLogService;
    private final ClassAllocationRepository classAllocationRepository;
    private final ClassAllocationService classAllocationService;
    
    public FacultyService(FacultyRepository facultyRepository, 
                         AuditLogService auditLogService, 
                         ClassAllocationRepository classAllocationRepository,
                         ClassAllocationService classAllocationService) {
        this.facultyRepository = facultyRepository;
        this.auditLogService = auditLogService;
        this.classAllocationRepository = classAllocationRepository;
        this.classAllocationService = classAllocationService;
    }

    @Transactional
    public FacultyDTO createFaculty(FacultyDTO dto) {
        System.out.println("Creating faculty with employee ID: " + dto.getEmployeeId());

        if (facultyRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new RuntimeException("Faculty with employee ID " + dto.getEmployeeId() + " already exists");
        }

        if (facultyRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Faculty with email " + dto.getEmail() + " already exists");
        }

        Faculty faculty = toEntity(dto);
        faculty = facultyRepository.save(faculty);

        // Log the create operation
        String userId = getCurrentUserId();
        Map<String, Object> newValue = facultyToMap(faculty);
        auditLogService.logCreate(userId, "FACULTY", faculty.getId(), newValue);

        System.out.println("Faculty created successfully with id: " + faculty.getId());
        return toDTO(faculty);
    }

    @Transactional
    public FacultyDTO updateFaculty(String id, FacultyDTO dto) {
        System.out.println("Updating faculty with id: " + id);

        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));

        if (faculty.getDeletedAt() != null) {
            throw new RuntimeException("Cannot update deleted faculty");
        }

        if (!faculty.getEmployeeId().equals(dto.getEmployeeId()) &&
                facultyRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new RuntimeException("Faculty with employee ID " + dto.getEmployeeId() + " already exists");
        }

        if (!faculty.getEmail().equals(dto.getEmail()) &&
                facultyRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Faculty with email " + dto.getEmail() + " already exists");
        }

        // Capture old values before update
        Map<String, Object> oldValue = facultyToMap(faculty);

        updateEntity(faculty, dto);
        faculty = facultyRepository.save(faculty);

        // Log the update operation
        String userId = getCurrentUserId();
        Map<String, Object> newValue = facultyToMap(faculty);
        auditLogService.logUpdate(userId, "FACULTY", faculty.getId(), oldValue, newValue);

        System.out.println("Faculty updated successfully");
        return toDTO(faculty);
    }

    @Transactional
    public void deleteFaculty(String id) {
        System.out.println("Soft deleting faculty with id: " + id);

        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));

        // Check for active class allocations
        long activeAllocations = classAllocationRepository.countByFacultyIdAndIsActive(id, true);
        if (activeAllocations > 0) {
            throw new RuntimeException("Cannot delete faculty with active class allocations");
        }

        // Capture old values before delete
        Map<String, Object> oldValue = facultyToMap(faculty);

        faculty.setDeletedAt(LocalDateTime.now());
        faculty.setIsActive(false);
        facultyRepository.save(faculty);

        // Log the delete operation
        String userId = getCurrentUserId();
        auditLogService.logDelete(userId, "FACULTY", faculty.getId(), oldValue);

        System.out.println("Faculty soft deleted successfully");
    }

    public FacultyDTO getFacultyById(String id) {
        System.out.println("Fetching faculty with id: " + id);

        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));

        if (faculty.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Faculty", "id", id);
        }

        return toDTO(faculty);
    }

    public Faculty getFacultyByUserId(String userId) {
        return facultyRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Faculty", "userId", userId));
    }

    public List<ClassAllocationDTO> getAllocationsByUserId(String userId) {
        Faculty faculty = getFacultyByUserId(userId);
        return classAllocationService.getAllocationsByFaculty(faculty.getId());
    }

    public Page<FacultyDTO> getAllFaculty(Pageable pageable) {
        System.out.println("Fetching all faculty with pagination");
        return facultyRepository.findAllActive(pageable).map(this::toDTO);
    }

    public Page<FacultyDTO> searchFaculty(String searchTerm, Pageable pageable) {
        System.out.println("Searching faculty with term: " + searchTerm);
        return facultyRepository.searchFaculty(searchTerm, pageable).map(this::toDTO);
    }

    public long getTotalFaculty() {
        return facultyRepository.countByDeletedAtIsNull();
    }

    private FacultyDTO toDTO(Faculty faculty) {
        return new FacultyDTO(
                faculty.getId(),
                faculty.getEmployeeId(),
                faculty.getFirstName(),
                faculty.getLastName(),
                faculty.getEmail(),
                faculty.getPhone(),
                faculty.getDepartment(),
                faculty.getDesignation(),
                faculty.getProfileImage(),
                faculty.getIsActive()
        );
    }

    private Faculty toEntity(FacultyDTO dto) {
        return new Faculty(
                null,
                null,
                dto.getEmployeeId(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getDepartment(),
                dto.getDesignation(),
                dto.getProfileImage(),
                dto.getIsActive() != null ? dto.getIsActive() : true,
                null,
                null,
                null
        );
    }

    private void updateEntity(Faculty faculty, FacultyDTO dto) {
        faculty.setEmployeeId(dto.getEmployeeId());
        faculty.setFirstName(dto.getFirstName());
        faculty.setLastName(dto.getLastName());
        faculty.setEmail(dto.getEmail());
        faculty.setPhone(dto.getPhone());
        faculty.setDepartment(dto.getDepartment());
        faculty.setDesignation(dto.getDesignation());
        faculty.setProfileImage(dto.getProfileImage());
        if (dto.getIsActive() != null) {
            faculty.setIsActive(dto.getIsActive());
        }
    }

    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                return userPrincipal.getId();
            }
        } catch (Exception e) {
            System.err.println("Could not get current user ID: " + e.getMessage());
        }
        return "SYSTEM";
    }

    private Map<String, Object> facultyToMap(Faculty faculty) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", faculty.getId());
        map.put("employeeId", faculty.getEmployeeId());
        map.put("firstName", faculty.getFirstName());
        map.put("lastName", faculty.getLastName());
        map.put("email", faculty.getEmail());
        map.put("phone", faculty.getPhone());
        map.put("department", faculty.getDepartment());
        map.put("designation", faculty.getDesignation());
        map.put("profileImage", faculty.getProfileImage());
        map.put("isActive", faculty.getIsActive());
        return map;
    }
}

