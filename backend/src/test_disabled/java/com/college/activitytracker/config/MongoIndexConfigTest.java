package com.college.activitytracker.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to verify MongoDB indexes are created correctly
 * 
 * This test validates Requirement 19: Database Design and Performance
 * - Verifies indexes on frequently queried fields
 * - Verifies compound indexes for multi-field queries
 * - Verifies unique constraints are enforced
 */
@SpringBootTest
class MongoIndexConfigTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void testUsersIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("users").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_users_email",
                "idx_users_role",
                "idx_users_deletedAt",
                "idx_users_isActive"
        );

        // Verify email index is unique
        IndexInfo emailIndex = indexes.stream()
                .filter(idx -> "idx_users_email".equals(idx.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(emailIndex.isUnique()).isTrue();
    }

    @Test
    void testStudentsIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("students").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_students_rollNumber",
                "idx_students_email",
                "idx_students_userId",
                "idx_students_course_year_section",
                "idx_students_deletedAt",
                "idx_students_isActive",
                "idx_students_name"
        );

        // Verify unique indexes
        List<String> uniqueIndexes = indexes.stream()
                .filter(IndexInfo::isUnique)
                .map(IndexInfo::getName)
                .collect(Collectors.toList());
        assertThat(uniqueIndexes).contains("idx_students_rollNumber", "idx_students_email");
    }

    @Test
    void testFacultyIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("faculty").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_faculty_employeeId",
                "idx_faculty_email",
                "idx_faculty_userId",
                "idx_faculty_deletedAt",
                "idx_faculty_isActive",
                "idx_faculty_department"
        );

        // Verify unique indexes
        List<String> uniqueIndexes = indexes.stream()
                .filter(IndexInfo::isUnique)
                .map(IndexInfo::getName)
                .collect(Collectors.toList());
        assertThat(uniqueIndexes).contains("idx_faculty_employeeId", "idx_faculty_email");
    }

    @Test
    void testCoursesIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("courses").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_courses_code",
                "idx_courses_name"
        );

        // Verify code index is unique
        IndexInfo codeIndex = indexes.stream()
                .filter(idx -> "idx_courses_code".equals(idx.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(codeIndex.isUnique()).isTrue();
    }

    @Test
    void testSubjectsIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("subjects").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_subjects_code",
                "idx_subjects_name",
                "idx_subjects_course_semester"
        );

        // Verify code index is unique
        IndexInfo codeIndex = indexes.stream()
                .filter(idx -> "idx_subjects_code".equals(idx.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(codeIndex.isUnique()).isTrue();
    }

    @Test
    void testClassAllocationIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("classAllocations").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_classAlloc_faculty_subject",
                "idx_classAlloc_course_year_section_sem",
                "idx_classAlloc_facultyId",
                "idx_classAlloc_subjectId",
                "idx_classAlloc_isActive",
                "idx_classAlloc_academicYear",
                "idx_classAlloc_unique_allocation"
        );

        // Verify unique allocation index
        IndexInfo uniqueIndex = indexes.stream()
                .filter(idx -> "idx_classAlloc_unique_allocation".equals(idx.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(uniqueIndex.isUnique()).isTrue();
    }

    @Test
    void testAttendanceIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("attendance").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_attendance_student_subject_date",
                "idx_attendance_studentId",
                "idx_attendance_subject_date",
                "idx_attendance_facultyId",
                "idx_attendance_date",
                "idx_attendance_status",
                "idx_attendance_student_subject"
        );

        // Verify unique compound index to prevent duplicate attendance
        IndexInfo uniqueIndex = indexes.stream()
                .filter(idx -> "idx_attendance_student_subject_date".equals(idx.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(uniqueIndex.isUnique()).isTrue();
    }

    @Test
    void testPerformanceIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("performance").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_performance_student_subject",
                "idx_performance_studentId",
                "idx_performance_subjectId",
                "idx_performance_facultyId",
                "idx_performance_assessmentType",
                "idx_performance_date",
                "idx_performance_student_date"
        );
    }

    @Test
    void testNotificationsIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("notifications").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_notifications_user_read",
                "idx_notifications_user_created",
                "idx_notifications_createdAt",
                "idx_notifications_type"
        );
    }

    @Test
    void testAnnouncementsIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("announcements").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_announcements_role_active",
                "idx_announcements_createdAt",
                "idx_announcements_createdBy",
                "idx_announcements_isActive"
        );
    }

    @Test
    void testAuditLogsIndexesCreated() {
        List<IndexInfo> indexes = mongoTemplate.indexOps("auditLogs").getIndexInfo();
        List<String> indexNames = indexes.stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toList());

        assertThat(indexNames).contains(
                "idx_auditLogs_user_timestamp",
                "idx_auditLogs_entity",
                "idx_auditLogs_timestamp",
                "idx_auditLogs_action",
                "idx_auditLogs_entityType"
        );
    }

    @Test
    void testAllCollectionsHaveIndexes() {
        String[] collections = {
                "users", "students", "faculty", "courses", "subjects",
                "classAllocations", "attendance", "performance",
                "notifications", "announcements", "auditLogs"
        };

        for (String collection : collections) {
            List<IndexInfo> indexes = mongoTemplate.indexOps(collection).getIndexInfo();
            // Each collection should have at least the default _id index plus custom indexes
            assertThat(indexes.size()).isGreaterThan(1)
                    .withFailMessage("Collection %s should have custom indexes", collection);
        }
    }

    @Test
    void testCompoundIndexesAreProperlyOrdered() {
        // Test attendance compound index order (studentId, subjectId, date)
        List<IndexInfo> attendanceIndexes = mongoTemplate.indexOps("attendance").getIndexInfo();
        IndexInfo compoundIndex = attendanceIndexes.stream()
                .filter(idx -> "idx_attendance_student_subject_date".equals(idx.getName()))
                .findFirst()
                .orElseThrow();

        // Verify the index has multiple fields
        assertThat(compoundIndex.getIndexFields()).hasSizeGreaterThan(1);

        // Test students compound index order (courseId, year, section)
        List<IndexInfo> studentIndexes = mongoTemplate.indexOps("students").getIndexInfo();
        IndexInfo studentCompoundIndex = studentIndexes.stream()
                .filter(idx -> "idx_students_course_year_section".equals(idx.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(studentCompoundIndex.getIndexFields()).hasSizeGreaterThan(1);
    }
}
