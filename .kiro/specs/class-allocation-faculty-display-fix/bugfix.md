# Bugfix Requirements Document

## Introduction

This document addresses a bug in the class allocation management system where faculty names are not displayed in the admin class allocation table. The issue occurs when the `mapEntityToDto` method in `ClassAllocationService` uses `ifPresent` to populate faculty names, which silently fails when faculty lookup doesn't work, leaving the `facultyName` field as null. This results in empty cells in the faculty column of the table, even though the allocation records exist in the database with valid faculty IDs.

The bug impacts the admin user experience by making it difficult to identify which faculty member is assigned to each class allocation. Manual assignment through the UI works correctly because it performs proper validation, but existing allocations may have orphaned faculty references that are not handled gracefully.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a class allocation has a facultyId that does not match any faculty record in the database THEN the system silently fails to populate facultyName and displays an empty cell in the table

1.2 WHEN the mapEntityToDto method calls facultyRepository.findById with a non-existent facultyId THEN the ifPresent block is skipped and facultyName remains null without any error indication

1.3 WHEN the frontend renders allocations with null facultyName THEN the table displays an empty faculty column cell with no placeholder or error message

### Expected Behavior (Correct)

2.1 WHEN a class allocation has a facultyId that does not match any faculty record in the database THEN the system SHALL populate facultyName with a clear placeholder value such as "Faculty Not Found" or "Unknown Faculty"

2.2 WHEN the mapEntityToDto method calls facultyRepository.findById with a non-existent facultyId THEN the system SHALL detect the missing faculty and set an appropriate error indicator in the DTO

2.3 WHEN the frontend renders allocations with missing faculty information THEN the table SHALL display a clear placeholder or error message in the faculty column to indicate the data issue

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a class allocation has a valid facultyId that matches an existing faculty record THEN the system SHALL CONTINUE TO populate facultyName with the faculty's full name (firstName + " " + lastName)

3.2 WHEN the mapEntityToDto method successfully finds faculty, subject, and course records THEN the system SHALL CONTINUE TO populate all related name fields correctly

3.3 WHEN manual assignment creates a new allocation through the UI THEN the system SHALL CONTINUE TO validate faculty existence and display the faculty name correctly

3.4 WHEN the getAllAllocations endpoint returns paginated results THEN the system SHALL CONTINUE TO return the same data structure and pagination format
