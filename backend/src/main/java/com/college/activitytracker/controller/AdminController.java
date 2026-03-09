package com.college.activitytracker.controller;

import com.college.activitytracker.dto.BulkUploadResultDTO;
import com.college.activitytracker.dto.StudentDTO;
import com.college.activitytracker.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {

    private final StudentService studentService;
    private final com.college.activitytracker.service.ExcelUploadService excelUploadService;
    private final com.college.activitytracker.service.FacultyService facultyService;
    private final com.college.activitytracker.service.CourseService courseService;
    private final com.college.activitytracker.service.SubjectService subjectService;
    private final com.college.activitytracker.service.ClassAllocationService classAllocationService;
    private final com.college.activitytracker.service.AdminService adminService;
    private final com.college.activitytracker.service.AuditLogService auditLogService;

    public AdminController(StudentService studentService,
                          com.college.activitytracker.service.ExcelUploadService excelUploadService,
                          com.college.activitytracker.service.FacultyService facultyService,
                          com.college.activitytracker.service.CourseService courseService,
                          com.college.activitytracker.service.SubjectService subjectService,
                          com.college.activitytracker.service.ClassAllocationService classAllocationService,
                          com.college.activitytracker.service.AdminService adminService,
                          com.college.activitytracker.service.AuditLogService auditLogService) {
        this.studentService = studentService;
        this.excelUploadService = excelUploadService;
        this.facultyService = facultyService;
        this.courseService = courseService;
        this.subjectService = subjectService;
        this.classAllocationService = classAllocationService;
        this.adminService = adminService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/students")
    @Operation(summary = "Create student", description = "Create a new student record")
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO created = studentService.createStudent(studentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/students")
    @Operation(summary = "Get all students", description = "Get paginated list of students")
    public ResponseEntity<Page<StudentDTO>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) Boolean isActive
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<StudentDTO> students;
        
        if (search != null && !search.isEmpty()) {
            students = studentService.searchStudents(search, pageable);
        } else if (courseId != null && !courseId.isEmpty()) {
            students = studentService.getStudentsByCourse(courseId, pageable);
        } else if (isActive != null) {
            students = studentService.getStudentsByStatus(isActive, pageable);
        } else {
            students = studentService.getAllStudents(pageable);
        }
        
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "Get student by ID", description = "Get student details by ID")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable String id) {
        StudentDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/students/{id}")
    @Operation(summary = "Update student", description = "Update student details")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable String id,
            @Valid @RequestBody StudentDTO studentDTO
    ) {
        StudentDTO updated = studentService.updateStudent(id, studentDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/students/{id}")
    @Operation(summary = "Delete student", description = "Soft delete student")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/students/bulk-upload")
    @Operation(summary = "Bulk upload students", description = "Upload students from Excel file")
    public ResponseEntity<com.college.activitytracker.dto.BulkUploadResultDTO> bulkUploadStudents(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            com.college.activitytracker.dto.BulkUploadResultDTO result = excelUploadService.uploadStudentsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            BulkUploadResultDTO.RowError rowError = new BulkUploadResultDTO.RowError();
            rowError.setRowNumber(0);
            rowError.setRollNumber("");
            rowError.setErrors(java.util.Collections.singletonList(e.getMessage()));
            
            BulkUploadResultDTO result = new BulkUploadResultDTO();
            result.setTotalRows(0);
            result.setSuccessCount(0);
            result.setErrorCount(1);
            result.setErrors(java.util.Collections.singletonList(rowError));
            
            return ResponseEntity.badRequest().body(result);
        } catch (java.io.IOException e) {
            BulkUploadResultDTO.RowError rowError = new BulkUploadResultDTO.RowError();
            rowError.setRowNumber(0);
            rowError.setRollNumber("");
            rowError.setErrors(java.util.Collections.singletonList("Error reading file: " + e.getMessage()));
            
            BulkUploadResultDTO result = new BulkUploadResultDTO();
            result.setTotalRows(0);
            result.setSuccessCount(0);
            result.setErrorCount(1);
            result.setErrors(java.util.Collections.singletonList(rowError));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard statistics", description = "Get admin dashboard statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = adminService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }

    // Faculty Management Endpoints
    
    @PostMapping("/faculty")
    @Operation(summary = "Create faculty", description = "Create a new faculty member")
    public ResponseEntity<com.college.activitytracker.dto.FacultyDTO> createFaculty(
            @Valid @RequestBody com.college.activitytracker.dto.FacultyDTO facultyDTO) {
        com.college.activitytracker.dto.FacultyDTO created = facultyService.createFaculty(facultyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/faculty")
    @Operation(summary = "Get all faculty", description = "Get paginated list of faculty")
    public ResponseEntity<Page<com.college.activitytracker.dto.FacultyDTO>> getAllFaculty(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String search
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.college.activitytracker.dto.FacultyDTO> faculty;
        
        if (search != null && !search.isEmpty()) {
            faculty = facultyService.searchFaculty(search, pageable);
        } else {
            faculty = facultyService.getAllFaculty(pageable);
        }
        
        return ResponseEntity.ok(faculty);
    }

    @GetMapping("/faculty/{id}")
    @Operation(summary = "Get faculty by ID", description = "Get faculty details by ID")
    public ResponseEntity<com.college.activitytracker.dto.FacultyDTO> getFacultyById(@PathVariable String id) {
        com.college.activitytracker.dto.FacultyDTO faculty = facultyService.getFacultyById(id);
        return ResponseEntity.ok(faculty);
    }

    @PutMapping("/faculty/{id}")
    @Operation(summary = "Update faculty", description = "Update faculty details")
    public ResponseEntity<com.college.activitytracker.dto.FacultyDTO> updateFaculty(
            @PathVariable String id,
            @Valid @RequestBody com.college.activitytracker.dto.FacultyDTO facultyDTO
    ) {
        com.college.activitytracker.dto.FacultyDTO updated = facultyService.updateFaculty(id, facultyDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/faculty/{id}")
    @Operation(summary = "Delete faculty", description = "Soft delete faculty")
    public ResponseEntity<Void> deleteFaculty(@PathVariable String id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.noContent().build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardStats {
        private Long totalStudents;
        private Long totalFaculty;
        private Long totalCourses;
    }

    // Course Management Endpoints
    
    @PostMapping("/courses")
    @Operation(summary = "Create course", description = "Create a new course")
    public ResponseEntity<com.college.activitytracker.dto.CourseDTO> createCourse(
            @Valid @RequestBody com.college.activitytracker.dto.CourseDTO courseDTO) {
        com.college.activitytracker.dto.CourseDTO created = courseService.createCourse(courseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/courses")
    @Operation(summary = "Get all courses", description = "Get paginated list of courses")
    public ResponseEntity<Page<com.college.activitytracker.dto.CourseDTO>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String search
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.college.activitytracker.dto.CourseDTO> courses;
        
        if (search != null && !search.isEmpty()) {
            courses = courseService.searchCourses(search, pageable);
        } else {
            courses = courseService.getAllCourses(pageable);
        }
        
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/courses/{id}")
    @Operation(summary = "Get course by ID", description = "Get course details by ID")
    public ResponseEntity<com.college.activitytracker.dto.CourseDTO> getCourseById(@PathVariable String id) {
        com.college.activitytracker.dto.CourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/courses/{id}")
    @Operation(summary = "Update course", description = "Update course details")
    public ResponseEntity<com.college.activitytracker.dto.CourseDTO> updateCourse(
            @PathVariable String id,
            @Valid @RequestBody com.college.activitytracker.dto.CourseDTO courseDTO
    ) {
        com.college.activitytracker.dto.CourseDTO updated = courseService.updateCourse(id, courseDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/courses/{id}")
    @Operation(summary = "Delete course", description = "Delete course")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // Subject Management Endpoints
    
    @PostMapping("/subjects")
    @Operation(summary = "Create subject", description = "Create a new subject")
    public ResponseEntity<com.college.activitytracker.dto.SubjectDTO> createSubject(
            @Valid @RequestBody com.college.activitytracker.dto.SubjectDTO subjectDTO) {
        com.college.activitytracker.dto.SubjectDTO created = subjectService.createSubject(subjectDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/subjects")
    @Operation(summary = "Get all subjects", description = "Get paginated list of subjects")
    public ResponseEntity<Page<com.college.activitytracker.dto.SubjectDTO>> getAllSubjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String courseId
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.college.activitytracker.dto.SubjectDTO> subjects;
        
        if (search != null && !search.isEmpty()) {
            subjects = subjectService.searchSubjects(search, pageable);
        } else {
            subjects = subjectService.getAllSubjects(pageable);
        }
        
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/subjects/{id}")
    @Operation(summary = "Get subject by ID", description = "Get subject details by ID")
    public ResponseEntity<com.college.activitytracker.dto.SubjectDTO> getSubjectById(@PathVariable String id) {
        com.college.activitytracker.dto.SubjectDTO subject = subjectService.getSubjectById(id);
        return ResponseEntity.ok(subject);
    }

    @GetMapping("/courses/{courseId}/subjects")
    @Operation(summary = "Get subjects by course", description = "Get all subjects for a course")
    public ResponseEntity<java.util.List<com.college.activitytracker.dto.SubjectDTO>> getSubjectsByCourse(
            @PathVariable String courseId) {
        java.util.List<com.college.activitytracker.dto.SubjectDTO> subjects = subjectService.getSubjectsByCourse(courseId);
        return ResponseEntity.ok(subjects);
    }

    @PutMapping("/subjects/{id}")
    @Operation(summary = "Update subject", description = "Update subject details")
    public ResponseEntity<com.college.activitytracker.dto.SubjectDTO> updateSubject(
            @PathVariable String id,
            @Valid @RequestBody com.college.activitytracker.dto.SubjectDTO subjectDTO
    ) {
        com.college.activitytracker.dto.SubjectDTO updated = subjectService.updateSubject(id, subjectDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/subjects/{id}")
    @Operation(summary = "Delete subject", description = "Delete subject")
    public ResponseEntity<Void> deleteSubject(@PathVariable String id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    // Class Allocation Endpoints
    
    @PostMapping("/allocations")
    @Operation(summary = "Create class allocation", description = "Allocate faculty to class")
    public ResponseEntity<com.college.activitytracker.dto.ClassAllocationDTO> createAllocation(
            @Valid @RequestBody com.college.activitytracker.dto.ClassAllocationDTO allocationDTO) {
        com.college.activitytracker.dto.ClassAllocationDTO created = classAllocationService.createAllocation(allocationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/allocations")
    @Operation(summary = "Get all allocations", description = "Get paginated list of allocations")
    public ResponseEntity<Page<com.college.activitytracker.dto.ClassAllocationDTO>> getAllAllocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.college.activitytracker.dto.ClassAllocationDTO> allocations = 
                classAllocationService.getAllAllocations(pageable);
        
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/allocations/{id}")
    @Operation(summary = "Get allocation by ID", description = "Get allocation details by ID")
    public ResponseEntity<com.college.activitytracker.dto.ClassAllocationDTO> getAllocationById(@PathVariable String id) {
        com.college.activitytracker.dto.ClassAllocationDTO allocation = classAllocationService.getAllocationById(id);
        return ResponseEntity.ok(allocation);
    }

    @GetMapping("/faculty/{facultyId}/allocations")
    @Operation(summary = "Get allocations by faculty", description = "Get all allocations for a faculty")
    public ResponseEntity<java.util.List<com.college.activitytracker.dto.ClassAllocationDTO>> getAllocationsByFaculty(
            @PathVariable String facultyId) {
        java.util.List<com.college.activitytracker.dto.ClassAllocationDTO> allocations = 
                classAllocationService.getAllocationsByFaculty(facultyId);
        return ResponseEntity.ok(allocations);
    }

    @PutMapping("/allocations/{id}")
    @Operation(summary = "Update allocation", description = "Update allocation details")
    public ResponseEntity<com.college.activitytracker.dto.ClassAllocationDTO> updateAllocation(
            @PathVariable String id,
            @Valid @RequestBody com.college.activitytracker.dto.ClassAllocationDTO allocationDTO
    ) {
        com.college.activitytracker.dto.ClassAllocationDTO updated = classAllocationService.updateAllocation(id, allocationDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/allocations/{id}")
    @Operation(summary = "Delete allocation", description = "Delete allocation")
    public ResponseEntity<Void> deleteAllocation(@PathVariable String id) {
        classAllocationService.deleteAllocation(id);
        return ResponseEntity.noContent().build();
    }

    // Audit Log Endpoints
    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs", description = "Get audit logs with optional filtering")
    public ResponseEntity<Page<com.college.activitytracker.dto.AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String[] sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sort[0]
        ));

        Page<com.college.activitytracker.dto.AuditLogDTO> logs;

        if (userId != null) {
            logs = auditLogService.getAuditLogsByUserId(userId, pageable);
        } else if (entityType != null) {
            logs = auditLogService.getAuditLogsByEntityType(entityType, pageable);
        } else if (action != null) {
            logs = auditLogService.getAuditLogsByAction(action, pageable);
        } else if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            logs = auditLogService.getAuditLogsByDateRange(start, end, pageable);
        } else {
            logs = auditLogService.getAuditLogs(pageable);
        }

        return ResponseEntity.ok(logs);
    }
}


