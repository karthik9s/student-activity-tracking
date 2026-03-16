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

    public ReportService(AttendanceRepository attendanceRepository,
                         PerformanceRepository performanceRepository,
                         StudentRepository studentRepository,
                         AttendanceService attendanceService,
                         PerformanceService performanceService) {
        this.attendanceRepository = attendanceRepository;
        this.performanceRepository = performanceRepository;
        this.studentRepository = studentRepository;
        this.attendanceService = attendanceService;
        this.performanceService = performanceService;
    }

    public byte[] generateAttendanceReportPDF(String subjectId, String courseId, Integer year,
                                              String section, LocalDate startDate, LocalDate endDate) {
        String report = "Attendance Report\n"
                + "Subject: " + subjectId + "\n"
                + "Course: " + courseId + "\n"
                + "Year: " + year + "\n"
                + "Section: " + section;
        return report.getBytes();
    }

    public byte[] generatePerformanceReportPDF(String subjectId, String courseId,
                                               Integer year, String section) {
        String report = "Performance Report\n"
                + "Subject: " + subjectId + "\n"
                + "Course: " + courseId + "\n"
                + "Year: " + year + "\n"
                + "Section: " + section;
        return report.getBytes();
    }

    public List<Map<String, Object>> generateAttendanceReport(String subjectId,
                                                               String courseId,
                                                               Integer year,
                                                               String section) {
        List<Student> students = studentRepository.findByCourseIdAndYearAndSection(courseId, year, section);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Student student : students) {
            List<com.college.activitytracker.model.Attendance> records =
                    attendanceRepository.findByStudentIdAndSubjectId(student.getId(), subjectId);

            long present = records.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()))
                    .count();
            long absent = records.stream()
                    .filter(a -> "ABSENT".equals(a.getStatus()))
                    .count();
            long total = records.size();
            double percentage = total > 0 ? (present * 100.0) / total : 0.0;

            Map<String, Object> record = new HashMap<>();
            record.put("studentId", student.getId());
            record.put("name", buildName(student));
            record.put("rollNumber", student.getRollNumber());
            record.put("present", present);
            record.put("absent", absent);
            record.put("totalClasses", total);
            record.put("attendancePercentage", Math.round(percentage * 10.0) / 10.0);
            result.add(record);
        }
        return result;
    }

    public List<Map<String, Object>> generateLowAttendanceList(String subjectId,
                                                                String courseId,
                                                                Integer year,
                                                                String section,
                                                                LocalDate startDate,
                                                                LocalDate endDate) {
        List<Student> students = studentRepository.findByCourseIdAndYearAndSection(courseId, year, section);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Student student : students) {
            List<com.college.activitytracker.model.Attendance> records =
                    attendanceRepository.findByStudentIdAndSubjectId(student.getId(), subjectId);

            long present = records.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()))
                    .count();
            long absent = records.stream()
                    .filter(a -> "ABSENT".equals(a.getStatus()))
                    .count();
            long total = records.size();
            double percentage = total > 0 ? (present * 100.0) / total : 0.0;

            if (percentage < 75) {
                Map<String, Object> record = new HashMap<>();
                record.put("studentId", student.getId());
                record.put("name", buildName(student));
                record.put("rollNumber", student.getRollNumber());
                record.put("present", present);
                record.put("absent", absent);
                record.put("totalClasses", total);
                record.put("attendancePercentage", Math.round(percentage * 10.0) / 10.0);
                result.add(record);
            }
        }
        return result;
    }

    public String generateAttendanceCSV(String subjectId, String courseId,
                                        Integer year, String section,
                                        LocalDate startDate, LocalDate endDate) {
        List<Student> students = studentRepository.findByCourseIdAndYearAndSection(courseId, year, section);
        StringBuilder csv = new StringBuilder();
        csv.append("RollNumber,Name,Percentage\n");

        for (Student student : students) {
            double percentage = attendanceService.calculateAttendancePercentage(student.getId(), subjectId);
            csv.append(student.getRollNumber()).append(",")
               .append(buildName(student)).append(",")
               .append(percentage).append("\n");
        }
        return csv.toString();
    }

    public String generatePerformanceCSV(String subjectId, String courseId,
                                         Integer year, String section) {
        List<Student> students = studentRepository.findByCourseIdAndYearAndSection(courseId, year, section);
        StringBuilder csv = new StringBuilder();
        csv.append("RollNumber,Name\n");

        for (Student student : students) {
            csv.append(student.getRollNumber()).append(",")
               .append(buildName(student)).append("\n");
        }
        return csv.toString();
    }

    private String buildName(Student student) {
        String name = student.getName();
        if (name == null || name.isBlank()) {
            return student.getRollNumber();
        }
        return name;
    }
}
