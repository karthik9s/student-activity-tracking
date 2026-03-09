# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Fault Condition** - Authentication Endpoints Blocked Before Controller
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate authentication requests are blocked with 401 before reaching AuthController
  - **Scoped PBT Approach**: Scope the property to concrete failing cases - POST requests to /api/v1/auth/login, /api/v1/auth/logout, /api/v1/auth/register
  - Test that authentication endpoint requests (login, logout, register) reach the controller and return appropriate responses (not 401 with empty body)
  - Test implementation details from Fault Condition: isBugCondition(input) where input.requestURI matches '/api/v1/auth/**' AND responseStatus == 401 AND responseBody.isEmpty() AND NOT controllerReached
  - The test assertions should match Expected Behavior: requests reach controller, return 200 OK for valid operations or proper error messages for failures
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found:
    - POST /api/v1/auth/login with valid credentials returns 401 with empty body instead of 200 OK with tokens
    - POST /api/v1/auth/logout returns 401 instead of 200 OK despite permitAll()
    - Requests never reach AuthController (no controller logs)
    - Possible root cause: RateLimitingFilter @Component annotation causing it to execute before Spring Security
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Protected Endpoint Security Unchanged
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-buggy inputs (protected endpoints with valid JWT tokens)
  - Observe: Requests to /api/v1/admin/** with valid admin JWT token are authenticated and authorized correctly
  - Observe: Role-based access control works - ADMIN can access admin endpoints, FACULTY can access faculty endpoints, STUDENT can access student endpoints
  - Observe: Invalid or expired JWT tokens are rejected with 401/403
  - Observe: Requests without JWT tokens to protected endpoints are rejected
  - Observe: CORS configuration allows cross-origin requests from configured frontend
  - Write property-based tests capturing observed behavior patterns from Preservation Requirements:
    - For all protected endpoint requests (NOT /api/v1/auth/**) with valid JWT tokens, authentication and authorization work exactly as before
    - For all protected endpoint requests without valid tokens, access is denied as before
    - For all role-based access checks, role hierarchy (ADMIN > FACULTY > STUDENT) is enforced as before
  - Property-based testing generates many test cases for stronger guarantees across different endpoints, roles, and token states
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline security behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [x] 3. Fix for authentication endpoint blocking

  - [x] 3.1 Remove @Component annotation from RateLimitingFilter
    - Open backend/src/main/java/com/college/activitytracker/config/RateLimitingFilter.java
    - Remove the @Component annotation to prevent automatic servlet filter registration
    - This prevents the filter from executing before Spring Security's filter chain
    - _Bug_Condition: isBugCondition(input) where input.requestURI matches '/api/v1/auth/**' AND responseStatus == 401 AND NOT controllerReached_
    - _Expected_Behavior: Authentication requests reach controller and return appropriate responses (200 OK or proper error messages)_
    - _Preservation: JWT token validation, role-based access control, and protected endpoint security remain unchanged_
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x] 3.2 Add endpoint exclusions to RateLimitingFilter
    - Modify RateLimitingFilter.doFilter() to skip rate limiting for /api/v1/auth/** endpoints
    - Add check: if (requestURI.startsWith("/api/v1/auth/")) { chain.doFilter(request, response); return; }
    - This ensures authentication requests proceed without rate limiting interference
    - _Bug_Condition: isBugCondition(input) where input.requestURI matches '/api/v1/auth/**'_
    - _Expected_Behavior: Authentication endpoints are excluded from rate limiting_
    - _Preservation: Rate limiting continues to apply to all non-auth endpoints_
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3_

  - [x] 3.3 Register RateLimitingFilter in SecurityConfig
    - Open backend/src/main/java/com/college/activitytracker/config/SecurityConfig.java
    - Inject RateLimitingFilter as a bean (create @Bean method if needed)
    - Add filter to SecurityFilterChain using .addFilterAfter(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
    - This positions the filter correctly in the Spring Security chain after authentication
    - _Bug_Condition: Filter order issue causing authentication requests to be blocked_
    - _Expected_Behavior: RateLimitingFilter executes at the correct position in the filter chain_
    - _Preservation: All existing Spring Security filters continue to function correctly_
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.3_

  - [x] 3.4 Add public endpoint check to JwtAuthenticationFilter
    - Open backend/src/main/java/com/college/activitytracker/security/JwtAuthenticationFilter.java
    - Add logic at the start of doFilterInternal() to skip JWT validation for /api/v1/auth/** endpoints
    - Add check: if (requestURI.startsWith("/api/v1/auth/")) { filterChain.doFilter(request, response); return; }
    - This ensures the JWT filter doesn't interfere with public authentication routes
    - _Bug_Condition: JWT filter may be processing authentication endpoints unnecessarily_
    - _Expected_Behavior: JWT filter skips authentication endpoints, allowing them to reach controller_
    - _Preservation: JWT validation continues for all protected endpoints_
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1_

  - [x] 3.5 Improve error logging in filters
    - In JwtAuthenticationFilter catch block, add detailed logging: logger.error("JWT authentication failed for {}: {}", requestURI, e.getMessage(), e)
    - In RateLimitingFilter, add logging for rate limit violations and filter execution
    - This enables debugging of authentication failures
    - _Expected_Behavior: Authentication errors are logged for debugging purposes_
    - _Requirements: 2.5_

  - [x] 3.6 Add exception handling to SecurityConfig
    - In SecurityConfig.securityFilterChain(), configure exception handling
    - Add .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> { response.setStatus(401); response.getWriter().write("{\"error\": \"" + authException.getMessage() + "\"}"); }))
    - This ensures 401 responses include error messages instead of empty bodies
    - _Expected_Behavior: Authentication failures return proper error messages in response body_
    - _Preservation: Existing exception handling behavior for other errors remains unchanged_
    - _Requirements: 2.4_

  - [x] 3.7 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Authentication Endpoints Accessible
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - Verify authentication requests reach AuthController and return appropriate responses:
      - POST /api/v1/auth/login with valid credentials returns 200 OK with JWT tokens
      - POST /api/v1/auth/logout returns 200 OK
      - POST /api/v1/auth/register reaches controller (may return 403 if not admin, but not 401 before controller)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [x] 3.8 Verify preservation tests still pass
    - **Property 2: Preservation** - Protected Endpoint Security Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all security behaviors are preserved:
      - JWT token validation works for protected endpoints
      - Role-based access control enforces ADMIN/FACULTY/STUDENT restrictions
      - Invalid tokens are rejected
      - CORS configuration continues to work
      - Rate limiting applies to non-auth endpoints
    - Confirm all tests still pass after fix (no regressions)

- [x] 4. Checkpoint - Ensure all tests pass
  - Run all unit tests, integration tests, and property-based tests
  - Verify authentication endpoints are accessible and return proper responses
  - Verify protected endpoints still require authentication and enforce role-based access
  - Verify no regressions in existing security behavior
  - Test with actual login flow: POST /api/v1/auth/login with admin@cvr.ac.in / admin123 should return JWT tokens
  - Ensure all tests pass, ask the user if questions arise
