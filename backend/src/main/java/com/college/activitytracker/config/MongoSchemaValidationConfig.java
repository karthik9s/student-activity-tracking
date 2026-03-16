package com.college.activitytracker.config;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class to set up MongoDB schema validation for all collections.
 * This ensures data integrity at the database level.
 */
@Configuration
public class MongoSchemaValidationConfig {

    /**
     * CommandLineRunner that sets up schema validation when the application starts.
     * This is idempotent - it can be run multiple times safely.
     */
    @Bean
    public CommandLineRunner setupSchemaValidation(MongoTemplate mongoTemplate) {
        return args -> {
            System.out.println("Setting up MongoDB schema validation...");
            
            MongoDatabase database = mongoTemplate.getDb();
            
            try {
                // Set up validation for each collection
                setupUsersValidation(database);
                setupStudentsValidation(database);
                setupFacultyValidation(database);
                setupCoursesValidation(database);
                setupSubjectsValidation(database);
                setupClassAllocationsValidation(database);
                setupAttendanceValidation(database);
                setupPerformanceValidation(database);
                
                System.out.println("MongoDB schema validation setup completed successfully!");
            } catch (Exception e) {
                System.err.println("Error setting up schema validation: " + e.getMessage());
                // Don't fail application startup if validation setup fails
                // Collections will still work, just without validation
            }
        };
    }

    private void setupUsersValidation(MongoDatabase database) {
        Document validator = new Document("$jsonSchema", new Document()
            .append("bsonType", "object")
            .append("required", Arrays.asList("email", "password", "role", "isActive"))
            .append("properties", new Document()
                .append("email", new Document()
                    .append("bsonType", "string")
                    .append("pattern", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                    .append("description", "must be a valid email address"))
                .append("password", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("role", new Document()
                    .append("enum", Arrays.asList("ROLE_ADMIN", "ROLE_FACULTY", "ROLE_STUDENT"))
                    .append("description", "must be one of: ROLE_ADMIN, ROLE_FACULTY, ROLE_STUDENT"))
                .append("isActive", new Document()
                    .append("bsonType", "bool")
                    .append("description", "must be a boolean"))
            )
        );
        
        applyValidation(database, "users", validator);
    }

    private void setupStudentsValidation(MongoDatabase database) {
        Document validator = new Document("$jsonSchema", new Document()
            .append("bsonType", "object")
            .append("required", Arrays.asList("userId", "rollNumber", "firstName", "lastName", 
                "email", "courseId", "year", "section", "isActive"))
            .append("properties", new Document()
                .append("userId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to users collection"))
                .append("rollNumber", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a unique non-empty string"))
                .append("firstName", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("lastName", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("email", new Document()
                    .append("bsonType", "string")
                    .append("pattern", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                    .append("description", "must be a valid email address"))
                .append("courseId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to courses collection"))
                .append("year", new Document()
                    .append("bsonType", "int")
                    .append("minimum", 1)
                    .append("maximum", 4)
                    .append("description", "must be an integer between 1 and 4"))
                .append("section", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("isActive", new Document()
                    .append("bsonType", "bool")
                    .append("description", "must be a boolean"))
            )
        );
        
        applyValidation(database, "students", validator);
    }

    private void setupFacultyValidation(MongoDatabase database) {
        Document validator = new Document("$jsonSchema", new Document()
            .append("bsonType", "object")
            .append("required", Arrays.asList("userId", "employeeId", "firstName", "lastName", 
                "email", "department", "isActive"))
            .append("properties", new Document()
                .append("userId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to users collection"))
                .append("employeeId", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a unique non-empty string"))
                .append("firstName", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("lastName", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("email", new Document()
                    .append("bsonType", "string")
                    .append("pattern", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                    .append("description", "must be a valid email address"))
                .append("department", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("isActive", new Document()
                    .append("bsonType", "bool")
                    .append("description", "must be a boolean"))
            )
        );
        
        applyValidation(database, "faculty", validator);
    }

    private void setupCoursesValidation(MongoDatabase database) {
        Document validator = new Document("$jsonSchema", new Document()
            .append("bsonType", "object")
            .append("required", Arrays.asList("code", "name", "duration"))
            .append("properties", new Document()
                .append("code", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a unique non-empty string"))
                .append("name", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("duration", new Document()
                    .append("bsonType", "int")
                    .append("minimum", 1)
                    .append("description", "must be a positive integer (years)"))
            )
        );
        
        applyValidation(database, "courses", validator);
    }

    private void setupSubjectsValidation(MongoDatabase database) {
        Document validator = new Document("$jsonSchema", new Document()
            .append("bsonType", "object")
            .append("required", Arrays.asList("code", "name", "credits"))
            .append("properties", new Document()
                .append("code", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a unique non-empty string"))
                .append("name", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("credits", new Document()
                    .append("bsonType", "int")
                    .append("minimum", 1)
                    .append("maximum", 10)
                    .append("description", "must be an integer between 1 and 10"))
                .append("type", new Document()
                    .append("enum", Arrays.asList("THEORY", "PRACTICAL", "LAB", null))
                    .append("description", "must be one of: THEORY, PRACTICAL, LAB, or null"))
            )
        );
        
        applyValidation(database, "subjects", validator);
    }

    private void setupClassAllocationsValidation(MongoDatabase database) {
        Document validator = new Document("$jsonSchema", new Document()
            .append("bsonType", "object")
            .append("required", Arrays.asList("facultyId", "subjectId", "courseId", 
                "year", "section", "academicYear", "isActive"))
            .append("properties", new Document()
                .append("facultyId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to faculty collection"))
                .append("subjectId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to subjects collection"))
                .append("courseId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to courses collection"))
                .append("year", new Document()
                    .append("bsonType", "int")
                    .append("minimum", 1)
                    .append("maximum", 4)
                    .append("description", "must be an integer between 1 and 4"))
                .append("section", new Document()
                    .append("bsonType", "string")
                    .append("minLength", 1)
                    .append("description", "must be a non-empty string"))
                .append("academicYear", new Document()
                    .append("bsonType", "string")
                    .append("pattern", "^\\d{4}-\\d{4}$")
                    .append("description", "must be in format YYYY-YYYY (e.g., 2023-2024)"))
                .append("isActive", new Document()
                    .append("bsonType", "bool")
                    .append("description", "must be a boolean"))
            )
        );
        
        applyValidation(database, "class_allocations", validator);
    }

    private void setupAttendanceValidation(MongoDatabase database) {
        Document validator = new Document("$jsonSchema", new Document()
            .append("bsonType", "object")
            .append("required", Arrays.asList("studentId", "subjectId", "facultyId", "date", "status"))
            .append("properties", new Document()
                .append("studentId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to students collection"))
                .append("subjectId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to subjects collection"))
                .append("facultyId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to faculty collection"))
                .append("date", new Document()
                    .append("bsonType", "date")
                    .append("description", "must be a date"))
                .append("status", new Document()
                    .append("enum", Arrays.asList("PRESENT", "ABSENT", "LATE", "EXCUSED"))
                    .append("description", "must be one of: PRESENT, ABSENT, LATE, EXCUSED"))
            )
        );
        
        applyValidation(database, "attendance", validator);
    }

    private void setupPerformanceValidation(MongoDatabase database) {
        Document validator = new Document("$jsonSchema", new Document()
            .append("bsonType", "object")
            .append("required", Arrays.asList("studentId", "subjectId", "examType", 
                "marksObtained", "totalMarks"))
            .append("properties", new Document()
                .append("studentId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to students collection"))
                .append("subjectId", new Document()
                    .append("bsonType", "string")
                    .append("description", "must be a string reference to subjects collection"))
                .append("examType", new Document()
                    .append("enum", Arrays.asList("INTERNAL", "ASSIGNMENT", "EXAM", "FINAL"))
                    .append("description", "must be one of: INTERNAL, ASSIGNMENT, EXAM, FINAL"))
                .append("marksObtained", new Document()
                    .append("bsonType", "double")
                    .append("minimum", 0)
                    .append("description", "must be a non-negative number"))
                .append("totalMarks", new Document()
                    .append("bsonType", "double")
                    .append("minimum", 0)
                    .append("description", "must be a positive number"))
                .append("percentage", new Document()
                    .append("bsonType", Arrays.asList("double", "null"))
                    .append("minimum", 0)
                    .append("maximum", 100)
                    .append("description", "must be a number between 0 and 100 or null"))
            )
        );
        
        applyValidation(database, "performance", validator);
    }

    /**
     * Apply validation to a collection. If the collection doesn't exist, it will be created.
     * If validation already exists, it will be updated.
     */
    private void applyValidation(MongoDatabase database, String collectionName, Document validator) {
        try {
            // Check if collection exists
            boolean collectionExists = database.listCollectionNames()
                .into(new java.util.ArrayList<>())
                .contains(collectionName);
            
            if (collectionExists) {
                // Update existing collection with validation
                database.runCommand(new Document()
                    .append("collMod", collectionName)
                    .append("validator", validator)
                    .append("validationLevel", "strict")
                    .append("validationAction", "error")
                );
                System.out.println(String.format("Updated schema validation for collection: {}", collectionName));
            } else {
                // Create new collection with validation
                database.createCollection(collectionName, 
                    new com.mongodb.client.model.CreateCollectionOptions()
                        .validationOptions(new com.mongodb.client.model.ValidationOptions()
                            .validator(validator)
                            .validationLevel(com.mongodb.client.model.ValidationLevel.STRICT)
                            .validationAction(com.mongodb.client.model.ValidationAction.ERROR)
                        )
                );
                System.out.println(String.format("Created collection with schema validation: %s", collectionName));
            }
        } catch (Exception e) {
            System.err.println(String.format("Could not set up validation for collection %s: %s", collectionName, e.getMessage()));
        }
    }
}


