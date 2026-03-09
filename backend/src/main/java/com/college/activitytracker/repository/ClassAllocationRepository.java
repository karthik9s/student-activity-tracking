package com.college.activitytracker.repository;

import com.college.activitytracker.model.ClassAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassAllocationRepository extends MongoRepository<ClassAllocation, String> {
    
    List<ClassAllocation> findByFacultyId(String facultyId);
    
    List<ClassAllocation> findByFacultyIdAndIsActive(String facultyId, Boolean isActive);
    
    List<ClassAllocation> findByCourseIdAndYearAndSection(String courseId, Integer year, String section);
    
    List<ClassAllocation> findBySubjectId(String subjectId);
    
    Optional<ClassAllocation> findByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
            String facultyId, String subjectId, String courseId, Integer year, String section);
    
    boolean existsByFacultyIdAndSubjectIdAndCourseIdAndYearAndSection(
            String facultyId, String subjectId, String courseId, Integer year, String section);
    
    long countByFacultyIdAndIsActive(String facultyId, Boolean isActive);
    
    Page<ClassAllocation> findAll(Pageable pageable);
}
