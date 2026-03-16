// MongoDB Schema Validation Setup Script
// Run this script using: mongosh <connection-string> mongodb-schema-validation.js

// Switch to the database
db = db.getSiblingDB('student_activity_tracker');

print('Setting up MongoDB schema validation for all collections...\n');

// 1. Users Collection Schema Validation
print('Creating schema validation for users collection...');
db.runCommand({
  collMod: 'users',
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['email', 'password', 'role', 'isActive'],
      properties: {
        _id: { bsonType: 'objectId' },
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$',
          description: 'must be a valid email address'
        },
        password: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string (BCrypt hashed)'
        },
        role: {
          enum: ['ROLE_ADMIN', 'ROLE_FACULTY', 'ROLE_STUDENT'],
          description: 'must be one of: ROLE_ADMIN, ROLE_FACULTY, ROLE_STUDENT'
        },
        isActive: {
          bsonType: 'bool',
          description: 'must be a boolean'
        },
        lastLogin: {
          bsonType: ['date', 'null'],
          description: 'must be a date or null'
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        deletedAt: {
          bsonType: ['date', 'null'],
          description: 'must be a date or null'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Users collection validation created\n');

// 2. Students Collection Schema Validation
print('Creating schema validation for students collection...');
db.runCommand({
  collMod: 'students',
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['userId', 'rollNumber', 'firstName', 'lastName', 'email', 'courseId', 'year', 'section', 'isActive'],
      properties: {
        _id: { bsonType: 'objectId' },
        userId: {
          bsonType: 'string',
          description: 'must be a string reference to users collection'
        },
        rollNumber: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a unique non-empty string'
        },
        firstName: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        lastName: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$',
          description: 'must be a valid email address'
        },
        phone: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        dateOfBirth: {
          bsonType: ['date', 'null'],
          description: 'must be a date or null'
        },
        courseId: {
          bsonType: 'string',
          description: 'must be a string reference to courses collection'
        },
        year: {
          bsonType: 'int',
          minimum: 1,
          maximum: 4,
          description: 'must be an integer between 1 and 4'
        },
        section: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        profileImage: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        isActive: {
          bsonType: 'bool',
          description: 'must be a boolean'
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        deletedAt: {
          bsonType: ['date', 'null'],
          description: 'must be a date or null'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Students collection validation created\n');

// 3. Faculty Collection Schema Validation
print('Creating schema validation for faculty collection...');
db.runCommand({
  collMod: 'faculty',
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['userId', 'employeeId', 'firstName', 'lastName', 'email', 'department', 'isActive'],
      properties: {
        _id: { bsonType: 'objectId' },
        userId: {
          bsonType: 'string',
          description: 'must be a string reference to users collection'
        },
        employeeId: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a unique non-empty string'
        },
        firstName: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        lastName: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$',
          description: 'must be a valid email address'
        },
        phone: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        department: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        designation: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        profileImage: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        isActive: {
          bsonType: 'bool',
          description: 'must be a boolean'
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        deletedAt: {
          bsonType: ['date', 'null'],
          description: 'must be a date or null'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Faculty collection validation created\n');

// 4. Courses Collection Schema Validation
print('Creating schema validation for courses collection...');
db.runCommand({
  collMod: 'courses',
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['code', 'name', 'duration'],
      properties: {
        _id: { bsonType: 'objectId' },
        code: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a unique non-empty string'
        },
        name: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        description: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        duration: {
          bsonType: 'int',
          minimum: 1,
          description: 'must be a positive integer (years)'
        },
        semesters: {
          bsonType: ['array', 'null'],
          items: {
            bsonType: 'object',
            required: ['semesterNumber'],
            properties: {
              semesterNumber: {
                bsonType: 'int',
                minimum: 1,
                description: 'must be a positive integer'
              },
              subjectIds: {
                bsonType: ['array', 'null'],
                items: {
                  bsonType: 'string',
                  description: 'must be string references to subjects collection'
                }
              }
            }
          }
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'must be a date'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Courses collection validation created\n');

// 5. Subjects Collection Schema Validation
print('Creating schema validation for subjects collection...');
db.runCommand({
  collMod: 'subjects',
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['code', 'name', 'credits'],
      properties: {
        _id: { bsonType: 'objectId' },
        code: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a unique non-empty string'
        },
        name: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        description: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        credits: {
          bsonType: 'int',
          minimum: 1,
          maximum: 10,
          description: 'must be an integer between 1 and 10'
        },
        courseId: {
          bsonType: ['string', 'null'],
          description: 'must be a string reference to courses collection or null'
        },
        semester: {
          bsonType: ['int', 'null'],
          minimum: 1,
          description: 'must be a positive integer or null'
        },
        type: {
          enum: ['THEORY', 'PRACTICAL', 'LAB', null],
          description: 'must be one of: THEORY, PRACTICAL, LAB, or null'
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'must be a date'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Subjects collection validation created\n');

// 6. Class Allocations Collection Schema Validation
print('Creating schema validation for class_allocations collection...');
db.runCommand({
  collMod: 'class_allocations',
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['facultyId', 'subjectId', 'courseId', 'year', 'section', 'academicYear', 'isActive'],
      properties: {
        _id: { bsonType: 'objectId' },
        facultyId: {
          bsonType: 'string',
          description: 'must be a string reference to faculty collection'
        },
        subjectId: {
          bsonType: 'string',
          description: 'must be a string reference to subjects collection'
        },
        courseId: {
          bsonType: 'string',
          description: 'must be a string reference to courses collection'
        },
        year: {
          bsonType: 'int',
          minimum: 1,
          maximum: 4,
          description: 'must be an integer between 1 and 4'
        },
        section: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        academicYear: {
          bsonType: 'string',
          pattern: '^\\d{4}-\\d{4}$',
          description: 'must be in format YYYY-YYYY (e.g., 2023-2024)'
        },
        semester: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        isActive: {
          bsonType: 'bool',
          description: 'must be a boolean'
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'must be a date'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Class Allocations collection validation created\n');

// 7. Attendance Collection Schema Validation
print('Creating schema validation for attendance collection...');
db.runCommand({
  collMod: 'attendance',
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['studentId', 'subjectId', 'facultyId', 'date', 'status'],
      properties: {
        _id: { bsonType: 'objectId' },
        studentId: {
          bsonType: 'string',
          description: 'must be a string reference to students collection'
        },
        subjectId: {
          bsonType: 'string',
          description: 'must be a string reference to subjects collection'
        },
        facultyId: {
          bsonType: 'string',
          description: 'must be a string reference to faculty collection'
        },
        courseId: {
          bsonType: ['string', 'null'],
          description: 'must be a string reference to courses collection or null'
        },
        year: {
          bsonType: ['int', 'null'],
          minimum: 1,
          maximum: 4,
          description: 'must be an integer between 1 and 4 or null'
        },
        section: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        date: {
          bsonType: 'date',
          description: 'must be a date'
        },
        status: {
          enum: ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED'],
          description: 'must be one of: PRESENT, ABSENT, LATE, EXCUSED'
        },
        remarks: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        createdBy: {
          bsonType: ['string', 'null'],
          description: 'must be a string reference to users collection or null'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Attendance collection validation created\n');

// 8. Performance Collection Schema Validation
print('Creating schema validation for performance collection...');
db.runCommand({
  collMod: 'performance',
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['studentId', 'subjectId', 'examType', 'marksObtained', 'totalMarks'],
      properties: {
        _id: { bsonType: 'objectId' },
        studentId: {
          bsonType: 'string',
          description: 'must be a string reference to students collection'
        },
        subjectId: {
          bsonType: 'string',
          description: 'must be a string reference to subjects collection'
        },
        courseId: {
          bsonType: ['string', 'null'],
          description: 'must be a string reference to courses collection or null'
        },
        year: {
          bsonType: ['int', 'null'],
          minimum: 1,
          maximum: 4,
          description: 'must be an integer between 1 and 4 or null'
        },
        section: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        semester: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        examType: {
          enum: ['INTERNAL', 'ASSIGNMENT', 'EXAM', 'FINAL'],
          description: 'must be one of: INTERNAL, ASSIGNMENT, EXAM, FINAL'
        },
        marksObtained: {
          bsonType: 'double',
          minimum: 0,
          description: 'must be a non-negative number'
        },
        totalMarks: {
          bsonType: 'double',
          minimum: 0,
          description: 'must be a positive number'
        },
        percentage: {
          bsonType: ['double', 'null'],
          minimum: 0,
          maximum: 100,
          description: 'must be a number between 0 and 100 or null'
        },
        grade: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        remarks: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'must be a date'
        },
        createdBy: {
          bsonType: ['string', 'null'],
          description: 'must be a string reference to users collection or null'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Performance collection validation created\n');

// 9. Notifications Collection Schema Validation (for future use)
print('Creating schema validation for notifications collection...');
db.createCollection('notifications', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['userId', 'type', 'title', 'message', 'isRead'],
      properties: {
        _id: { bsonType: 'objectId' },
        userId: {
          bsonType: 'string',
          description: 'must be a string reference to users collection'
        },
        type: {
          enum: ['LOW_ATTENDANCE', 'PERFORMANCE_UPDATE', 'ANNOUNCEMENT', 'SYSTEM'],
          description: 'must be one of: LOW_ATTENDANCE, PERFORMANCE_UPDATE, ANNOUNCEMENT, SYSTEM'
        },
        title: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        message: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string'
        },
        isRead: {
          bsonType: 'bool',
          description: 'must be a boolean'
        },
        metadata: {
          bsonType: ['object', 'null'],
          description: 'must be an object or null'
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Notifications collection validation created\n');

// 10. Audit Logs Collection Schema Validation (for future use)
print('Creating schema validation for audit_logs collection...');
db.createCollection('audit_logs', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['userId', 'action', 'entityType', 'timestamp'],
      properties: {
        _id: { bsonType: 'objectId' },
        userId: {
          bsonType: 'string',
          description: 'must be a string reference to users collection'
        },
        action: {
          enum: ['CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT'],
          description: 'must be one of: CREATE, UPDATE, DELETE, LOGIN, LOGOUT'
        },
        entityType: {
          bsonType: 'string',
          minLength: 1,
          description: 'must be a non-empty string (e.g., STUDENT, FACULTY, ATTENDANCE)'
        },
        entityId: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        oldValue: {
          bsonType: ['object', 'null'],
          description: 'must be an object or null'
        },
        newValue: {
          bsonType: ['object', 'null'],
          description: 'must be an object or null'
        },
        ipAddress: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        userAgent: {
          bsonType: ['string', 'null'],
          description: 'must be a string or null'
        },
        timestamp: {
          bsonType: 'date',
          description: 'must be a date'
        }
      }
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});
print('✓ Audit Logs collection validation created\n');

print('\n========================================');
print('Schema validation setup completed successfully!');
print('========================================\n');
print('All collections now have strict schema validation enabled.');
print('Invalid documents will be rejected with detailed error messages.\n');
