package com.college.activitytracker.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "performance")
@CompoundIndex(name = "unique_performance", def = "{'studentId': 1, 'subjectId': 1, 'examType': 1, 'semester': 1}", unique = true)
public class Performance {
    
    @Id
    private String id;
    
    private String studentId;
    private String subjectId;
    private String facultyId;
    private String courseId;
    private Integer year;
    private String section;
    private String semester;
    
    private String examType;
    private Double marksObtained;
    private Double totalMarks;
    private Double percentage;
    private String grade;
    
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public Performance() {
    }

    public Performance(String id, String studentId, String subjectId, String facultyId, 
                      String courseId, Integer year, String section, String semester, 
                      String examType, Double marksObtained, Double totalMarks, 
                      Double percentage, String grade, String remarks, 
                      LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.facultyId = facultyId;
        this.courseId = courseId;
        this.year = year;
        this.section = section;
        this.semester = semester;
        this.examType = examType;
        this.marksObtained = marksObtained;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.grade = grade;
        this.remarks = remarks;
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

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
    }

    public Double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
