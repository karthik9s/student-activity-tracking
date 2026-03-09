# Admin Login Credentials Fix - Bugfix Design

## Overview

This bugfix addresses the authentication failure for the admin user with credentials admin@cvr.ac.in / admin123. The bug prevents administrative access to the Student Activity Tracker application. The root cause is likely one of four issues: (1) the admin user document is missing from the users collection, (2) the password hash in the database doesn't match the BCrypt hash of "admin123", (3) the role field is incorrect or missing, or (4) the isActive field is set to false or missing. The fix will ensure the admin user exists in the database with the correct password hash, role, and active status.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when a user attempts to login with admin@cvr.ac.in / admin123 credentials
- **Property (P)**: The desired behavior when admin credentials are used - successful authentication with JWT tokens returned
- **Preservation**: Existing authentication behavior for faculty and student users that must remain unchanged by the fix
- **AuthService.login()**: The method in `backend/src/main/java/com/college/activitytracker/service/AuthService.java` that handles user authentication using Spring Security's AuthenticationManager
- **UserRepository**: The MongoDB repository that queries the users collection to find user documents by email
- **BCrypt**: The password hashing algorithm used by Spring Security's PasswordEncoder to hash and verify passwords
- **isActive**: The boolean field in the User model that determines if a user account is enabled for authentication

## Bug Details

### Fault Condition

The bug manifests when a user attempts to login with the documented admin credentials (admin@cvr.ac.in / admin123). The AuthService.login() method fails to authenticate because either: (1) the UserRepository cannot find a user document with email "admin@cvr.ac.in", (2) the password hash stored in the database doesn't match the BCrypt hash of "admin123", (3) the role field is not set to "ROLE_ADMIN", or (4) the isActive field is false or null.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type LoginRequest
  OUTPUT: boolean
  
  RETURN input.email == 'admin@cvr.ac.in'
         AND input.password == 'admin123'
         AND (NOT userExistsInDatabase(input.email)
              OR NOT passwordHashMatches(input.password, storedHash)
              OR userRole != 'ROLE_ADMIN'
              OR userIsActive != true)
END FUNCTION
```

### Examples

- **Missing admin user**: POST /api/v1/auth/login with body {email: "admin@cvr.ac.in", password: "admin123"} returns "Login failed. Please check your credentials." because no user document exists with that email
- **Wrong password hash**: POST /api/v1/auth/login with admin credentials returns authentication failure because the stored password hash doesn't match BCrypt hash of "admin123"
- **Incorrect role**: POST /api/v1/auth/login with admin credentials returns authentication failure because the user document has role "ADMIN" instead of "ROLE_ADMIN"
- **Inactive account**: POST /api/v1/auth/login with admin credentials returns authentication failure because isActive is false or null

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Faculty users with @cvr.ac.in credentials must continue to authenticate successfully with ROLE_FACULTY
- Student users with @cvr.ac.in credentials must continue to authenticate successfully with ROLE_STUDENT
- Invalid password attempts must continue to trigger brute force protection tracking
- BCrypt password verification must continue to work for all user types
- JWT token generation must continue to include correct role and permissions in claims

**Scope:**
All inputs that do NOT involve the admin@cvr.ac.in credentials should be completely unaffected by this fix. This includes:
- Faculty login flows (e.g., rajesh.kumar@cvr.ac.in / faculty123)
- Student login flows (e.g., cse21a001@cvr.ac.in / student123)
- Invalid credential handling and error responses
- Token refresh operations
- Protected endpoint authorization checks

## Hypothesized Root Cause

Based on the bug description and codebase analysis, the most likely issues are:

1. **Missing Admin User Document**: The users collection in MongoDB does not contain a document with email "admin@cvr.ac.in"
   - The seed-complete-demo-data.js script may not have been executed
   - The admin user may have been deleted accidentally
   - Database initialization may have failed

2. **Incorrect Password Hash**: The password field in the admin user document doesn't match the BCrypt hash of "admin123"
   - The password may have been hashed with a different algorithm
   - The password may have been stored as plaintext "admin123" instead of hashed
   - The BCrypt salt rounds may be different from what PasswordEncoder expects

3. **Incorrect Role Assignment**: The role field is not set to "ROLE_ADMIN"
   - The role may be set to "ADMIN" without the "ROLE_" prefix
   - The role field may be missing or null
   - Spring Security requires the "ROLE_" prefix for role-based authorization

4. **Inactive Account Status**: The isActive field is set to false or null
   - The UserRepository.findByEmailAndIsActiveTrue() query will not find the user
   - The account may have been deactivated
   - The isActive field may be missing from the document

## Correctness Properties

Property 1: Fault Condition - Admin Login Success

_For any_ login request where the email is "admin@cvr.ac.in" and the password is "admin123", the fixed authentication system SHALL successfully authenticate the user, verify the password hash matches using BCrypt, confirm the role is "ROLE_ADMIN" and isActive is true, and return a 200 OK response with valid JWT access and refresh tokens in the AuthResponse.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Non-Admin Authentication

_For any_ login request where the email is NOT "admin@cvr.ac.in" (faculty or student credentials), the fixed authentication system SHALL produce exactly the same authentication behavior as before the fix, preserving successful authentication for valid credentials, brute force protection tracking, BCrypt password verification, and JWT token generation with correct roles.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct, the fix involves ensuring the admin user exists in the database with correct credentials:

**File**: `seed-complete-demo-data.js` (or create a new admin-user-seed.js script)

**Function**: Database seeding script

**Specific Changes**:
1. **Verify Admin User Exists**: Query the users collection to check if admin@cvr.ac.in exists
   - If missing, insert a new admin user document
   - If exists, verify the password hash, role, and isActive fields

2. **Ensure Correct Password Hash**: Use BCrypt with 10 salt rounds to hash "admin123"
   - The hash should match the format: $2a$10$... (BCrypt identifier with 10 rounds)
   - Verify the PasswordEncoder can successfully verify "admin123" against the stored hash

3. **Set Correct Role**: Ensure the role field is set to "ROLE_ADMIN" with the prefix
   - Spring Security requires the "ROLE_" prefix for role-based authorization
   - The role must match exactly "ROLE_ADMIN" (case-sensitive)

4. **Set Active Status**: Ensure isActive is set to true (boolean, not string)
   - The UserRepository.findByEmailAndIsActiveTrue() query requires this field to be true
   - Missing or null values will cause authentication to fail

5. **Set Required Timestamps**: Include createdAt and updatedAt fields
   - These fields are expected by the User model
   - Set both to the current timestamp when creating the user

**Alternative Approach**: If the admin user already exists but has incorrect data, create a migration script to update the existing document rather than inserting a new one.

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on the current database state, then verify the fix works correctly and preserves existing authentication behavior for other users.

### Exploratory Fault Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis by checking the database state and authentication flow.

**Test Plan**: Write diagnostic scripts that query the users collection to check if the admin user exists, verify the password hash format, check the role field, and verify the isActive status. Then attempt authentication with admin@cvr.ac.in / admin123 to observe the failure. Run these tests on the UNFIXED database to understand the root cause.

**Test Cases**:
1. **Admin User Existence Test**: Query users collection for email "admin@cvr.ac.in" (will fail if user doesn't exist)
2. **Password Hash Verification Test**: Check if stored password hash matches BCrypt format and can be verified with "admin123" (will fail if hash is incorrect)
3. **Role Field Test**: Verify the role field is exactly "ROLE_ADMIN" (will fail if role is wrong or missing)
4. **Active Status Test**: Verify isActive is true (will fail if false or missing)
5. **Authentication Flow Test**: POST /api/v1/auth/login with admin credentials (will fail with "Login failed" message)

**Expected Counterexamples**:
- Admin user document is missing from users collection, OR
- Password hash doesn't match BCrypt hash of "admin123", OR
- Role field is "ADMIN" instead of "ROLE_ADMIN", OR
- isActive field is false or null
- Possible causes: seed script not executed, incorrect password hashing, missing ROLE_ prefix, inactive account

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds (admin login attempts), the fixed database produces the expected behavior (successful authentication).

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := AuthService.login(input)
  ASSERT result.statusCode == 200
  ASSERT result.accessToken IS NOT NULL
  ASSERT result.refreshToken IS NOT NULL
  ASSERT result.user.email == 'admin@cvr.ac.in'
  ASSERT result.user.role == 'ROLE_ADMIN'
  ASSERT result.user.isActive == true
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold (faculty and student login attempts), the fixed database produces the same authentication results as before.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT AuthService.login_original(input) == AuthService.login_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain (different user types, valid/invalid credentials)
- It catches edge cases that manual unit tests might miss (special characters in passwords, different email formats)
- It provides strong guarantees that authentication behavior is unchanged for all non-admin users

**Test Plan**: Observe authentication behavior on UNFIXED database first for faculty and student users, then write property-based tests capturing that behavior. Verify the same behavior after fixing the admin user.

**Test Cases**:
1. **Faculty Authentication Preservation**: Observe that rajesh.kumar@cvr.ac.in / faculty123 authenticates successfully on unfixed database, then verify this continues after admin fix
2. **Student Authentication Preservation**: Observe that cse21a001@cvr.ac.in / student123 authenticates successfully on unfixed database, then verify this continues after admin fix
3. **Invalid Credentials Preservation**: Observe that wrong passwords trigger brute force protection on unfixed database, then verify this continues after admin fix
4. **Token Generation Preservation**: Observe that JWT tokens contain correct roles and claims on unfixed database, then verify this continues after admin fix

### Unit Tests

- Test admin user document exists in users collection with correct email
- Test admin password hash matches BCrypt hash of "admin123"
- Test admin role is exactly "ROLE_ADMIN"
- Test admin isActive is true
- Test authentication with admin@cvr.ac.in / admin123 returns 200 OK with JWT tokens

### Property-Based Tests

- Generate random valid faculty credentials and verify authentication continues to work after admin fix
- Generate random valid student credentials and verify authentication continues to work after admin fix
- Generate random invalid passwords and verify brute force protection continues to work after admin fix
- Test that all non-admin authentication flows produce identical results before and after the fix

### Integration Tests

- Test full login flow with admin@cvr.ac.in / admin123 from frontend to backend
- Test that admin user can access admin-only endpoints after successful authentication
- Test that JWT tokens generated for admin contain ROLE_ADMIN in claims
- Test that admin user's lastLogin timestamp is updated after successful authentication
