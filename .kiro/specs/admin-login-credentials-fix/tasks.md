# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Fault Condition** - Admin Login Authentication Failure
  - **CRITICAL**: This test MUST FAIL on unfixed database - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the database when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists in the current database state
  - **Scoped PBT Approach**: Scope the property to the concrete failing case: admin@cvr.ac.in / admin123 credentials
  - Test that login with admin@cvr.ac.in / admin123 succeeds (from Fault Condition in design)
  - The test assertions should verify: (1) 200 OK response, (2) valid JWT access and refresh tokens returned, (3) user email is admin@cvr.ac.in, (4) user role is ROLE_ADMIN, (5) user isActive is true
  - Run diagnostic checks on UNFIXED database: query users collection for admin@cvr.ac.in, verify password hash format, check role field, verify isActive status
  - Run authentication test on UNFIXED database
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found: admin user missing, OR password hash incorrect, OR role wrong, OR isActive false/null
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3_

- [ ] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Non-Admin Authentication Behavior
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED database for non-admin users (faculty and student credentials)
  - Observe: rajesh.kumar@cvr.ac.in / faculty123 authenticates successfully with ROLE_FACULTY on unfixed database
  - Observe: cse21a001@cvr.ac.in / student123 authenticates successfully with ROLE_STUDENT on unfixed database
  - Observe: invalid password attempts trigger brute force protection on unfixed database
  - Observe: JWT tokens contain correct roles and claims on unfixed database
  - Write property-based tests capturing observed behavior patterns from Preservation Requirements
  - Property-based testing generates many test cases for stronger guarantees
  - Test that for all non-admin credentials (faculty and student), authentication behavior is identical before and after fix
  - Run tests on UNFIXED database
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed database
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 3. Fix for admin login credentials

  - [ ] 3.1 Implement the database fix
    - Query users collection to check if admin@cvr.ac.in exists
    - If missing, insert new admin user document with: email="admin@cvr.ac.in", password=BCrypt hash of "admin123" with 10 salt rounds, role="ROLE_ADMIN", isActive=true, createdAt and updatedAt timestamps
    - If exists but incorrect, update the document to set: password=BCrypt hash of "admin123", role="ROLE_ADMIN", isActive=true
    - Verify the password hash matches BCrypt format: $2a$10$...
    - Verify the role field is exactly "ROLE_ADMIN" (with ROLE_ prefix)
    - Verify isActive is boolean true (not string or null)
    - Create or update seed script (seed-complete-demo-data.js or new admin-user-seed.js)
    - _Bug_Condition: isBugCondition(input) where input.email == 'admin@cvr.ac.in' AND input.password == 'admin123' AND (user missing OR password hash wrong OR role wrong OR isActive false)_
    - _Expected_Behavior: AuthService.login() returns 200 OK with valid JWT tokens, user email is admin@cvr.ac.in, role is ROLE_ADMIN, isActive is true_
    - _Preservation: Faculty and student authentication flows, brute force protection, BCrypt password verification, JWT token generation with correct roles_
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 3.5_

  - [ ] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Admin Login Authentication Success
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - Verify: (1) 200 OK response, (2) valid JWT access and refresh tokens, (3) user email is admin@cvr.ac.in, (4) role is ROLE_ADMIN, (5) isActive is true
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Non-Admin Authentication Behavior
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm faculty authentication still works (rajesh.kumar@cvr.ac.in / faculty123)
    - Confirm student authentication still works (cse21a001@cvr.ac.in / student123)
    - Confirm brute force protection still works for invalid passwords
    - Confirm JWT token generation still includes correct roles and claims
    - Confirm all tests still pass after fix (no regressions)

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
