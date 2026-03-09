package com.college.activitytracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceDTO {
    
    private String id;
    
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "Subject ID is required")
    private String subjectId;
    
    @NotBlank(message = "Faculty ID is required")
    private String facultyId;
    
    private String courseId;
    private Integer year;
    private String section;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PRESENT|ABSENT", message = "Status must be either PRESENT or ABSENT")
    private String status;
    
    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
    
    private String studentName;
    private String subjectName;
    private String facultyName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public AttendanceDTO() {
    }

    public AttendanceDTO(String id, String studentId, String subjectId, String facultyId, 
                        String courseId, Integer year, String section, LocalDate date, 
                        String status, String remarks, String studentName, String subjectName, 
                        String facultyName, LocalDateTime createdAt, LocalDateTime updatedAt, 
                        String createdBy) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.facultyId = facultyId;
        this.courseId = courseId;
        this.year = year;
        this.section = section;
        this.date = date;
        this.status = status;
        this.remarks = remarks;
        this.studentName = studentName;
        this.subjectName = subjectName;
        this.facultyName = facultyName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
