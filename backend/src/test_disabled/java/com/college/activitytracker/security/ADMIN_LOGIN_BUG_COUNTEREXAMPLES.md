# Admin Login Bug - Counterexamples Documentation

## Bug Description

Admin user with credentials `admin@cvr.ac.in` / `admin123` cannot login. The login page displays "Login failed. Please check your credentials." error message.

## Test Implementation

**Test File**: `backend/src/test/java/com/college/activitytracker/security/AdminLoginBugTest.java`

**Test Status**: ✅ Created (awaiting execution on unfixed database)

## Diagnostic Tests Created

### Test 1: Admin User Existence
**Purpose**: Verify admin user document exists in users collection

**Query**: `userRepository.findByEmail("admin@cvr.ac.in")`

**Expected Outcome on UNFIXED Database**: MAY FAIL
- **Counterexample**: "Admin user admin@cvr.ac.in does not exist in database"
- **Root Cause**: Missing admin user document
- **Evidence**: UserRepository.findByEmail() returns Optional.empty()

### Test 2: Password Hash Verification
**Purpose**: Verify stored password hash matches BCrypt hash of "admin123"

**Verification**: `passwordEncoder.matches("admin123", user.getPassword())`

**Expected Outcome on UNFIXED Database**: MAY FAIL
- **Counterexample**: "Admin password hash does not match BCrypt hash of 'admin123'"
- **Root Cause**: Incorrect password hash
- **Possible Issues**:
  - Password stored as plaintext "admin123" instead of hashed
  - Password hashed with different algorithm
  - BCrypt salt rounds mismatch

### Test 3: Role Field Verification
**Purpose**: Verify role field is exactly "ROLE_ADMIN"

**Check**: `user.getRole().equals("ROLE_ADMIN")`

**Expected Outcome on UNFIXED Database**: MAY FAIL
- **Counterexample**: "Admin role is 'ADMIN' instead of 'ROLE_ADMIN'"
- **Root Cause**: Incorrect role assignment
- **Issue**: Spring Security requires "ROLE_" prefix for role-based authorization

### Test 4: Active Status Verification
**Purpose**: Verify isActive field is set to true

**Check**: `user.getIsActive() == true`

**Expected Outcome on UNFIXED Database**: MAY FAIL
- **Counterexample**: "Admin isActive is false/null instead of true"
- **Root Cause**: Inactive account status or missing field
- **Issue**: UserRepository.findByEmailAndIsActiveTrue() requires isActive=true

## Main Authentication Test

### Test 5: Admin Login Success
**Purpose**: Verify login with admin@cvr.ac.in / admin123 succeeds

**Request**: POST /api/v1/auth/login
```json
{
  "email": "admin@cvr.ac.in",
  "password": "admin123"
}
```

**Assertions**:
1. Response status is 200 OK
2. Response contains valid JWT access token
3. Response contains valid JWT refresh token
4. User email is admin@cvr.ac.in
5. User role is ROLE_ADMIN
6. User isActive is true

**Expected Outcome on UNFIXED Database**: TEST FAILS
- **Counterexample**: "Login with admin@cvr.ac.in / admin123 returns authentication failure"
- **Root Causes**: One or more of the following:
  - Admin user missing from database
  - Password hash incorrect
  - Role field wrong
  - isActive false/null

**Expected Outcome on FIXED Database**: TEST PASSES
- Login returns 200 OK with JWT tokens
- All assertions pass
- Bug is confirmed fixed

## Test Execution Instructions

### To Run Tests:
```bash
# Run all admin login bug tests
mvn test -Dtest=AdminLoginBugTest -f backend/pom.xml

# Run specific diagnostic test
mvn test -Dtest=AdminLoginBugTest#testAdminUserExists -f backend/pom.xml
mvn test -Dtest=AdminLoginBugTest#testAdminPasswordHashIsCorrect -f backend/pom.xml
mvn test -Dtest=AdminLoginBugTest#testAdminRoleIsCorrect -f backend/pom.xml
mvn test -Dtest=AdminLoginBugTest#testAdminAccountIsActive -f backend/pom.xml

# Run main authentication test
mvn test -Dtest=AdminLoginBugTest#testAdminLoginSucceeds -f backend/pom.xml
```

### Alternative: Run Diagnostic Script
```bash
# Check database state directly
node check-admin-user.js
```

## Counterexamples Summary

The bug condition exploration test is designed to surface one or more of these counterexamples:

1. **Missing User**: Admin user document does not exist in users collection
2. **Wrong Password Hash**: Password hash doesn't match BCrypt hash of "admin123"
3. **Wrong Role**: Role field is not "ROLE_ADMIN" (may be "ADMIN" without prefix)
4. **Inactive Account**: isActive field is false or null

## Test Methodology

This follows the **Bug Condition Exploration** methodology:

1. **CRITICAL**: Test MUST FAIL on unfixed database (failure confirms bug exists)
2. **DO NOT** attempt to fix the test or database when it fails
3. **GOAL**: Surface counterexamples that demonstrate the bug
4. **NOTE**: Test encodes expected behavior - will validate fix when it passes after implementation

## Next Steps

1. ✅ Test created: `AdminLoginBugTest.java`
2. ⏳ Run test on UNFIXED database to surface counterexamples
3. ⏳ Document which counterexample(s) are found
4. ⏳ Implement fix based on root cause
5. ⏳ Re-run test on FIXED database to verify fix

## Notes

- Test compilation blocked by pre-existing test suite compilation errors (71 errors in other test files)
- Test file itself is syntactically correct and follows Spring Boot test patterns
- Diagnostic script `check-admin-user.js` created as alternative verification method
- MongoDB must be running to execute tests or diagnostic script
