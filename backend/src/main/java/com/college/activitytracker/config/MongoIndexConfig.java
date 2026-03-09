package com.college.activitytracker.config;

import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * MongoDB Index Configuration
 * 
 * This class creates all necessary database indexes for optimal query performance.
 * Indexes are created automatically when the application starts.
 * 
 * According to Requirement 19: Database Design and Performance
 * - Creates indexes on frequently queried fields
 * - Uses compound indexes for multi-field queries
 * - Enforces unique constraints where required
 */
@Component
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    public MongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initIndexes() {
        System.out.println("Starting MongoDB index creation...");
        
        try {
            createUsersIndexes();
            createStudentsIndexes();
            createFacultyIndexes();
            createCoursesIndexes();
            createSubjectsIndexes();
            createClassAllocationIndexes();
            createAttendanceIndexes();
            createPerformanceIndexes();
            createNotificationsIndexes();
            createAnnouncementsIndexes();
            createAuditLogsIndexes();
            
            System.out.println("MongoDB indexes created successfully!");
        } catch (Exception e) {
            System.err.println("Error creating MongoDB indexes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create indexes for Users collection
     * - Unique index on email for fast user lookup and authentication
     * - Index on role for role-based queries
     * - Index on deletedAt for soft delete filtering
     */
    private void createUsersIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("users");
        
        // Unique index on email
        indexOps.ensureIndex(new Index()
            .on("email", Sort.Direction.ASC)
            .unique()
            .named("idx_users_email"));
        
        // Index on role for RBAC queries
        indexOps.ensureIndex(new Index()
            .on("role", Sort.Direction.ASC)
            .named("idx_users_role"));
        
        // Index on deletedAt for soft delete filtering
        indexOps.ensureIndex(new Index()
            .on("deletedAt", Sort.Direction.ASC)
            .named("idx_users_deletedAt"));
        
        // Index on isActive for active user queries
        indexOps.ensureIndex(new Index()
            .on("isActive", Sort.Direction.ASC)
            .named("idx_users_isActive"));
        
        // Logging removed
    }

    /**
     * Create indexes for Students collection
     * - Unique indexes on rollNumber and email
     * - Compound index on course, year, section for class queries
     * - Index on deletedAt for soft delete filtering
     */
    private void createStudentsIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("students");
        
        // Unique index on rollNumber
        indexOps.ensureIndex(new Index()
            .on("rollNumber", Sort.Direction.ASC)
            .unique()
            .named("idx_students_rollNumber"));
        
        // Unique index on email
        indexOps.ensureIndex(new Index()
            .on("email", Sort.Direction.ASC)
            .unique()
            .named("idx_students_email"));
        
        // Index on userId for user-student mapping
        indexOps.ensureIndex(new Index()
            .on("userId", Sort.Direction.ASC)
            .named("idx_students_userId"));
        
        // Compound index for class queries (course + year + section)
        indexOps.ensureIndex(new Index()
            .on("courseId", Sort.Direction.ASC)
            .on("year", Sort.Direction.ASC)
            .on("section", Sort.Direction.ASC)
            .named("idx_students_course_year_section"));
        
        // Index on deletedAt for soft delete filtering
        indexOps.ensureIndex(new Index()
            .on("deletedAt", Sort.Direction.ASC)
            .named("idx_students_deletedAt"));
        
        // Index on isActive
        indexOps.ensureIndex(new Index()
            .on("isActive", Sort.Direction.ASC)
            .named("idx_students_isActive"));
        
        // Compound index on name for search
        indexOps.ensureIndex(new Index()
            .on("firstName", Sort.Direction.ASC)
            .on("lastName", Sort.Direction.ASC)
            .named("idx_students_name"));
        
        // Logging removed
    }

    /**
     * Create indexes for Faculty collection
     * - Unique indexes on employeeId and email
     * - Index on deletedAt for soft delete filtering
     */
    private void createFacultyIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("faculty");
        
        // Unique index on employeeId
        indexOps.ensureIndex(new Index()
            .on("employeeId", Sort.Direction.ASC)
            .unique()
            .named("idx_faculty_employeeId"));
        
        // Unique index on email
        indexOps.ensureIndex(new Index()
            .on("email", Sort.Direction.ASC)
            .unique()
            .named("idx_faculty_email"));
        
        // Index on userId for user-faculty mapping
        indexOps.ensureIndex(new Index()
            .on("userId", Sort.Direction.ASC)
            .named("idx_faculty_userId"));
        
        // Index on deletedAt for soft delete filtering
        indexOps.ensureIndex(new Index()
            .on("deletedAt", Sort.Direction.ASC)
            .named("idx_faculty_deletedAt"));
        
        // Index on isActive
        indexOps.ensureIndex(new Index()
            .on("isActive", Sort.Direction.ASC)
            .named("idx_faculty_isActive"));
        
        // Index on department for department-wise queries
        indexOps.ensureIndex(new Index()
            .on("department", Sort.Direction.ASC)
            .named("idx_faculty_department"));
        
        // Logging removed
    }

    /**
     * Create indexes for Courses collection
     * - Unique index on course code
     */
    private void createCoursesIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("courses");
        
        // Unique index on code
        indexOps.ensureIndex(new Index()
            .on("code", Sort.Direction.ASC)
            .unique()
            .named("idx_courses_code"));
        
        // Index on name for search
        indexOps.ensureIndex(new Index()
            .on("name", Sort.Direction.ASC)
            .named("idx_courses_name"));
        
        // Logging removed
    }

    /**
     * Create indexes for Subjects collection
     * - Unique index on subject code
     * - Compound index on course and semester
     */
    private void createSubjectsIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("subjects");
        
        // Unique index on code
        indexOps.ensureIndex(new Index()
            .on("code", Sort.Direction.ASC)
            .unique()
            .named("idx_subjects_code"));
        
        // Index on name for search
        indexOps.ensureIndex(new Index()
            .on("name", Sort.Direction.ASC)
            .named("idx_subjects_name"));
        
        // Compound index for course-semester queries
        indexOps.ensureIndex(new Index()
            .on("courseId", Sort.Direction.ASC)
            .on("semester", Sort.Direction.ASC)
            .named("idx_subjects_course_semester"));
        
        // Logging removed
    }

    /**
     * Create indexes for ClassAllocation collection
     * - Compound indexes for faculty-subject and course-year-section queries
     * - Unique compound index to prevent duplicate allocations
     */
    private void createClassAllocationIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("classAllocations");
        
        // Compound index for faculty-subject queries
        indexOps.ensureIndex(new Index()
            .on("facultyId", Sort.Direction.ASC)
            .on("subjectId", Sort.Direction.ASC)
            .named("idx_classAlloc_faculty_subject"));
        
        // Compound index for class queries
        indexOps.ensureIndex(new Index()
            .on("courseId", Sort.Direction.ASC)
            .on("year", Sort.Direction.ASC)
            .on("section", Sort.Direction.ASC)
            .on("semester", Sort.Direction.ASC)
            .named("idx_classAlloc_course_year_section_sem"));
        
        // Index on facultyId
        indexOps.ensureIndex(new Index()
            .on("facultyId", Sort.Direction.ASC)
            .named("idx_classAlloc_facultyId"));
        
        // Index on subjectId
        indexOps.ensureIndex(new Index()
            .on("subjectId", Sort.Direction.ASC)
            .named("idx_classAlloc_subjectId"));
        
        // Index on isActive
        indexOps.ensureIndex(new Index()
            .on("isActive", Sort.Direction.ASC)
            .named("idx_classAlloc_isActive"));
        
        // Index on academicYear
        indexOps.ensureIndex(new Index()
            .on("academicYear", Sort.Direction.ASC)
            .named("idx_classAlloc_academicYear"));
        
        // Unique compound index to prevent duplicate allocations
        indexOps.ensureIndex(new Index()
            .on("facultyId", Sort.Direction.ASC)
            .on("subjectId", Sort.Direction.ASC)
            .on("courseId", Sort.Direction.ASC)
            .on("year", Sort.Direction.ASC)
            .on("section", Sort.Direction.ASC)
            .on("semester", Sort.Direction.ASC)
            .on("academicYear", Sort.Direction.ASC)
            .unique()
            .named("idx_classAlloc_unique_allocation"));
        
        // Logging removed
    }

    /**
     * Create indexes for Attendance collection
     * - Unique compound index on student-subject-date to prevent duplicates
     * - Compound indexes for common query patterns
     */
    private void createAttendanceIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("attendance");
        
        // Unique compound index to prevent duplicate attendance records
        indexOps.ensureIndex(new Index()
            .on("studentId", Sort.Direction.ASC)
            .on("subjectId", Sort.Direction.ASC)
            .on("date", Sort.Direction.ASC)
            .unique()
            .named("idx_attendance_student_subject_date"));
        
        // Index on studentId for student attendance queries
        indexOps.ensureIndex(new Index()
            .on("studentId", Sort.Direction.ASC)
            .named("idx_attendance_studentId"));
        
        // Compound index for subject-date queries
        indexOps.ensureIndex(new Index()
            .on("subjectId", Sort.Direction.ASC)
            .on("date", Sort.Direction.ASC)
            .named("idx_attendance_subject_date"));
        
        // Index on facultyId for faculty queries
        indexOps.ensureIndex(new Index()
            .on("facultyId", Sort.Direction.ASC)
            .named("idx_attendance_facultyId"));
        
        // Index on date for date-range queries
        indexOps.ensureIndex(new Index()
            .on("date", Sort.Direction.ASC)
            .named("idx_attendance_date"));
        
        // Index on status for filtering by attendance status
        indexOps.ensureIndex(new Index()
            .on("status", Sort.Direction.ASC)
            .named("idx_attendance_status"));
        
        // Compound index for student-subject queries
        indexOps.ensureIndex(new Index()
            .on("studentId", Sort.Direction.ASC)
            .on("subjectId", Sort.Direction.ASC)
            .named("idx_attendance_student_subject"));
        
        // Logging removed
    }

    /**
     * Create indexes for Performance collection
     * - Compound indexes for student-subject queries
     * - Indexes for common filtering patterns
     */
    private void createPerformanceIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("performance");
        
        // Compound index for student-subject queries
        indexOps.ensureIndex(new Index()
            .on("studentId", Sort.Direction.ASC)
            .on("subjectId", Sort.Direction.ASC)
            .named("idx_performance_student_subject"));
        
        // Index on studentId for student performance queries
        indexOps.ensureIndex(new Index()
            .on("studentId", Sort.Direction.ASC)
            .named("idx_performance_studentId"));
        
        // Index on subjectId for subject performance queries
        indexOps.ensureIndex(new Index()
            .on("subjectId", Sort.Direction.ASC)
            .named("idx_performance_subjectId"));
        
        // Index on facultyId for faculty queries
        indexOps.ensureIndex(new Index()
            .on("facultyId", Sort.Direction.ASC)
            .named("idx_performance_facultyId"));
        
        // Index on assessmentType for filtering by type
        indexOps.ensureIndex(new Index()
            .on("assessmentType", Sort.Direction.ASC)
            .named("idx_performance_assessmentType"));
        
        // Index on date for date-range queries
        indexOps.ensureIndex(new Index()
            .on("date", Sort.Direction.ASC)
            .named("idx_performance_date"));
        
        // Compound index for student-date queries
        indexOps.ensureIndex(new Index()
            .on("studentId", Sort.Direction.ASC)
            .on("date", Sort.Direction.ASC)
            .named("idx_performance_student_date"));
        
        // Logging removed
    }

    /**
     * Create indexes for Notifications collection
     * - Compound index on user-read status for notification queries
     * - Index on createdAt for sorting
     */
    private void createNotificationsIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("notifications");
        
        // Compound index for user-read queries
        indexOps.ensureIndex(new Index()
            .on("userId", Sort.Direction.ASC)
            .on("isRead", Sort.Direction.ASC)
            .named("idx_notifications_user_read"));
        
        // Compound index for user-created queries with sorting
        indexOps.ensureIndex(new Index()
            .on("userId", Sort.Direction.ASC)
            .on("createdAt", Sort.Direction.DESC)
            .named("idx_notifications_user_created"));
        
        // Index on createdAt for sorting
        indexOps.ensureIndex(new Index()
            .on("createdAt", Sort.Direction.DESC)
            .named("idx_notifications_createdAt"));
        
        // Index on type for filtering by notification type
        indexOps.ensureIndex(new Index()
            .on("type", Sort.Direction.ASC)
            .named("idx_notifications_type"));
        
        // Logging removed
    }

    /**
     * Create indexes for Announcements collection
     * - Compound index on targetRole and isActive
     * - Index on createdAt for sorting
     */
    private void createAnnouncementsIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("announcements");
        
        // Compound index for role-active queries
        indexOps.ensureIndex(new Index()
            .on("targetRole", Sort.Direction.ASC)
            .on("isActive", Sort.Direction.ASC)
            .named("idx_announcements_role_active"));
        
        // Index on createdAt for sorting
        indexOps.ensureIndex(new Index()
            .on("createdAt", Sort.Direction.DESC)
            .named("idx_announcements_createdAt"));
        
        // Index on createdBy for creator queries
        indexOps.ensureIndex(new Index()
            .on("createdBy", Sort.Direction.ASC)
            .named("idx_announcements_createdBy"));
        
        // Index on isActive
        indexOps.ensureIndex(new Index()
            .on("isActive", Sort.Direction.ASC)
            .named("idx_announcements_isActive"));
        
        // Logging removed
    }

    /**
     * Create indexes for AuditLogs collection
     * - Compound index on user-timestamp for audit queries
     * - Compound index on entity type and ID
     * - Index on timestamp for sorting
     */
    private void createAuditLogsIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("auditLogs");
        
        // Compound index for user-timestamp queries
        indexOps.ensureIndex(new Index()
            .on("userId", Sort.Direction.ASC)
            .on("timestamp", Sort.Direction.DESC)
            .named("idx_auditLogs_user_timestamp"));
        
        // Compound index for entity queries
        indexOps.ensureIndex(new Index()
            .on("entityType", Sort.Direction.ASC)
            .on("entityId", Sort.Direction.ASC)
            .named("idx_auditLogs_entity"));
        
        // Index on timestamp for sorting
        indexOps.ensureIndex(new Index()
            .on("timestamp", Sort.Direction.DESC)
            .named("idx_auditLogs_timestamp"));
        
        // Index on action for filtering by action type
        indexOps.ensureIndex(new Index()
            .on("action", Sort.Direction.ASC)
            .named("idx_auditLogs_action"));
        
        // Index on entityType for filtering by entity type
        indexOps.ensureIndex(new Index()
            .on("entityType", Sort.Direction.ASC)
            .named("idx_auditLogs_entityType"));
        
        // Logging removed
    }
}




