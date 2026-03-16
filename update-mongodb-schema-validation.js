// MongoDB Schema Update Script - Fix Performance examType validation
// Run this script using: mongosh <connection-string> update-mongodb-schema-validation.js

// Switch to the database
db = db.getSiblingDB('student_activity_tracker');

print('Updating Performance collection schema validation...\n');

// Update Performance Collection Schema Validation
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
        facultyId: {
          bsonType: ['string', 'null'],
          description: 'must be a string reference to faculty collection or null'
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

print('✓ Performance collection validation updated successfully!\n');
print('examType now accepts: INTERNAL, ASSIGNMENT, EXAM, FINAL\n');
