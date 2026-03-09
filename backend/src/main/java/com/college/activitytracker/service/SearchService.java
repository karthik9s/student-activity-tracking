package com.college.activitytracker.service;

import com.college.activitytracker.model.Student;
import com.college.activitytracker.model.Faculty;
import com.college.activitytracker.model.Course;
import com.college.activitytracker.model.Subject;
import com.college.activitytracker.repository.StudentRepository;
import com.college.activitytracker.repository.FacultyRepository;
import com.college.activitytracker.repository.CourseRepository;
import com.college.activitytracker.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    /**
     * Global search across all entities
     */
    public Map<String, Object> globalSearch(String searchTerm) {
        Map<String, Object> results = new HashMap<>();
        
        // Search students
        List<Student> students = searchStudents(searchTerm);
        results.put("students", students);
        
        // Search faculty
        List<Faculty> faculty = searchFaculty(searchTerm);
        results.put("faculty", faculty);
        
        // Search courses
        List<Course> courses = searchCourses(searchTerm);
        results.put("courses", courses);
        
        // Search subjects
        List<Subject> subjects = searchSubjects(searchTerm);
        results.put("subjects", subjects);
        
        return results;
    }

    /**
     * Search students by name, email, or roll number
     */
    public List<Student> searchStudents(String searchTerm) {
        Query query = new Query();
        Criteria criteria = new Criteria().orOperator(
            Criteria.where("name").regex(searchTerm, "i"),
            Criteria.where("email").regex(searchTerm, "i"),
            Criteria.where("rollNumber").regex(searchTerm, "i")
        );
        query.addCriteria(criteria);
        query.addCriteria(Criteria.where("deletedAt").is(null));
        return mongoTemplate.find(query, Student.class);
    }

    /**
     * Advanced filtering for students with multiple criteria
     */
    public List<Student> filterStudents(String courseId, Integer year, String section, 
                                       Boolean isActive, String searchTerm) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        
        // Always exclude soft-deleted records
        criteriaList.add(Criteria.where("deletedAt").is(null));
        
        // Apply filters
        if (courseId != null && !courseId.isEmpty()) {
            criteriaList.add(Criteria.where("courseId").is(courseId));
        }
        if (year != null) {
            criteriaList.add(Criteria.where("year").is(year));
        }
        if (section != null && !section.isEmpty()) {
            criteriaList.add(Criteria.where("section").is(section));
        }
        if (isActive != null) {
            criteriaList.add(Criteria.where("isActive").is(isActive));
        }
        if (searchTerm != null && !searchTerm.isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("name").regex(searchTerm, "i"),
                Criteria.where("email").regex(searchTerm, "i"),
                Criteria.where("rollNumber").regex(searchTerm, "i")
            );
            criteriaList.add(searchCriteria);
        }
        
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        return mongoTemplate.find(query, Student.class);
    }

    /**
     * Advanced filtering for students with pagination
     */
    public Page<Student> filterStudentsWithPagination(String courseId, Integer year, String section, 
                                                      Boolean isActive, String searchTerm, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        
        // Always exclude soft-deleted records
        criteriaList.add(Criteria.where("deletedAt").is(null));
        
        // Apply filters
        if (courseId != null && !courseId.isEmpty()) {
            criteriaList.add(Criteria.where("courseId").is(courseId));
        }
        if (year != null) {
            criteriaList.add(Criteria.where("year").is(year));
        }
        if (section != null && !section.isEmpty()) {
            criteriaList.add(Criteria.where("section").is(section));
        }
        if (isActive != null) {
            criteriaList.add(Criteria.where("isActive").is(isActive));
        }
        if (searchTerm != null && !searchTerm.isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("name").regex(searchTerm, "i"),
                Criteria.where("email").regex(searchTerm, "i"),
                Criteria.where("rollNumber").regex(searchTerm, "i")
            );
            criteriaList.add(searchCriteria);
        }
        
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        // Get total count
        long total = mongoTemplate.count(query, Student.class);
        
        // Apply pagination
        query.with(pageable);
        
        // Execute query
        List<Student> students = mongoTemplate.find(query, Student.class);
        
        return new PageImpl<>(students, pageable, total);
    }

    /**
     * Search faculty by name, email, or employee ID
     */
    public List<Faculty> searchFaculty(String searchTerm) {
        Query query = new Query();
        Criteria criteria = new Criteria().orOperator(
            Criteria.where("name").regex(searchTerm, "i"),
            Criteria.where("email").regex(searchTerm, "i"),
            Criteria.where("employeeId").regex(searchTerm, "i")
        );
        query.addCriteria(criteria);
        query.addCriteria(Criteria.where("deletedAt").is(null));
        return mongoTemplate.find(query, Faculty.class);
    }

    /**
     * Advanced filtering for faculty with multiple criteria
     */
    public List<Faculty> filterFaculty(String department, Boolean isActive, String searchTerm) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        
        // Always exclude soft-deleted records
        criteriaList.add(Criteria.where("deletedAt").is(null));
        
        // Apply filters
        if (department != null && !department.isEmpty()) {
            criteriaList.add(Criteria.where("department").is(department));
        }
        if (isActive != null) {
            criteriaList.add(Criteria.where("isActive").is(isActive));
        }
        if (searchTerm != null && !searchTerm.isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("name").regex(searchTerm, "i"),
                Criteria.where("email").regex(searchTerm, "i"),
                Criteria.where("employeeId").regex(searchTerm, "i")
            );
            criteriaList.add(searchCriteria);
        }
        
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        return mongoTemplate.find(query, Faculty.class);
    }

    /**
     * Advanced filtering for faculty with pagination
     */
    public Page<Faculty> filterFacultyWithPagination(String department, Boolean isActive, 
                                                     String searchTerm, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        
        // Always exclude soft-deleted records
        criteriaList.add(Criteria.where("deletedAt").is(null));
        
        // Apply filters
        if (department != null && !department.isEmpty()) {
            criteriaList.add(Criteria.where("department").is(department));
        }
        if (isActive != null) {
            criteriaList.add(Criteria.where("isActive").is(isActive));
        }
        if (searchTerm != null && !searchTerm.isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("name").regex(searchTerm, "i"),
                Criteria.where("email").regex(searchTerm, "i"),
                Criteria.where("employeeId").regex(searchTerm, "i")
            );
            criteriaList.add(searchCriteria);
        }
        
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        // Get total count
        long total = mongoTemplate.count(query, Faculty.class);
        
        // Apply pagination
        query.with(pageable);
        
        // Execute query
        List<Faculty> faculty = mongoTemplate.find(query, Faculty.class);
        
        return new PageImpl<>(faculty, pageable, total);
    }

    /**
     * Search courses by name or code
     */
    public List<Course> searchCourses(String searchTerm) {
        Query query = new Query();
        Criteria criteria = new Criteria().orOperator(
            Criteria.where("name").regex(searchTerm, "i"),
            Criteria.where("code").regex(searchTerm, "i")
        );
        query.addCriteria(criteria);
        return mongoTemplate.find(query, Course.class);
    }

    /**
     * Search subjects by name or code
     */
    public List<Subject> searchSubjects(String searchTerm) {
        Query query = new Query();
        Criteria criteria = new Criteria().orOperator(
            Criteria.where("name").regex(searchTerm, "i"),
            Criteria.where("code").regex(searchTerm, "i")
        );
        query.addCriteria(criteria);
        return mongoTemplate.find(query, Subject.class);
    }
}
