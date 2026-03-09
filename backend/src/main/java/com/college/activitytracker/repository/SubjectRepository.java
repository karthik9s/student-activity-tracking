package com.college.activitytracker.repository;

import com.college.activitytracker.model.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends MongoRepository<Subject, String> {
    
    Optional<Subject> findByCode(String code);
    
    boolean existsByCode(String code);
    
    boolean existsByCourseIdAndCode(String courseId, String code);
    
    List<Subject> findByCourseId(String courseId);
    
    List<Subject> findByCourseIdAndSemester(String courseId, Integer semester);
    
    Page<Subject> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
            String code, String name, Pageable pageable);
}
