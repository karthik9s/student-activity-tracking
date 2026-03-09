package com.college.activitytracker.service;

import com.college.activitytracker.model.Student;
import com.college.activitytracker.repository.AttendanceRepository;
import com.college.activitytracker.repository.PerformanceRepository;
import com.college.activitytracker.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final AttendanceRepository attendanceRepository;
    private final PerformanceRepository performanceRepository;
    private final StudentRepository studentRepository;
    private final AttendanceService attendanceService;
    private final PerformanceService performanceService;

    public ReportService(AttendanceRepository attendanceRepository, PerformanceRepository performanceRepository, StudentRepository studentRepository, AttendanceService attendanceService, PerformanceService performanceService) {
        this.attendanceRepository = attendanceRepository;
        this.performanceRepository = performanceRepository;
        this.studentRepository = studentRepository;
        this.attendanceService = attendanceService;
        this.performanceService = performanceService;
    }

    public byte[] generateAttendanceReportPDF(String subjectId, String courseId, Integer year, String section, LocalDate startDate, LocalDate endDate) {
        String report = "Attendance Report\nSubject: " + subjectId + "\nCourse: " + courseId + "\nYear: " + year + "\nSection: " + section;
        return report.getBytes();
    }

    public byte[] generatePerformanceReportPDF(String subjectId, String courseId, Integer year, String section) {
        String report = "Performance Report\nSubject: " + subjectId + "\nCourse: " + courseId + "\nYear: " + year + "\nSection: " + section;
        return report.getBytes();
    }

    public List<Map<String, Object>> generateLowAttendanceList(String subjectId, String courseId, Integer year, String section, LocalDate startDate, LocalDate endDate) {
        List<Student> students = studentRepository.findByCourseIdAndYearAndSection(courseId, year, section);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Student student : students) {
            double percentage = attendanceService.calculateAttendancePercentage(student.getId(), subjectId);
            if (percentage < 75) {
                Map<String, Object> record = new HashMap<>();
                record.put("studentId", student.getId());
                record.put("name", student.getFirstName() + " " + student.getLastName());
                record.put("rollNumber", student.getRollNumber());
                record.put("attendancePercentage", percentage);
                result.add(record);
            }
        }
        return result;
    }

    public String generateAttendanceCSV(String subjectId, String courseId, Integer year, String section, LocalDate startDate, LocalDate endDate) {
        List<Student> students = studentRepository.findByCourseIdAndYearAndSection(courseId, year, section);
        StringBuilder csv = new StringBuilder();
        csv.append("RollNumber,Name,Percentage\n");
        for (Student student : students) {
            double percentage = attendanceService.calculateAttendancePercentage(student.getId(), subjectId);
            csv.append(student.getRollNumber()).append(",").append(student.getFirstName()).append(" ").append(student.getLastName()).append(",").append(percentage).append("\n");
        }
        return csv.toString();
    }

    public String generatePerformanceCSV(String subjectId, String courseId, Integer year, String section) {
        List<Student> students = studentRepository.findByCourseIdAndYearAndSection(courseId, year, section);
        StringBuilder csv = new StringBuilder();
        csv.append("RollNumber,Name\n");
        for (Student student : students) {
            csv.append(student.getRollNumber()).append(",").append(student.getFirstName()).append(" ").append(student.getLastName()).append("\n");
        }
        return csv.toString();
    }
}
