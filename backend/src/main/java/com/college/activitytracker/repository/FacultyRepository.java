package com.college.activitytracker.repository;

import com.college.activitytracker.model.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends MongoRepository<Faculty, String> {
    
    Optional<Faculty> findByEmployeeId(String employeeId);
    
    Optional<Faculty> findByUserId(String userId);
    
    boolean existsByEmployeeId(String employeeId);
    
    boolean existsByEmail(String email);
    
    @Query("{ 'deletedAt': null }")
    Page<Faculty> findAllActive(Pageable pageable);
    
    @Query("{ 'department': ?0, 'deletedAt': null }")
    Page<Faculty> findByDepartment(String department, Pageable pageable);
    
    @Query("{ 'isActive': ?0, 'deletedAt': null }")
    Page<Faculty> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("{ '$or': [ { 'firstName': { '$regex': ?0, '$options': 'i' } }, { 'lastName': { '$regex': ?0, '$options': 'i' } }, { 'email': { '$regex': ?0, '$options': 'i' } }, { 'employeeId': { '$regex': ?0, '$options': 'i' } } ], 'deletedAt': null }")
    Page<Faculty> searchFaculty(String searchTerm, Pageable pageable);
    
    long countByDeletedAtIsNull();
}
