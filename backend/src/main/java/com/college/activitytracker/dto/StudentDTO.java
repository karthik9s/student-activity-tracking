package com.college.activitytracker.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class StudentDTO {
    
    private String id;
    
    @NotBlank(message = "Roll number is required")
    private String rollNumber;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Course is required")
    private String courseId;
    
    private String courseName;
    
    @NotNull(message = "Year is required")
    @Min(value = 1, message = "Year must be between 1 and 4")
    @Max(value = 4, message = "Year must be between 1 and 4")
    private Integer year;
    
    @NotBlank(message = "Section is required")
    @Pattern(regexp = "^[A-Z]$", message = "Section must be a single uppercase letter (A-Z)")
    private String section;
    
    private String profileImage;
    
    private Boolean isActive;

    public StudentDTO() {
    }

    public StudentDTO(String id, String rollNumber, String name,
                     String email, String phone, LocalDate dateOfBirth, String courseId, 
                     String courseName, Integer year, String section, String profileImage, 
                     Boolean isActive) {
        this.id = id;
        this.rollNumber = rollNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.courseId = courseId;
        this.courseName = courseName;
        this.year = year;
        this.section = section;
        this.profileImage = profileImage;
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
