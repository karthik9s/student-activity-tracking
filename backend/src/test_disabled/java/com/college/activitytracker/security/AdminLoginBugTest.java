package com.college.activitytracker.security;

import com.college.activitytracker.dto.LoginRequest;
import com.college.activitytracker.model.User;
import com.college.activitytracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Test for Admin Login Credentials Fix
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 2.1, 2.2, 2.3**
 * 
 * **Property 1: Fault Condition** - Admin Login Authentication Failure
 * 
 * CRITICAL: This test MUST FAIL on unfixed database - failure confirms the bug exists
 * DO NOT attempt to fix the test or the database when it fails
 * 
 * NOTE: This test encodes the expected behavior - it will validate the fix when it passes after implementation
 * 
 * GOAL: Surface counterexamples that demonstrate the bug exists in the current database state
 * 
 * Bug Description:
 * - Admin user with credentials admin@cvr.ac.in / admin123 cannot login
 * - Login page displays "Login failed. Please check your credentials." error message
 * - Root causes: missing user, incorrect password hash, wrong role, or inactive status
 * 
 * Test Approach:
 * - Run diagnostic checks on UNFIXED database: query users collection for admin@cvr.ac.in
 * - Verify password hash format, check role field, verify isActive status
 * - Send POST request to /api/v1/auth/login with admin@cvr.ac.in / admin123
 * - Verify authentication succeeds with 200 OK and JWT tokens
 * 
 * Expected Outcome on UNFIXED database:
 * - Test FAILS (this is correct - it proves the bug exists)
 * - Counterexamples: admin user missing, OR password hash incorrect, OR role wrong, OR isActive false/null
 * 
 * Expected Outcome on FIXED database:
 * - Test PASSES with 200 OK and valid JWT tokens
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminLoginBugTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ADMIN_EMAIL = "admin@cvr.ac.in";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String EXPECTED_ROLE = "ROLE_ADMIN";

    /**
     * Diagnostic Test 1: Verify admin user exists in database
     * 
     * This test checks if the admin user document exists in the users collection.
     * 
     * EXPECTED OUTCOME ON UNFIXED DATABASE: May FAIL
     * - Admin user document is missing from users collection
     * - Counterexample: "Admin user admin@cvr.ac.in does not exist in database"
     * 
     * EXPECTED OUTCOME ON FIXED DATABASE: PASSES
     * - Admin user document exists with email admin@cvr.ac.in
     */
    @Test
    @DisplayName("Diagnostic: Admin user should exist in database")
    void testAdminUserExists() {
        // Act: Query users collection for admin@cvr.ac.in
        Optional<User> adminUser = userRepository.findByEmail(ADMIN_EMAIL);

        // Assert: Verify admin user exists
        assertTrue(adminUser.isPresent(),
            String.format("COUNTEREXAMPLE FOUND: Admin user %s does not exist in database. " +
                "This confirms one root cause: missing admin user document. " +
                "Expected: Admin user should exist in users collection.",
                ADMIN_EMAIL)
        );

        // Document the finding
        if (adminUser.isPresent()) {
            User user = adminUser.get();
            System.out.println("=== DIAGNOSTIC: Admin User Found ===");
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("IsActive: " + user.getIsActive());
            System.out.println("Password Hash: " + (user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) + "..." : "null"));
        } else {
            System.out.println("=== DIAGNOSTIC: Admin User NOT Found ===");
            System.out.println("Email searched: " + ADMIN_EMAIL);
        }
    }

    /**
     * Diagnostic Test 2: Verify admin password hash is correct
     * 
     * This test checks if the stored password hash matches the BCrypt hash of "admin123".
     * 
     * EXPECTED OUTCOME ON UNFIXED DATABASE: May FAIL
     * - Password hash doesn't match BCrypt hash of "admin123"
     * - Counterexample: "Admin password hash does not match BCrypt hash of 'admin123'"
     * 
     * EXPECTED OUTCOME ON FIXED DATABASE: PASSES
     * - Password hash matches BCrypt hash of "admin123"
     */
    @Test
    @DisplayName("Diagnostic: Admin password hash should be correct")
    void testAdminPasswordHashIsCorrect() {
        // Arrange: Get admin user from database
        Optional<User> adminUser = userRepository.findByEmail(ADMIN_EMAIL);
        
        // Skip test if admin user doesn't exist (covered by previous test)
        Assumptions.assumeTrue(adminUser.isPresent(), "Admin user must exist for this test");

        User user = adminUser.get();

        // Act: Verify password hash matches "admin123"
        boolean passwordMatches = passwordEncoder.matches(ADMIN_PASSWORD, user.getPassword());

        // Assert: Verify password hash is correct
        assertTrue(passwordMatches,
            String.format("COUNTEREXAMPLE FOUND: Admin password hash does not match BCrypt hash of '%s'. " +
                "Stored hash: %s... " +
                "This confirms one root cause: incorrect password hash. " +
                "Expected: Password hash should match BCrypt hash of 'admin123'.",
                ADMIN_PASSWORD,
                user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) : "null")
        );

        // Document the finding
        System.out.println("=== DIAGNOSTIC: Password Hash Verification ===");
        System.out.println("Password matches: " + passwordMatches);
        System.out.println("Hash format: " + (user.getPassword() != null && user.getPassword().startsWith("$2a$") ? "BCrypt" : "Unknown"));
    }

    /**
     * Diagnostic Test 3: Verify admin role is correct
     * 
     * This test checks if the role field is set to "ROLE_ADMIN".
     * 
     * EXPECTED OUTCOME ON UNFIXED DATABASE: May FAIL
     * - Role field is not "ROLE_ADMIN" (may be "ADMIN" or missing)
     * - Counterexample: "Admin role is 'ADMIN' instead of 'ROLE_ADMIN'"
     * 
     * EXPECTED OUTCOME ON FIXED DATABASE: PASSES
     * - Role field is exactly "ROLE_ADMIN"
     */
    @Test
    @DisplayName("Diagnostic: Admin role should be ROLE_ADMIN")
    void testAdminRoleIsCorrect() {
        // Arrange: Get admin user from database
        Optional<User> adminUser = userRepository.findByEmail(ADMIN_EMAIL);
        
        // Skip test if admin user doesn't exist (covered by previous test)
        Assumptions.assumeTrue(adminUser.isPresent(), "Admin user must exist for this test");

        User user = adminUser.get();

        // Assert: Verify role is exactly "ROLE_ADMIN"
        assertEquals(EXPECTED_ROLE, user.getRole(),
            String.format("COUNTEREXAMPLE FOUND: Admin role is '%s' instead of '%s'. " +
                "This confirms one root cause: incorrect role assignment. " +
                "Expected: Role field should be exactly 'ROLE_ADMIN' (with ROLE_ prefix).",
                user.getRole(), EXPECTED_ROLE)
        );

        // Document the finding
        System.out.println("=== DIAGNOSTIC: Role Verification ===");
        System.out.println("Role: " + user.getRole());
        System.out.println("Expected: " + EXPECTED_ROLE);
        System.out.println("Matches: " + EXPECTED_ROLE.equals(user.getRole()));
    }

    /**
     * Diagnostic Test 4: Verify admin account is active
     * 
     * This test checks if the isActive field is set to true.
     * 
     * EXPECTED OUTCOME ON UNFIXED DATABASE: May FAIL
     * - isActive field is false or null
     * - Counterexample: "Admin isActive is false/null instead of true"
     * 
     * EXPECTED OUTCOME ON FIXED DATABASE: PASSES
     * - isActive field is boolean true
     */
    @Test
    @DisplayName("Diagnostic: Admin account should be active")
    void testAdminAccountIsActive() {
        // Arrange: Get admin user from database
        Optional<User> adminUser = userRepository.findByEmail(ADMIN_EMAIL);
        
        // Skip test if admin user doesn't exist (covered by previous test)
        Assumptions.assumeTrue(adminUser.isPresent(), "Admin user must exist for this test");

        User user = adminUser.get();

        // Assert: Verify isActive is true
        assertNotNull(user.getIsActive(),
            "COUNTEREXAMPLE FOUND: Admin isActive field is null. " +
            "This confirms one root cause: missing isActive field. " +
            "Expected: isActive should be boolean true."
        );

        assertTrue(user.getIsActive(),
            String.format("COUNTEREXAMPLE FOUND: Admin isActive is %s instead of true. " +
                "This confirms one root cause: inactive account status. " +
                "Expected: isActive should be boolean true.",
                user.getIsActive())
        );

        // Document the finding
        System.out.println("=== DIAGNOSTIC: Active Status Verification ===");
        System.out.println("IsActive: " + user.getIsActive());
        System.out.println("Expected: true");
    }

    /**
     * Main Bug Condition Test: Admin login should succeed
     * 
     * This test verifies that login with admin@cvr.ac.in / admin123 succeeds.
     * 
     * Test Assertions:
     * 1. Response status is 200 OK
     * 2. Response contains valid JWT access token
     * 3. Response contains valid JWT refresh token
     * 4. User email is admin@cvr.ac.in
     * 5. User role is ROLE_ADMIN
     * 6. User isActive is true
     * 
     * EXPECTED OUTCOME ON UNFIXED DATABASE: Test FAILS
     * - Login returns authentication failure
     * - This proves the bug exists
     * - Counterexample documents which root cause is present
     * 
     * EXPECTED OUTCOME ON FIXED DATABASE: Test PASSES
     * - Login returns 200 OK with JWT tokens
     * - This proves the bug is fixed
     */
    @Test
    @DisplayName("Property 1: Admin login with admin@cvr.ac.in / admin123 should succeed")
    void testAdminLoginSucceeds() throws Exception {
        // Arrange: Create login request with admin credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(ADMIN_EMAIL);
        loginRequest.setPassword(ADMIN_PASSWORD);
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        // Act: Send POST request to login endpoint
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andReturn();

        int status = result.getResponse().getStatus();
        String responseBody = result.getResponse().getContentAsString();

        // Document the response
        System.out.println("=== AUTHENTICATION TEST RESULT ===");
        System.out.println("Status: " + status);
        System.out.println("Response body: " + responseBody);

        // Assert 1: Verify response status is 200 OK
        assertEquals(200, status,
            String.format("COUNTEREXAMPLE FOUND: Login with admin@cvr.ac.in / admin123 returns %d instead of 200 OK. " +
                "Response: %s. " +
                "This confirms the bug: admin credentials fail authentication. " +
                "Root causes: admin user missing, OR password hash incorrect, OR role wrong, OR isActive false/null. " +
                "Expected: Login should return 200 OK with JWT tokens.",
                status, responseBody)
        );

        // Assert 2: Verify response body is not empty
        assertFalse(responseBody.isEmpty(),
            "COUNTEREXAMPLE FOUND: Login response body is empty. " +
            "Expected: Response should contain JWT tokens and user details."
        );

        // Parse response to verify token structure
        var responseMap = objectMapper.readValue(responseBody, java.util.Map.class);

        // Assert 3: Verify access token is present
        assertTrue(responseMap.containsKey("accessToken"),
            "COUNTEREXAMPLE FOUND: Response does not contain accessToken field. " +
            "Expected: Response should contain valid JWT access token."
        );
        assertNotNull(responseMap.get("accessToken"),
            "COUNTEREXAMPLE FOUND: accessToken is null. " +
            "Expected: Response should contain valid JWT access token."
        );

        // Assert 4: Verify refresh token is present
        assertTrue(responseMap.containsKey("refreshToken"),
            "COUNTEREXAMPLE FOUND: Response does not contain refreshToken field. " +
            "Expected: Response should contain valid JWT refresh token."
        );
        assertNotNull(responseMap.get("refreshToken"),
            "COUNTEREXAMPLE FOUND: refreshToken is null. " +
            "Expected: Response should contain valid JWT refresh token."
        );

        // Assert 5: Verify user details are present
        assertTrue(responseMap.containsKey("user"),
            "COUNTEREXAMPLE FOUND: Response does not contain user field. " +
            "Expected: Response should contain user details."
        );

        @SuppressWarnings("unchecked")
        var userMap = (java.util.Map<String, Object>) responseMap.get("user");

        // Assert 6: Verify user email is admin@cvr.ac.in
        assertEquals(ADMIN_EMAIL, userMap.get("email"),
            String.format("COUNTEREXAMPLE FOUND: User email is '%s' instead of '%s'. " +
                "Expected: User email should be admin@cvr.ac.in.",
                userMap.get("email"), ADMIN_EMAIL)
        );

        // Assert 7: Verify user role is ROLE_ADMIN
        assertEquals(EXPECTED_ROLE, userMap.get("role"),
            String.format("COUNTEREXAMPLE FOUND: User role is '%s' instead of '%s'. " +
                "Expected: User role should be ROLE_ADMIN.",
                userMap.get("role"), EXPECTED_ROLE)
        );

        // Assert 8: Verify user isActive is true
        assertEquals(true, userMap.get("isActive"),
            String.format("COUNTEREXAMPLE FOUND: User isActive is '%s' instead of true. " +
                "Expected: User isActive should be true.",
                userMap.get("isActive"))
        );

        System.out.println("=== TEST PASSED: Admin login successful ===");
        System.out.println("Access token: " + responseMap.get("accessToken").toString().substring(0, 20) + "...");
        System.out.println("User email: " + userMap.get("email"));
        System.out.println("User role: " + userMap.get("role"));
        System.out.println("User isActive: " + userMap.get("isActive"));
    }
}
