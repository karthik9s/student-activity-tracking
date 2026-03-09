package com.college.activitytracker.controller;

import com.college.activitytracker.dto.AttendanceDTO;
import com.college.activitytracker.dto.NotificationDTO;
import com.college.activitytracker.dto.PerformanceDTO;
import com.college.activitytracker.dto.StudentDTO;
import com.college.activitytracker.dto.SubjectDTO;
import com.college.activitytracker.security.UserPrincipal;
import com.college.activitytracker.service.AttendanceService;
import com.college.activitytracker.service.ClassAllocationService;
import com.college.activitytracker.service.NotificationService;
import com.college.activitytracker.service.PerformanceService;
import com.college.activitytracker.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/student")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student", description = "Student endpoints")
public class StudentController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final PerformanceService performanceService;
    private final NotificationService notificationService;
    private final ClassAllocationService classAllocationService;

    public StudentController(StudentService studentService,
                            AttendanceService attendanceService,
                            PerformanceService performanceService,
                            NotificationService notificationService,
                            ClassAllocationService classAllocationService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.performanceService = performanceService;
        this.notificationService = notificationService;
        this.classAllocationService = classAllocationService;
    }

    private String getStudentId(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String userId = userPrincipal.getId();
        StudentDTO student = studentService.getStudentByUserId(userId);
        return student.getId();
    }

    @GetMapping("/profile")
    @Operation(summary = "Get my profile", description = "Get logged-in student's profile")
    public ResponseEntity<StudentDTO> getMyProfile(Authentication authentication) {
        String studentId = getStudentId(authentication);
        StudentDTO student = studentService.getStudentById(studentId);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/attendance")
    @Operation(summary = "Get my attendance", description = "Get attendance records for logged-in student")
    public ResponseEntity<List<AttendanceDTO>> getMyAttendance(Authentication authentication) {
        String studentId = getStudentId(authentication);
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/attendance/subject/{subjectId}")
    @Operation(summary = "Get attendance by subject")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceBySubject(
            @PathVariable String subjectId,
            Authentication authentication) {
        String studentId = getStudentId(authentication);
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByStudentAndSubject(studentId, subjectId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/attendance/summary")
    @Operation(summary = "Get attendance summary")
    public ResponseEntity<Map<String, Object>> getAttendanceSummary(Authentication authentication) {
        String studentId = getStudentId(authentication);
        Map<String, Object> summary = attendanceService.getAttendanceSummary(studentId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/performance")
    @Operation(summary = "Get my performance", description = "Get performance records for logged-in student")
    public ResponseEntity<List<PerformanceDTO>> getMyPerformance(Authentication authentication) {
        String studentId = getStudentId(authentication);
        List<PerformanceDTO> performance = performanceService.getPerformanceByStudent(studentId);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/performance/subject/{subjectId}")
    @Operation(summary = "Get performance by subject")
    public ResponseEntity<List<PerformanceDTO>> getPerformanceBySubject(
            @PathVariable String subjectId,
            Authentication authentication) {
        String studentId = getStudentId(authentication);
        List<PerformanceDTO> performance = performanceService.getPerformanceByStudentAndSubject(studentId, subjectId);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/performance/summary")
    @Operation(summary = "Get performance summary")
    public ResponseEntity<Map<String, Object>> getPerformanceSummary(Authentication authentication) {
        String studentId = getStudentId(authentication);
        Map<String, Object> summary = performanceService.getPerformanceSummary(studentId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<DashboardStats> getDashboardStats(Authentication authentication) {
        String studentId = getStudentId(authentication);
        
        Map<String, Object> attendanceSummary = attendanceService.getAttendanceSummary(studentId);
        Map<String, Object> performanceSummary = performanceService.getPerformanceSummary(studentId);
        
        DashboardStats stats = new DashboardStats();
        stats.setOverallAttendance((Double) attendanceSummary.getOrDefault("overallPercentage", 0.0));
        stats.setCurrentGPA((Double) performanceSummary.getOrDefault("gpa", 0.0));
        stats.setTotalSubjects((Integer) performanceSummary.getOrDefault("totalSubjects", 0));
        stats.setLowAttendanceCount((Integer) attendanceSummary.getOrDefault("lowAttendanceCount", 0));
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get my notifications", description = "Get all notifications for logged-in student")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String userId = userPrincipal.getId();
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String userId = userPrincipal.getId();
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String userId = userPrincipal.getId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/notifications/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable String id) {
        NotificationDTO notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/notifications/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String userId = userPrincipal.getId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subjects")
    @Operation(summary = "Get my enrolled subjects", description = "Get all subjects the student is enrolled in based on class allocations")
    public ResponseEntity<List<SubjectDTO>> getMySubjects(Authentication authentication) {
        String studentId = getStudentId(authentication);
        StudentDTO student = studentService.getStudentById(studentId);
        
        // Get subjects based on student's course, year, and section from class allocations
        List<SubjectDTO> subjects = classAllocationService.getSubjectsByStudentCourseYearSection(
            student.getCourseId(), 
            student.getYear(), 
            student.getSection()
        );
        
        return ResponseEntity.ok(subjects);
    }

    @lombok.Data
    public static class DashboardStats {
        private Double overallAttendance;
        private Double currentGPA;
        private Integer totalSubjects;
        private Integer lowAttendanceCount;

        public DashboardStats() {
        }

        public DashboardStats(Double overallAttendance, Double currentGPA, Integer totalSubjects, Integer lowAttendanceCount) {
            this.overallAttendance = overallAttendance;
            this.currentGPA = currentGPA;
            this.totalSubjects = totalSubjects;
            this.lowAttendanceCount = lowAttendanceCount;
        }

        public Double getOverallAttendance() {
            return overallAttendance;
        }

        public void setOverallAttendance(Double overallAttendance) {
            this.overallAttendance = overallAttendance;
        }

        public Double getCurrentGPA() {
            return currentGPA;
        }

        public void setCurrentGPA(Double currentGPA) {
            this.currentGPA = currentGPA;
        }

        public Integer getTotalSubjects() {
            return totalSubjects;
        }

        public void setTotalSubjects(Integer totalSubjects) {
            this.totalSubjects = totalSubjects;
        }

        public Integer getLowAttendanceCount() {
            return lowAttendanceCount;
        }

        public void setLowAttendanceCount(Integer lowAttendanceCount) {
            this.lowAttendanceCount = lowAttendanceCount;
        }
    }
}


