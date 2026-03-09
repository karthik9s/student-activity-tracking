package com.college.activitytracker.repository;

import com.college.activitytracker.model.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
    
    List<Attendance> findByStudentIdAndSubjectId(String studentId, String subjectId);
    
    List<Attendance> findByStudentId(String studentId);
    
    List<Attendance> findBySubjectIdAndDate(String subjectId, LocalDate date);
    
    List<Attendance> findByCourseIdAndYearAndSectionAndSubjectIdAndDate(
            String courseId, Integer year, String section, String subjectId, LocalDate date);
    
    Optional<Attendance> findByStudentIdAndSubjectIdAndDate(String studentId, String subjectId, LocalDate date);
    
    @Query("{ 'studentId': ?0, 'subjectId': ?1, 'date': { $gte: ?2, $lte: ?3 } }")
    List<Attendance> findByStudentAndSubjectBetweenDates(
            String studentId, String subjectId, LocalDate startDate, LocalDate endDate);
    
    @Query("{ 'studentId': ?0, 'date': { $gte: ?1, $lte: ?2 } }")
    List<Attendance> findByStudentBetweenDates(String studentId, LocalDate startDate, LocalDate endDate);
    
    long countByStudentIdAndSubjectIdAndStatus(String studentId, String subjectId, String status);
    
    long countByStatus(String status);
    
    Page<Attendance> findBySubjectIdAndDate(String subjectId, LocalDate date, Pageable pageable);
}
