package com.college.activitytracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class ClassAllocationDTO {
    
    private String id;
    
    @NotBlank(message = "Faculty ID is required")
    private String facultyId;
    
    @NotBlank(message = "Subject ID is required")
    private String subjectId;
    
    @NotBlank(message = "Course ID is required")
    private String courseId;
    
    @NotNull(message = "Year is required")
    @Min(value = 1, message = "Year must be between 1 and 4")
    @Max(value = 4, message = "Year must be between 1 and 4")
    private Integer year;
    
    @NotBlank(message = "Section is required")
    @Pattern(regexp = "^[A-Z]$", message = "Section must be a single uppercase letter (A-Z)")
    private String section;
    
    private String academicYear;
    private String semester;
    
    private Boolean isActive;
    
    private String facultyName;
    private String subjectName;
    private String courseName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ClassAllocationDTO() {
    }

    public ClassAllocationDTO(String id, String facultyId, String subjectId, String courseId, 
                             Integer year, String section, String academicYear, String semester, 
                             Boolean isActive, String facultyName, String subjectName, 
                             String courseName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.facultyId = facultyId;
        this.subjectId = subjectId;
        this.courseId = courseId;
        this.year = year;
        this.section = section;
        this.academicYear = academicYear;
        this.semester = semester;
        this.isActive = isActive;
        this.facultyName = facultyName;
        this.subjectName = subjectName;
        this.courseName = courseName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
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

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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
}
