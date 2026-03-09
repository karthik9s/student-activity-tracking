# Login Authentication Fix Bugfix Design

## Overview

This design addresses a critical authentication bug where all login attempts return 401 Unauthorized, preventing users from accessing the application. The bug affects all authentication endpoints including public routes configured with permitAll(). Investigation confirms that valid users exist in the database with correct password hashes, but requests are being rejected before reaching the AuthController. The fix will focus on identifying and correcting the Spring Security filter chain configuration issue that is blocking legitimate authentication requests.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when authentication requests to public endpoints return 401 Unauthorized before reaching the controller
- **Property (P)**: The desired behavior when authentication requests are made - public auth endpoints should be accessible without authentication and return appropriate responses
- **Preservation**: Existing JWT token validation, password verification, role-based access control, and protected endpoint security that must remain unchanged
- **SecurityFilterChain**: The Spring Security filter chain in `SecurityConfig.java` that processes HTTP requests and applies security rules
- **JwtAuthenticationFilter**: The filter in `backend/src/main/java/com/college/activitytracker/security/JwtAuthenticationFilter.java` that validates JWT tokens for authenticated requests
- **RateLimitingFilter**: The servlet filter in `backend/src/main/java/com/college/activitytracker/config/RateLimitingFilter.java` that limits request rates per IP address
- **permitAll()**: Spring Security configuration that allows unauthenticated access to specific endpoints
- **AuthController**: The controller in `backend/src/main/java/com/college/activitytracker/controller/AuthController.java` that handles authentication endpoints

## Bug Details

### Fault Condition

The bug manifests when a user attempts to access any authentication endpoint (login, register, logout) or when any request is made to endpoints configured with permitAll(). The Spring Security filter chain or a servlet filter is rejecting these requests with 401 Unauthorized before they reach the AuthController, despite the SecurityConfig explicitly permitting these endpoints.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type HttpServletRequest
  OUTPUT: boolean
  
  RETURN input.requestURI MATCHES '/api/v1/auth/**'
         AND input.method IN ['POST', 'GET']
         AND responseStatus == 401
         AND responseBody.isEmpty()
         AND NOT controllerReached
END FUNCTION
```

### Examples

- **Login with valid credentials**: POST /api/v1/auth/login with body {email: "admin@cvr.ac.in", password: "admin123"} returns 401 Unauthorized with empty response body instead of 200 OK with JWT tokens
- **Login with any valid user**: POST /api/v1/auth/login with any valid credentials from the 29 users in database returns 401 Unauthorized instead of successful authentication
- **Public logout endpoint**: POST /api/v1/auth/logout returns 401 Unauthorized instead of 200 OK, despite being configured as permitAll()
- **Register endpoint**: POST /api/v1/auth/register returns 401 Unauthorized before checking admin role authorization

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- JWT token validation for authenticated requests must continue to work exactly as before
- BCrypt password verification must continue to compare provided passwords with stored hashes
- Protected endpoints (non-auth) must continue to require authentication
- Role-based access control must continue to restrict endpoints based on user roles (ADMIN, FACULTY, STUDENT)
- CORS configuration must continue to allow cross-origin requests from configured frontend
- Database integrity with 29 valid users and correct password hashes must be maintained
- JwtAuthenticationFilter must continue to extract and validate JWT tokens from Authorization headers
- Role hierarchy (ADMIN > FACULTY > STUDENT) must continue to function correctly

**Scope:**
All requests that do NOT target /api/v1/auth/** endpoints should be completely unaffected by this fix. This includes:
- Requests to protected endpoints with valid JWT tokens
- Role-based authorization checks
- Token refresh mechanisms
- All business logic in controllers and services

## Hypothesized Root Cause

Based on the bug description and code analysis, the most likely issues are:

1. **Servlet Filter Registration Order**: The RateLimitingFilter is annotated with @Component, which automatically registers it as a servlet filter. This causes it to execute BEFORE Spring Security's filter chain, potentially interfering with authentication requests or causing unexpected behavior when combined with Spring Security filters.

2. **Missing Filter Exclusions**: The RateLimitingFilter applies to ALL requests without excluding authentication endpoints. If there's an error in the filter logic or if it's blocking requests unexpectedly, it would prevent authentication requests from reaching Spring Security.

3. **Spring Security Filter Chain Misconfiguration**: Although the SecurityConfig appears correct with permitAll() for /api/v1/auth/**, there may be an issue with how the filter chain is built or how the JwtAuthenticationFilter interacts with public endpoints.

4. **Exception Handling in Filters**: If an exception occurs in the RateLimitingFilter or JwtAuthenticationFilter and is not properly handled, it could result in a 401 response without reaching the controller or logging errors.

5. **Authentication Provider Configuration**: The DaoAuthenticationProvider may not be properly configured or may be rejecting requests before they reach the authentication logic.

## Correctness Properties

Property 1: Fault Condition - Authentication Endpoints Accessible

_For any_ HTTP request where the request URI matches /api/v1/auth/** (login, register, logout, refresh), the fixed security configuration SHALL allow the request to pass through the filter chain and reach the AuthController without requiring authentication, returning appropriate responses (200 OK for successful operations, 401 for invalid credentials with error messages).

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**

Property 2: Preservation - Protected Endpoint Security

_For any_ HTTP request where the request URI does NOT match /api/v1/auth/** (protected endpoints like /api/v1/admin/**, /api/v1/faculty/**, /api/v1/student/**), the fixed security configuration SHALL produce exactly the same authentication and authorization behavior as the original configuration, requiring valid JWT tokens and enforcing role-based access control.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `backend/src/main/java/com/college/activitytracker/config/RateLimitingFilter.java`

**Specific Changes**:
1. **Remove @Component Annotation**: Remove the @Component annotation from RateLimitingFilter to prevent automatic servlet filter registration. This ensures the filter doesn't execute before Spring Security's filter chain.

2. **Register Filter in SecurityConfig**: Manually register the RateLimitingFilter in the Spring Security filter chain at the appropriate position (after authentication but before authorization) so it only applies to authenticated requests or can be configured to exclude specific endpoints.

3. **Add Endpoint Exclusions**: Modify the RateLimitingFilter to skip rate limiting for /api/v1/auth/** endpoints, allowing authentication requests to proceed without rate limiting interference.

**File**: `backend/src/main/java/com/college/activitytracker/config/SecurityConfig.java`

**Specific Changes**:
4. **Add Exception Handling**: Ensure the SecurityFilterChain has proper exception handling configuration to return meaningful error messages instead of empty 401 responses.

5. **Verify Filter Order**: Confirm that JwtAuthenticationFilter is correctly positioned and doesn't interfere with public endpoints by ensuring it only processes requests with JWT tokens.

**File**: `backend/src/main/java/com/college/activitytracker/security/JwtAuthenticationFilter.java`

**Specific Changes**:
6. **Add Public Endpoint Check**: Add logic to skip JWT validation for /api/v1/auth/** endpoints to ensure the filter doesn't interfere with public authentication routes.

7. **Improve Error Logging**: Enhance error logging in the catch block to capture and log authentication failures for debugging purposes.

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code to confirm the root cause, then verify the fix works correctly and preserves existing security behavior.

### Exploratory Fault Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis regarding filter registration and ordering. If we refute, we will need to re-hypothesize.

**Test Plan**: Write integration tests that simulate HTTP requests to authentication endpoints and verify they reach the controller. Run these tests on the UNFIXED code to observe 401 failures and confirm the root cause. Use logging and debugging to trace request flow through filters.

**Test Cases**:
1. **Login Request Test**: Send POST /api/v1/auth/login with valid credentials (will fail with 401 on unfixed code)
2. **Logout Request Test**: Send POST /api/v1/auth/logout (will fail with 401 on unfixed code despite permitAll)
3. **Register Request Test**: Send POST /api/v1/auth/register (will fail with 401 on unfixed code before role check)
4. **Filter Order Test**: Verify RateLimitingFilter executes before Spring Security (will confirm root cause on unfixed code)

**Expected Counterexamples**:
- Authentication requests return 401 Unauthorized with empty response body
- Requests never reach AuthController (no controller logs)
- Possible causes: RateLimitingFilter blocking requests, filter order issues, missing permitAll configuration

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds (authentication endpoint requests), the fixed configuration produces the expected behavior (requests reach controller and return appropriate responses).

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := processRequest_fixed(input)
  ASSERT expectedBehavior(result)
  // expectedBehavior: request reaches controller, returns 200 OK or proper error with message
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold (protected endpoint requests), the fixed configuration produces the same result as the original configuration.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT processRequest_original(input) = processRequest_fixed(input)
  // Verify JWT validation, role-based access, and authorization remain unchanged
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain (different endpoints, roles, tokens)
- It catches edge cases that manual unit tests might miss (expired tokens, invalid roles, missing headers)
- It provides strong guarantees that security behavior is unchanged for all non-auth endpoints

**Test Plan**: Observe behavior on UNFIXED code first for protected endpoints with valid JWT tokens, then write property-based tests capturing that exact security behavior.

**Test Cases**:
1. **JWT Token Validation Preservation**: Observe that requests to /api/v1/admin/** with valid admin JWT work on unfixed code, then verify this continues after fix
2. **Role-Based Access Preservation**: Observe that role restrictions work correctly on unfixed code, then verify ADMIN/FACULTY/STUDENT access rules remain unchanged
3. **Invalid Token Rejection Preservation**: Observe that invalid/expired tokens are rejected on unfixed code, then verify this security behavior continues
4. **CORS Configuration Preservation**: Observe that CORS headers are applied correctly on unfixed code, then verify cross-origin requests continue working

### Unit Tests

- Test authentication endpoint accessibility without JWT tokens
- Test that login with valid credentials returns JWT tokens
- Test that login with invalid credentials returns 401 with error message
- Test that logout endpoint is accessible without authentication
- Test filter order and execution sequence
- Test RateLimitingFilter exclusions for auth endpoints

### Property-Based Tests

- Generate random valid credentials and verify successful authentication
- Generate random protected endpoint requests with valid tokens and verify access is granted
- Generate random protected endpoint requests without tokens and verify access is denied
- Generate random role combinations and verify role-based access control works correctly
- Test that rate limiting applies to non-auth endpoints but not auth endpoints

### Integration Tests

- Test full login flow from HTTP request to JWT token response
- Test that authenticated requests to protected endpoints work after login
- Test that unauthenticated requests to protected endpoints are rejected
- Test role hierarchy enforcement across different user roles
- Test CORS preflight requests to authentication endpoints
- Test error responses include proper error messages and status codes
