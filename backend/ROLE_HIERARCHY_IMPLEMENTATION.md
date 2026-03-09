# Role Hierarchy Implementation

## Overview

This document describes the implementation of role hierarchy enforcement for the Student Activity & Academic Tracking System, fulfilling **Requirement 2.5**: System must enforce role hierarchy (ADMIN > FACULTY > STUDENT) where higher roles can access lower role endpoints.

## Implementation Details

### Role Hierarchy Configuration

The role hierarchy has been implemented in `SecurityConfig.java` using Spring Security's `RoleHierarchy` feature.

#### Hierarchy Structure

```
ROLE_ADMIN > ROLE_FACULTY > ROLE_STUDENT
```

This means:
- **ADMIN** users can access all endpoints (admin, faculty, and student)
- **FACULTY** users can access faculty and student endpoints
- **STUDENT** users can only access student endpoints

### Code Changes

#### 1. SecurityConfig.java

Added two new beans to configure role hierarchy:

```java
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    String hierarchy = "ROLE_ADMIN > ROLE_FACULTY \n ROLE_FACULTY > ROLE_STUDENT";
    roleHierarchy.setHierarchy(hierarchy);
    return roleHierarchy;
}

@Bean
public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
    DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
    expressionHandler.setRoleHierarchy(roleHierarchy());
    return expressionHandler;
}
```

**Key Points:**
- `RoleHierarchy` bean defines the hierarchy relationships
- `DefaultWebSecurityExpressionHandler` bean applies the hierarchy to all security expressions
- The hierarchy is transitive: ADMIN > FACULTY > STUDENT means ADMIN inherits both FACULTY and STUDENT permissions

### How It Works

1. **Without Role Hierarchy** (previous behavior):
   - ADMIN user accessing `/api/v1/admin/**` → ✅ Allowed
   - ADMIN user accessing `/api/v1/faculty/**` → ❌ Denied (403 Forbidden)
   - ADMIN user accessing `/api/v1/student/**` → ❌ Denied (403 Forbidden)

2. **With Role Hierarchy** (new behavior):
   - ADMIN user accessing `/api/v1/admin/**` → ✅ Allowed
   - ADMIN user accessing `/api/v1/faculty/**` → ✅ Allowed (inherited)
   - ADMIN user accessing `/api/v1/student/**` → ✅ Allowed (inherited)
   
   - FACULTY user accessing `/api/v1/admin/**` → ❌ Denied (403 Forbidden)
   - FACULTY user accessing `/api/v1/faculty/**` → ✅ Allowed
   - FACULTY user accessing `/api/v1/student/**` → ✅ Allowed (inherited)
   
   - STUDENT user accessing `/api/v1/admin/**` → ❌ Denied (403 Forbidden)
   - STUDENT user accessing `/api/v1/faculty/**` → ❌ Denied (403 Forbidden)
   - STUDENT user accessing `/api/v1/student/**` → ✅ Allowed

### Testing

Two test classes have been created to verify the role hierarchy implementation:

#### 1. RoleHierarchyUnitTest.java

A unit test that verifies the role hierarchy configuration without requiring full Spring context:

- `testAdminInheritsAllRoles()`: Verifies ADMIN has access to all three roles
- `testFacultyInheritsStudentRole()`: Verifies FACULTY has access to FACULTY and STUDENT roles
- `testStudentHasNoInheritedRoles()`: Verifies STUDENT only has STUDENT role
- `testRoleHierarchyTransitivity()`: Verifies transitive inheritance works correctly

#### 2. RoleHierarchyTest.java

An integration test that verifies the role hierarchy with full Spring context:

- Tests the same scenarios as the unit test but with the actual Spring Security configuration
- Requires the application to compile successfully

### Benefits

1. **Simplified Access Control**: Higher-level users automatically inherit permissions of lower-level roles
2. **Flexibility**: Admins can perform all operations without needing multiple role assignments
3. **Maintainability**: Role relationships are defined in one place
4. **Security**: Lower-level roles cannot access higher-level endpoints

### Usage Examples

#### Example 1: Admin Accessing Faculty Endpoints

```java
// Admin user with JWT token containing ROLE_ADMIN
GET /api/v1/faculty/attendance
Authorization: Bearer <admin-jwt-token>

// Response: 200 OK (allowed due to role hierarchy)
```

#### Example 2: Faculty Accessing Student Endpoints

```java
// Faculty user with JWT token containing ROLE_FACULTY
GET /api/v1/student/attendance
Authorization: Bearer <faculty-jwt-token>

// Response: 200 OK (allowed due to role hierarchy)
```

#### Example 3: Student Accessing Faculty Endpoints

```java
// Student user with JWT token containing ROLE_STUDENT
GET /api/v1/faculty/attendance
Authorization: Bearer <student-jwt-token>

// Response: 403 Forbidden (not allowed - no hierarchy inheritance)
```

### Integration with Existing Security

The role hierarchy integrates seamlessly with existing security configurations:

- JWT authentication still works as before
- Method-level security annotations (`@PreAuthorize`, `@Secured`) respect the hierarchy
- URL-based security rules in `SecurityFilterChain` respect the hierarchy
- No changes needed to controllers or services

### Verification Steps

To verify the implementation works correctly:

1. **Compile the project**: Ensure no compilation errors in SecurityConfig.java
2. **Run unit tests**: Execute `RoleHierarchyUnitTest` to verify hierarchy logic
3. **Run integration tests**: Execute `RoleHierarchyTest` with full Spring context
4. **Manual testing**: 
   - Login as ADMIN and access faculty/student endpoints
   - Login as FACULTY and access student endpoints
   - Login as STUDENT and verify cannot access admin/faculty endpoints

### Requirements Fulfilled

✅ **Requirement 2.5**: System must enforce role hierarchy (ADMIN > FACULTY > STUDENT) where higher roles can access lower role endpoints

### Related Files

- `backend/src/main/java/com/college/activitytracker/config/SecurityConfig.java` - Main configuration
- `backend/src/test/java/com/college/activitytracker/config/RoleHierarchyUnitTest.java` - Unit tests
- `backend/src/test/java/com/college/activitytracker/config/RoleHierarchyTest.java` - Integration tests

### Notes

- The implementation follows Spring Security best practices
- Role hierarchy is applied globally to all security expressions
- The hierarchy is transitive (ADMIN > FACULTY > STUDENT means ADMIN gets all permissions)
- No changes to existing controllers or services are required
- The implementation is backward compatible with existing security configurations

## Conclusion

The role hierarchy has been successfully implemented in the SecurityConfig class. The implementation allows higher-level roles to automatically inherit permissions of lower-level roles, fulfilling Requirement 2.5 of the system specification.
