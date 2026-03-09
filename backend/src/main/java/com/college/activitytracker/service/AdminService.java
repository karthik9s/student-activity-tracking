package com.college.activitytracker.service;

import com.college.activitytracker.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;
    private final PerformanceRepository performanceRepository;

    public AdminService(StudentRepository studentRepository, FacultyRepository facultyRepository,
                       CourseRepository courseRepository, AttendanceRepository attendanceRepository,
                       PerformanceRepository performanceRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.attendanceRepository = attendanceRepository;
        this.performanceRepository = performanceRepository;
    }

    @Cacheable(value = "adminDashboard", unless = "#result == null")
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total counts
        stats.put("totalStudents", getTotalStudentsCount());
        stats.put("totalFaculty", getTotalFacultyCount());
        stats.put("totalCourses", getTotalCoursesCount());
        
        // System-wide statistics
        stats.put("systemStats", getSystemWideStatistics());
        
        return stats;
    }

    public long getTotalStudentsCount() {
        return studentRepository.countByDeletedAtIsNull();
    }

    public long getTotalFacultyCount() {
        return facultyRepository.countByDeletedAtIsNull();
    }

    public long getTotalCoursesCount() {
        return courseRepository.count();
    }

    public Map<String, Object> getSystemWideStatistics() {
        Map<String, Object> systemStats = new HashMap<>();
        
        // Total attendance records
        long totalAttendanceRecords = attendanceRepository.count();
        systemStats.put("totalAttendanceRecords", totalAttendanceRecords);
        
        // Total performance records
        long totalPerformanceRecords = performanceRepository.count();
        systemStats.put("totalPerformanceRecords", totalPerformanceRecords);
        
        // Average attendance percentage (system-wide)
        Double avgAttendance = calculateSystemWideAttendancePercentage();
        systemStats.put("averageAttendancePercentage", avgAttendance != null ? avgAttendance : 0.0);
        
        return systemStats;
    }

    private Double calculateSystemWideAttendancePercentage() {
        try {
            long totalRecords = attendanceRepository.count();
            if (totalRecords == 0) {
                return 0.0;
            }
            long presentRecords = attendanceRepository.countByStatus("PRESENT");
            return (presentRecords * 100.0) / totalRecords;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
