package com.college.activitytracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CourseDTO {
    
    private String id;
    
    @NotBlank(message = "Course code is required")
    @Size(min = 2, max = 20, message = "Course code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Course code must contain only uppercase letters, numbers, and hyphens")
    private String code;
    
    @NotBlank(message = "Course name is required")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 6, message = "Duration cannot exceed 6 years")
    private Integer duration;
    
    private List<SemesterDTO> semesters;

    public CourseDTO() {
    }

    public CourseDTO(String id, String code, String name, String description, Integer duration, 
                    List<SemesterDTO> semesters) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.semesters = semesters;
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

    public List<SemesterDTO> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<SemesterDTO> semesters) {
        this.semesters = semesters;
    }

    public static class SemesterDTO {
        private Integer semesterNumber;
        private List<String> subjectIds;

        public SemesterDTO() {
        }

        public SemesterDTO(Integer semesterNumber, List<String> subjectIds) {
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
