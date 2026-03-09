# Bugfix Requirements Document

## Introduction

This document addresses a critical bug in the faculty dashboard where allocated classes are not displayed to faculty members despite being correctly stored in the database. The issue stems from an ID mismatch: the admin panel stores class allocations using the Faculty ID, but the faculty dashboard API queries allocations using the User ID from authentication. Since these are different identifiers, faculty members cannot see their assigned classes in the Class Allocation or Attendance sections.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a faculty member logs into their account THEN the system uses the User ID (from authentication) to query class allocations

1.2 WHEN the system queries class allocations using the User ID THEN no results are returned because allocations are stored with Faculty ID

1.3 WHEN no allocations are found THEN the faculty dashboard displays empty Class Allocation and Attendance sections

1.4 WHEN the admin panel creates a class allocation THEN the system stores the Faculty ID in the allocation record

### Expected Behavior (Correct)

2.1 WHEN a faculty member logs into their account THEN the system SHALL resolve the User ID to the corresponding Faculty ID before querying allocations

2.2 WHEN the system queries class allocations using the correct Faculty ID THEN all allocations assigned to that faculty member SHALL be returned

2.3 WHEN allocations are found THEN the faculty dashboard SHALL display all allocated classes in the Class Allocation and Attendance sections

2.4 WHEN the admin panel creates a class allocation THEN the system SHALL continue to store the Faculty ID in the allocation record (no change needed)

### Unchanged Behavior (Regression Prevention)

3.1 WHEN an admin views class allocations in the admin panel THEN the system SHALL CONTINUE TO display all allocations correctly

3.2 WHEN an admin creates a new class allocation THEN the system SHALL CONTINUE TO save the allocation with the Faculty ID

3.3 WHEN an admin edits or deletes a class allocation THEN the system SHALL CONTINUE TO perform these operations successfully

3.4 WHEN a faculty member with no allocations logs in THEN the system SHALL CONTINUE TO display an empty state (not an error)

3.5 WHEN the authentication system validates user credentials THEN the system SHALL CONTINUE TO use the User ID for authentication purposes

3.6 WHEN other faculty members with correctly configured data log in THEN the system SHALL CONTINUE TO display their allocations correctly
