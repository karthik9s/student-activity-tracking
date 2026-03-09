package com.college.activitytracker.controller;

import com.college.activitytracker.dto.AttendanceDTO;
import com.college.activitytracker.dto.ClassAllocationDTO;
import com.college.activitytracker.dto.PerformanceDTO;
import com.college.activitytracker.dto.StudentDTO;
import com.college.activitytracker.security.UserPrincipal;
import com.college.activitytracker.service.AttendanceService;
import com.college.activitytracker.service.ClassAllocationService;
import com.college.activitytracker.service.FacultyService;
import com.college.activitytracker.service.PerformanceService;
import com.college.activitytracker.service.ReportService;
import com.college.activitytracker.service.StudentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/faculty")
@PreAuthorize("hasRole('FACULTY')")
@Tag(name = "Faculty", description = "Faculty endpoints")
public class FacultyController {

    private final AttendanceService attendanceService;
    private final PerformanceService performanceService;
    private final ClassAllocationService classAllocationService;
    private final ReportService reportService;
    private final FacultyService facultyService;
    private final StudentService studentService;

    public FacultyController(AttendanceService attendanceService,
                            PerformanceService performanceService,
                            ClassAllocationService classAllocationService,
                            ReportService reportService,
                            FacultyService facultyService,
                            StudentService studentService) {
        this.attendanceService = attendanceService;
        this.performanceService = performanceService;
        this.classAllocationService = classAllocationService;
        this.reportService = reportService;
        this.facultyService = facultyService;
        this.studentService = studentService;
    }

    @PostMapping("/attendance")
    @Operation(summary = "Mark attendance", description = "Mark attendance for a student")
    public ResponseEntity<AttendanceDTO> markAttendance(
            @Valid @RequestBody AttendanceDTO attendanceDTO,
            Authentication authentication) {
        attendanceDTO.setCreatedBy(authentication.getName());
        AttendanceDTO created = attendanceService.markAttendance(attendanceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/attendance/bulk")
    @Operation(summary = "Mark bulk attendance", description = "Mark attendance for multiple students")
    public ResponseEntity<List<AttendanceDTO>> markBulkAttendance(
            @Valid @RequestBody List<AttendanceDTO> attendanceDTOs,
            Authentication authentication) {
        attendanceDTOs.forEach(dto -> dto.setCreatedBy(authentication.getName()));
        List<AttendanceDTO> created = attendanceService.markBulkAttendance(attendanceDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/attendance/subject/{subjectId}/date/{date}")
    @Operation(summary = "Get attendance by subject and date")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceBySubjectAndDate(
            @PathVariable String subjectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceDTO> attendance = attendanceService.getAttendanceBySubjectAndDate(subjectId, date);
        return ResponseEntity.ok(attendance);
    }

    @PutMapping("/attendance/{id}")
    @Operation(summary = "Update attendance")
    public ResponseEntity<AttendanceDTO> updateAttendance(
            @PathVariable String id,
            @Valid @RequestBody AttendanceDTO attendanceDTO) {
        AttendanceDTO updated = attendanceService.updateAttendance(id, attendanceDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/performance")
    @Operation(summary = "Add performance", description = "Add performance record for a student")
    public ResponseEntity<PerformanceDTO> addPerformance(
            @Valid @RequestBody PerformanceDTO performanceDTO,
            Authentication authentication) {
        performanceDTO.setCreatedBy(authentication.getName());
        PerformanceDTO created = performanceService.addPerformance(performanceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/performance/subject/{subjectId}")
    @Operation(summary = "Get performance by subject")
    public ResponseEntity<List<PerformanceDTO>> getPerformanceBySubject(
            @PathVariable String subjectId) {
        List<PerformanceDTO> performance = performanceService.getPerformanceByStudentAndSubject(null, subjectId);
        return ResponseEntity.ok(performance);
    }

    @PutMapping("/performance/{id}")
    @Operation(summary = "Update performance")
    public ResponseEntity<PerformanceDTO> updatePerformance(
            @PathVariable String id,
            @Valid @RequestBody PerformanceDTO performanceDTO) {
        PerformanceDTO updated = performanceService.updatePerformance(id, performanceDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/allocations")
    @Operation(summary = "Get my allocations", description = "Get all class allocations for logged-in faculty")
    public ResponseEntity<List<ClassAllocationDTO>> getMyAllocations(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String userId = userPrincipal.getId();
        List<ClassAllocationDTO> allocations = facultyService.getAllocationsByUserId(userId);
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/students")
    @Operation(summary = "Get students by course", description = "Get all students for a specific course")
    public ResponseEntity<List<StudentDTO>> getStudentsByCourse(
            @RequestParam String courseId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String section) {
        List<StudentDTO> students = studentService.getStudentsByCourse(courseId, year, section);
        return ResponseEntity.ok(students);
    }

    // Report Endpoints

    @GetMapping("/reports/attendance/pdf")
    @Operation(summary = "Generate attendance report PDF")
    public ResponseEntity<byte[]> generateAttendanceReportPDF(
            @RequestParam String subjectId,
            @RequestParam String courseId,
            @RequestParam Integer year,
            @RequestParam String section,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] pdfBytes = reportService.generateAttendanceReportPDF(
                subjectId, courseId, year, section, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "attendance-report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/reports/performance/pdf")
    @Operation(summary = "Generate performance report PDF")
    public ResponseEntity<byte[]> generatePerformanceReportPDF(
            @RequestParam String subjectId,
            @RequestParam String courseId,
            @RequestParam Integer year,
            @RequestParam String section) {

        byte[] pdfBytes = reportService.generatePerformanceReportPDF(
                subjectId, courseId, year, section);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "performance-report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/reports/low-attendance")
    @Operation(summary = "Get low attendance list")
    public ResponseEntity<List<java.util.Map<String, Object>>> getLowAttendanceList(
            @RequestParam String subjectId,
            @RequestParam String courseId,
            @RequestParam Integer year,
            @RequestParam String section,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<java.util.Map<String, Object>> lowAttendanceList = reportService.generateLowAttendanceList(
                subjectId, courseId, year, section, startDate, endDate);

        return ResponseEntity.ok(lowAttendanceList);
    }

    @GetMapping("/reports/attendance/csv")
    @Operation(summary = "Export attendance to CSV")
    public ResponseEntity<String> exportAttendanceCSV(
            @RequestParam String subjectId,
            @RequestParam String courseId,
            @RequestParam Integer year,
            @RequestParam String section,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String csv = reportService.generateAttendanceCSV(
                subjectId, courseId, year, section, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "attendance-export.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }

    @GetMapping("/reports/performance/csv")
    @Operation(summary = "Export performance to CSV")
    public ResponseEntity<String> exportPerformanceCSV(
            @RequestParam String subjectId,
            @RequestParam String courseId,
            @RequestParam Integer year,
            @RequestParam String section) {

        String csv = reportService.generatePerformanceCSV(
                subjectId, courseId, year, section);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "performance-export.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }

}


