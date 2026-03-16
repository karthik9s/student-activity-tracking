# MongoDB Schema Validation Setup

This directory contains MongoDB schema validation configuration for the Student Activity & Academic Tracking System.

## Overview

Schema validation ensures data integrity at the database level by enforcing rules on document structure, data types, and constraints. This prevents invalid data from being inserted into the database.

## Validation Methods

There are two ways to set up schema validation:

### Method 1: Automatic Setup (Recommended)

The schema validation is automatically applied when the Spring Boot application starts via the `MongoSchemaValidationConfig` class. This is the easiest method and requires no manual intervention.

**How it works:**
- When the application starts, the `CommandLineRunner` bean executes
- It checks each collection and applies/updates validation rules
- If a collection doesn't exist, it creates it with validation
- If validation setup fails, the application continues (validation is optional)

**Advantages:**
- Automatic - no manual steps required
- Idempotent - can be run multiple times safely
- Updates validation rules if they change
- No external tools needed

### Method 2: Manual Setup Using MongoDB Shell

If you prefer to set up validation manually or need to apply it to an existing database, use the provided JavaScript file.

**Steps:**

1. Ensure MongoDB is running and accessible

2. Run the validation script using mongosh:
   ```bash
   mongosh "mongodb://localhost:27017/student_activity_tracker" mongodb-schema-validation.js
   ```

   Or for MongoDB Atlas:
   ```bash
   mongosh "mongodb+srv://username:password@cluster.mongodb.net/student_activity_tracker" mongodb-schema-validation.js
   ```

3. The script will:
   - Connect to the database
   - Apply validation rules to all collections
   - Create collections if they don't exist
   - Display success messages for each collection

**Advantages:**
- Can be run independently of the application
- Useful for database migrations
- Can be version controlled and tracked

## Collections with Validation

The following collections have schema validation enabled:

1. **users** - User authentication and role information
2. **students** - Student records with personal and academic details
3. **faculty** - Faculty member records
4. **courses** - Course/program definitions
5. **subjects** - Subject/course unit definitions
6. **class_allocations** - Faculty-subject-class assignments
7. **attendance** - Student attendance records
8. **performance** - Student performance/marks records
9. **notifications** - System notifications (created on first use)
10. **audit_logs** - Audit trail for system operations (created on first use)

## Validation Rules

### Key Validation Features

- **Required Fields**: Ensures critical fields are always present
- **Data Types**: Enforces correct BSON types (string, int, double, date, bool)
- **Format Validation**: Email addresses, academic year format (YYYY-YYYY)
- **Range Validation**: Year (1-4), credits (1-10), percentage (0-100)
- **Enum Validation**: Role, status, exam type, etc.
- **Referential Integrity**: Validates foreign key references exist

### Example Validation Rules

**Users Collection:**
- Email must be valid format
- Role must be one of: ROLE_ADMIN, ROLE_FACULTY, ROLE_STUDENT
- Password must be non-empty (BCrypt hashed)
- isActive must be boolean

**Students Collection:**
- Roll number must be unique and non-empty
- Year must be between 1 and 4
- Email must be valid format
- All name fields must be non-empty

**Attendance Collection:**
- Status must be one of: PRESENT, ABSENT, LATE, EXCUSED
- Date must be a valid date
- Student, subject, and faculty IDs are required

**Performance Collection:**
- Marks obtained must be >= 0
- Total marks must be > 0
- Percentage must be between 0 and 100
- Exam type must be one of: INTERNAL, ASSIGNMENT, MID_TERM, END_TERM

## Validation Levels

The system uses **strict** validation with **error** action:

- **Validation Level: strict** - All inserts and updates are validated
- **Validation Action: error** - Invalid documents are rejected with an error

This ensures maximum data integrity but requires all documents to conform to the schema.

## Testing Validation

### Test Valid Document (Should Succeed)

```javascript
db.students.insertOne({
  userId: "507f1f77bcf86cd799439011",
  rollNumber: "BT2023001",
  firstName: "John",
  lastName: "Doe",
  email: "john.doe@example.com",
  courseId: "507f1f77bcf86cd799439012",
  year: 2,
  section: "A",
  isActive: true,
  createdAt: new Date(),
  updatedAt: new Date()
});
```

### Test Invalid Document (Should Fail)

```javascript
// Missing required field (firstName)
db.students.insertOne({
  userId: "507f1f77bcf86cd799439011",
  rollNumber: "BT2023002",
  lastName: "Smith",
  email: "smith@example.com",
  courseId: "507f1f77bcf86cd799439012",
  year: 2,
  section: "A",
  isActive: true
});
// Error: Document failed validation

// Invalid year (must be 1-4)
db.students.insertOne({
  userId: "507f1f77bcf86cd799439011",
  rollNumber: "BT2023003",
  firstName: "Jane",
  lastName: "Smith",
  email: "jane@example.com",
  courseId: "507f1f77bcf86cd799439012",
  year: 5,  // Invalid!
  section: "A",
  isActive: true
});
// Error: Document failed validation
```

## Viewing Current Validation Rules

To view the validation rules for a collection:

```javascript
db.getCollectionInfos({name: "students"})[0].options.validator
```

## Disabling Validation (Not Recommended)

If you need to temporarily disable validation for maintenance:

```javascript
db.runCommand({
  collMod: "students",
  validationLevel: "off"
});
```

To re-enable:

```javascript
db.runCommand({
  collMod: "students",
  validationLevel: "strict"
});
```

## Troubleshooting

### Validation Errors in Application

If you see validation errors when inserting/updating documents:

1. Check the error message - it will indicate which field failed validation
2. Verify the data types match the schema (e.g., year should be Integer, not String)
3. Ensure all required fields are present
4. Check enum values match exactly (case-sensitive)

### Updating Validation Rules

If you need to modify validation rules:

1. Update the `MongoSchemaValidationConfig.java` file
2. Restart the application - validation will be updated automatically
3. Or manually run the updated JavaScript file

### Migration from Existing Data

If you have existing data that doesn't conform to the new validation rules:

1. Export the data: `mongoexport --collection=students --out=students.json`
2. Clean/transform the data to match the schema
3. Drop the collection: `db.students.drop()`
4. Apply validation (automatic on app start or run script)
5. Import cleaned data: `mongoimport --collection=students --file=students.json`

## Benefits of Schema Validation

1. **Data Integrity**: Prevents invalid data at the database level
2. **Early Error Detection**: Catches data issues before they cause problems
3. **Documentation**: Schema serves as documentation for data structure
4. **Consistency**: Ensures all documents follow the same structure
5. **Security**: Prevents injection of malicious or malformed data
6. **Debugging**: Clear error messages help identify data issues quickly

## References

- [MongoDB Schema Validation Documentation](https://www.mongodb.com/docs/manual/core/schema-validation/)
- [JSON Schema Specification](https://json-schema.org/)
- Design Document: `.kiro/specs/student-activity-tracking-system/design.md`
- Requirements Document: `.kiro/specs/student-activity-tracking-system/requirements.md`
