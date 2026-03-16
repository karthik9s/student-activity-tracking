package com.college.activitytracker.util;

import com.college.activitytracker.dto.StudentDTO;
import com.college.activitytracker.model.Course;
import com.college.activitytracker.model.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {
    
    public StudentDTO toDTO(Student student, Course course) {
        if (student == null) {
            return null;
        }
        
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setRollNumber(student.getRollNumber());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setCourseId(student.getCourseId());
        dto.setCourseName(course != null ? course.getName() : null);
        dto.setYear(student.getYear());
        dto.setSection(student.getSection());
        dto.setProfileImage(student.getProfileImage());
        dto.setIsActive(student.getIsActive());
        
        return dto;
    }
    
    public Student toEntity(StudentDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Student student = new Student();
        student.setId(dto.getId());
        student.setRollNumber(dto.getRollNumber());
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setPhone(dto.getPhone());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setCourseId(dto.getCourseId());
        student.setYear(dto.getYear());
        student.setSection(dto.getSection());
        student.setProfileImage(dto.getProfileImage());
        student.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        return student;
    }
    
    public void updateEntity(Student student, StudentDTO dto) {
        if (student == null || dto == null) {
            return;
        }
        
        student.setRollNumber(dto.getRollNumber());
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setPhone(dto.getPhone());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setCourseId(dto.getCourseId());
        student.setYear(dto.getYear());
        student.setSection(dto.getSection());
        student.setProfileImage(dto.getProfileImage());
        if (dto.getIsActive() != null) {
            student.setIsActive(dto.getIsActive());
        }
    }
}
