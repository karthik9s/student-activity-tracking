package com.college.activitytracker.repository;

import com.college.activitytracker.model.Performance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceRepository extends MongoRepository<Performance, String> {
    
    List<Performance> findByStudentId(String studentId);
    
    List<Performance> findByStudentIdAndSubjectId(String studentId, String subjectId);
    
    List<Performance> findByStudentIdAndSemester(String studentId, String semester);
    
    List<Performance> findBySubjectIdAndExamType(String subjectId, String examType);
    
    Optional<Performance> findByStudentIdAndSubjectIdAndExamTypeAndSemester(
            String studentId, String subjectId, String examType, String semester);
    
    @Query("{ 'courseId': ?0, 'year': ?1, 'section': ?2, 'subjectId': ?3, 'examType': ?4 }")
    List<Performance> findByClassAndSubjectAndExamType(
            String courseId, Integer year, String section, String subjectId, String examType);
    
    List<Performance> findBySubjectId(String subjectId);

    Page<Performance> findBySubjectId(String subjectId, Pageable pageable);
    
    @Query("{ 'studentId': ?0, 'subjectId': ?1 }")
    List<Performance> findAllByStudentAndSubject(String studentId, String subjectId);
}
