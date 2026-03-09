// MongoDB Index Creation Script
// This script creates all necessary indexes for optimal query performance
// Run this script using: mongosh mongodb://localhost:27017/student_tracker < mongodb-indexes.js

// Switch to the database
use('student_tracker');

print("Creating database indexes for Student Activity Tracker...\n");

// ============================================
// Users Collection Indexes
// ============================================
print("1. Creating indexes for Users collection...");
db.users.createIndex({ email: 1 }, { unique: true, name: "idx_users_email" });
db.users.createIndex({ role: 1 }, { name: "idx_users_role" });
db.users.createIndex({ deletedAt: 1 }, { name: "idx_users_deletedAt" });
db.users.createIndex({ isActive: 1 }, { name: "idx_users_isActive" });
print("✓ Users indexes created");

// ============================================
// Students Collection Indexes
// ============================================
print("\n2. Creating indexes for Students collection...");
db.students.createIndex({ rollNumber: 1 }, { unique: true, name: "idx_students_rollNumber" });
db.students.createIndex({ email: 1 }, { unique: true, name: "idx_students_email" });
db.students.createIndex({ userId: 1 }, { name: "idx_students_userId" });
db.students.createIndex({ courseId: 1, year: 1, section: 1 }, { name: "idx_students_course_year_section" });
db.students.createIndex({ deletedAt: 1 }, { name: "idx_students_deletedAt" });
db.students.createIndex({ isActive: 1 }, { name: "idx_students_isActive" });
db.students.createIndex({ firstName: 1, lastName: 1 }, { name: "idx_students_name" });
// Text index for full-text search
db.students.createIndex(
  { name: "text", email: "text", rollNumber: "text" },
  { name: "idx_students_text_search" }
);
print("✓ Students indexes created");

// ============================================
// Faculty Collection Indexes
// ============================================
print("\n3. Creating indexes for Faculty collection...");
db.faculty.createIndex({ employeeId: 1 }, { unique: true, name: "idx_faculty_employeeId" });
db.faculty.createIndex({ email: 1 }, { unique: true, name: "idx_faculty_email" });
db.faculty.createIndex({ userId: 1 }, { name: "idx_faculty_userId" });
db.faculty.createIndex({ deletedAt: 1 }, { name: "idx_faculty_deletedAt" });
db.faculty.createIndex({ isActive: 1 }, { name: "idx_faculty_isActive" });
db.faculty.createIndex({ department: 1 }, { name: "idx_faculty_department" });
// Text index for full-text search
db.faculty.createIndex(
  { name: "text", email: "text", employeeId: "text" },
  { name: "idx_faculty_text_search" }
);
print("✓ Faculty indexes created");

// ============================================
// Courses Collection Indexes
// ============================================
print("\n4. Creating indexes for Courses collection...");
db.courses.createIndex({ code: 1 }, { unique: true, name: "idx_courses_code" });
db.courses.createIndex({ name: 1 }, { name: "idx_courses_name" });
// Text index for full-text search
db.courses.createIndex(
  { name: "text", code: "text" },
  { name: "idx_courses_text_search" }
);
print("✓ Courses indexes created");

// ============================================
// Subjects Collection Indexes
// ============================================
print("\n5. Creating indexes for Subjects collection...");
db.subjects.createIndex({ code: 1 }, { unique: true, name: "idx_subjects_code" });
db.subjects.createIndex({ name: 1 }, { name: "idx_subjects_name" });
db.subjects.createIndex({ courseId: 1, semester: 1 }, { name: "idx_subjects_course_semester" });
// Text index for full-text search
db.subjects.createIndex(
  { name: "text", code: "text" },
  { name: "idx_subjects_text_search" }
);
print("✓ Subjects indexes created");

// ============================================
// ClassAllocation Collection Indexes
// ============================================
print("\n6. Creating indexes for ClassAllocation collection...");
db.classAllocations.createIndex({ facultyId: 1, subjectId: 1 }, { name: "idx_classAlloc_faculty_subject" });
db.classAllocations.createIndex({ courseId: 1, year: 1, section: 1, semester: 1 }, { name: "idx_classAlloc_course_year_section_sem" });
db.classAllocations.createIndex({ facultyId: 1 }, { name: "idx_classAlloc_facultyId" });
db.classAllocations.createIndex({ subjectId: 1 }, { name: "idx_classAlloc_subjectId" });
db.classAllocations.createIndex({ isActive: 1 }, { name: "idx_classAlloc_isActive" });
db.classAllocations.createIndex({ academicYear: 1 }, { name: "idx_classAlloc_academicYear" });
// Compound unique index to prevent duplicate allocations
db.classAllocations.createIndex(
  { facultyId: 1, subjectId: 1, courseId: 1, year: 1, section: 1, semester: 1, academicYear: 1 },
  { unique: true, name: "idx_classAlloc_unique_allocation" }
);
print("✓ ClassAllocation indexes created");

// ============================================
// Attendance Collection Indexes
// ============================================
print("\n7. Creating indexes for Attendance collection...");
// Compound unique index to prevent duplicate attendance records
db.attendance.createIndex(
  { studentId: 1, subjectId: 1, date: 1 },
  { unique: true, name: "idx_attendance_student_subject_date" }
);
db.attendance.createIndex({ studentId: 1 }, { name: "idx_attendance_studentId" });
db.attendance.createIndex({ subjectId: 1, date: 1 }, { name: "idx_attendance_subject_date" });
db.attendance.createIndex({ facultyId: 1 }, { name: "idx_attendance_facultyId" });
db.attendance.createIndex({ date: 1 }, { name: "idx_attendance_date" });
db.attendance.createIndex({ status: 1 }, { name: "idx_attendance_status" });
db.attendance.createIndex({ studentId: 1, subjectId: 1 }, { name: "idx_attendance_student_subject" });
print("✓ Attendance indexes created");

// ============================================
// Performance Collection Indexes
// ============================================
print("\n8. Creating indexes for Performance collection...");
db.performance.createIndex({ studentId: 1, subjectId: 1 }, { name: "idx_performance_student_subject" });
db.performance.createIndex({ studentId: 1 }, { name: "idx_performance_studentId" });
db.performance.createIndex({ subjectId: 1 }, { name: "idx_performance_subjectId" });
db.performance.createIndex({ facultyId: 1 }, { name: "idx_performance_facultyId" });
db.performance.createIndex({ assessmentType: 1 }, { name: "idx_performance_assessmentType" });
db.performance.createIndex({ date: 1 }, { name: "idx_performance_date" });
db.performance.createIndex({ studentId: 1, date: 1 }, { name: "idx_performance_student_date" });
print("✓ Performance indexes created");

// ============================================
// Notifications Collection Indexes
// ============================================
print("\n9. Creating indexes for Notifications collection...");
db.notifications.createIndex({ userId: 1, isRead: 1 }, { name: "idx_notifications_user_read" });
db.notifications.createIndex({ userId: 1, createdAt: -1 }, { name: "idx_notifications_user_created" });
db.notifications.createIndex({ createdAt: -1 }, { name: "idx_notifications_createdAt" });
db.notifications.createIndex({ type: 1 }, { name: "idx_notifications_type" });
print("✓ Notifications indexes created");

// ============================================
// Announcements Collection Indexes
// ============================================
print("\n10. Creating indexes for Announcements collection...");
db.announcements.createIndex({ targetRole: 1, isActive: 1 }, { name: "idx_announcements_role_active" });
db.announcements.createIndex({ createdAt: -1 }, { name: "idx_announcements_createdAt" });
db.announcements.createIndex({ createdBy: 1 }, { name: "idx_announcements_createdBy" });
db.announcements.createIndex({ isActive: 1 }, { name: "idx_announcements_isActive" });
print("✓ Announcements indexes created");

// ============================================
// AuditLogs Collection Indexes
// ============================================
print("\n11. Creating indexes for AuditLogs collection...");
db.auditLogs.createIndex({ userId: 1, timestamp: -1 }, { name: "idx_auditLogs_user_timestamp" });
db.auditLogs.createIndex({ entityType: 1, entityId: 1 }, { name: "idx_auditLogs_entity" });
db.auditLogs.createIndex({ timestamp: -1 }, { name: "idx_auditLogs_timestamp" });
db.auditLogs.createIndex({ action: 1 }, { name: "idx_auditLogs_action" });
db.auditLogs.createIndex({ entityType: 1 }, { name: "idx_auditLogs_entityType" });
print("✓ AuditLogs indexes created");

// ============================================
// Display Index Summary
// ============================================
print("\n========================================");
print("Index Creation Summary");
print("========================================");

function displayIndexes(collectionName) {
  print(`\n${collectionName} Collection Indexes:`);
  const indexes = db.getCollection(collectionName).getIndexes();
  indexes.forEach(idx => {
    const keys = Object.keys(idx.key).map(k => `${k}: ${idx.key[k]}`).join(", ");
    const unique = idx.unique ? " [UNIQUE]" : "";
    print(`  - ${idx.name}: { ${keys} }${unique}`);
  });
}

displayIndexes("users");
displayIndexes("students");
displayIndexes("faculty");
displayIndexes("courses");
displayIndexes("subjects");
displayIndexes("classAllocations");
displayIndexes("attendance");
displayIndexes("performance");
displayIndexes("notifications");
displayIndexes("announcements");
displayIndexes("auditLogs");

print("\n========================================");
print("All indexes created successfully!");
print("========================================");
print("\nIndex Benefits:");
print("✓ Optimized query performance for frequently accessed fields");
print("✓ Enforced uniqueness constraints on critical fields");
print("✓ Improved compound query performance");
print("✓ Faster sorting and filtering operations");
print("✓ Enhanced data integrity");
print("========================================");
