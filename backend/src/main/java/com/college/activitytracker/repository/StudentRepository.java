package com.college.activitytracker.repository;

import com.college.activitytracker.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
    
    Optional<Student> findByRollNumber(String rollNumber);
    
    Optional<Student> findByUserId(String userId);
    
    boolean existsByRollNumber(String rollNumber);
    
    boolean existsByEmail(String email);
    
    @Query("{ 'deletedAt': null }")
    Page<Student> findAllActive(Pageable pageable);
    
    @Query("{ 'courseId': ?0, 'year': ?1, 'section': ?2, 'deletedAt': null }")
    Page<Student> findByCourseAndYearAndSection(String courseId, Integer year, String section, Pageable pageable);
    
    @Query("{ 'courseId': ?0, 'deletedAt': null }")
    Page<Student> findByCourse(String courseId, Pageable pageable);
    
    @Query("{ 'isActive': ?0, 'deletedAt': null }")
    Page<Student> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("{ '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, { 'email': { '$regex': ?0, '$options': 'i' } }, { 'rollNumber': { '$regex': ?0, '$options': 'i' } } ], 'deletedAt': null }")
    Page<Student> searchStudents(String searchTerm, Pageable pageable);
    
    long countByDeletedAtIsNull();
    
    long countByCourseId(String courseId);


    @Query("{ 'courseId': ?0, 'year': ?1, 'section': ?2, 'deletedAt': null }")
    java.util.List<Student> findByCourseIdAndYearAndSection(String courseId, Integer year, String section);


    @Query("{ 'courseId': ?0, 'year': ?1, 'section': ?2, 'deletedAt': null }")
    java.util.List<Student> findByCourseIdAndYearAndSectionAndDeletedAtIsNull(String courseId, Integer year, String section);

    @Query("{ 'courseId': ?0, 'year': ?1, 'deletedAt': null }")
    java.util.List<Student> findByCourseIdAndYearAndDeletedAtIsNull(String courseId, Integer year);

    @Query("{ 'courseId': ?0, 'deletedAt': null }")
    java.util.List<Student> findByCourseIdAndDeletedAtIsNull(String courseId);

    @Query("{ 'rollNumber': { '$regex': ?0, '$options': 'i' }, 'deletedAt': null }")
    java.util.List<Student> findByRollNumberContaining(String rollNumber);

}
