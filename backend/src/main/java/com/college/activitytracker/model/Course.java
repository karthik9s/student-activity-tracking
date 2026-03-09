package com.college.activitytracker.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "courses")
public class Course {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String code;
    
    private String name;
    
    private String description;
    
    private Integer duration;
    
    private List<Semester> semesters;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Course() {
    }

    public Course(String id, String code, String name, String description, Integer duration, 
                 List<Semester> semesters, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.semesters = semesters;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public List<Semester> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<Semester> semesters) {
        this.semesters = semesters;
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

    public static class Semester {
        private Integer semesterNumber;
        private List<String> subjectIds;

        public Semester() {
        }

        public Semester(Integer semesterNumber, List<String> subjectIds) {
            this.semesterNumber = semesterNumber;
            this.subjectIds = subjectIds;
        }

        public Integer getSemesterNumber() {
            return semesterNumber;
        }

        public void setSemesterNumber(Integer semesterNumber) {
            this.semesterNumber = semesterNumber;
        }

        public List<String> getSubjectIds() {
            return subjectIds;
        }

        public void setSubjectIds(List<String> subjectIds) {
            this.subjectIds = subjectIds;
        }
    }
}
