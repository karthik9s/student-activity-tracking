package com.college.activitytracker.security;

import com.college.activitytracker.dto.LoginRequest;
import com.college.activitytracker.model.User;
import com.college.activitytracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Test for Login Authentication Fix
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**
 * 
 * **Property 1: Fault Condition** - Authentication Endpoints Blocked Before Controller
 * 
 * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists
 * DO NOT attempt to fix the test or the code when it fails
 * 
 * NOTE: This test encodes the expected behavior - it will validate the fix when it passes after implementation
 * 
 * GOAL: Surface counterexamples that demonstrate authentication requests are blocked with 401 before reaching AuthController
 * 
 * Bug Description:
 * - All login attempts return 401 Unauthorized with empty response body
 * - Valid users exist in database with correct password hashes
 * - Issue is in Spring Security filter chain configuration
 * - Requests are rejected before reaching AuthController
 * 
 * Test Approach:
 * - Create valid test users with correct BCrypt password hashes
 * - Send POST requests to /api/v1/auth/login, /api/v1/auth/logout, /api/v1/auth/register
 * - Verify requests reach controller and return appropriate responses (not 401 with empty body)
 * 
 * Expected Outcome on UNFIXED code:
 * - Test FAILS with 401 Unauthorized and empty response body (proves bug exists)
 * - Counterexample: "POST /api/v1/auth/login with valid credentials returns 401 with empty body"
 * 
 * Expected Outcome on FIXED code:
 * - Test PASSES with 200 OK for valid operations or proper error messages for failures
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginAuthenticationBugTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private static final String TEST_EMAIL = "testuser@cvr.ac.in";
    private static final String TEST_PASSWORD = "testpass123";

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();

        // Create a valid test user with correct BCrypt password hash
        testUser = new User();
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setRole("STUDENT");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        if (testUser != null && testUser.getId() != null) {
            userRepository.deleteById(testUser.getId());
        }
        // Clean up by email in case ID is not set
        userRepository.findByEmail(TEST_EMAIL).ifPresent(user -> 
            userRepository.deleteById(user.getId())
        );
    }

    /**
     * Simple JUnit test to verify the bug exists on unfixed code
     * 
     * This test demonstrates that login requests with valid credentials
     * return 401 Unauthorized with empty response body.
     * 
     * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS
     * - Login request returns 401 with empty body
     * - This proves the bug exists
     * 
     * EXPECTED OUTCOME ON FIXED CODE: Test PASSES
     * - Login request returns 200 OK with JWT tokens
     * - This proves the bug is fixed
     */
    @Test
    void testLoginWithValidCredentialsReturnsUnauthorized() throws Exception {
        // Arrange: Create login request with valid credentials
        LoginRequest loginRequest = createLoginRequest(TEST_EMAIL, TEST_PASSWORD);
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        // Act: Send POST request to login endpoint
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andReturn();

        int status = result.getResponse().getStatus();
        String responseBody = result.getResponse().getContentAsString();

        // Assert: Verify the request is NOT blocked with 401 and empty body
        // This assertion will FAIL on unfixed code (status will be 401, body will be empty)
        // This assertion will PASS on fixed code (status will be 200, body will contain tokens)
        
        boolean isBugCondition = (status == 401 && responseBody.isEmpty());
        
        assertFalse(isBugCondition,
            String.format("COUNTEREXAMPLE FOUND: POST /api/v1/auth/login with valid credentials {email='%s', password='***'} " +
                "returns 401 Unauthorized with empty response body. " +
                "This confirms the bug: authentication requests are blocked before reaching AuthController. " +
                "Expected: Request should reach controller and return 200 OK with JWT tokens.",
                TEST_EMAIL)
        );

        // Additional verification: For valid credentials, expect 200 OK
        assertEquals(200, status,
            String.format("Login with valid credentials should return 200 OK, but got %d. Response body: %s",
                status, responseBody)
        );
        
        assertFalse(responseBody.isEmpty(),
            "Login response body should contain JWT tokens, but body is empty"
        );
    }

    /**
     * Test that logout endpoint is accessible without authentication
     * 
     * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS
     * - Logout request returns 401 with empty body
     * - This proves the bug affects all auth endpoints
     * 
     * EXPECTED OUTCOME ON FIXED CODE: Test PASSES
     * - Logout request returns 200 OK
     * - This proves public endpoints are accessible
     */
    @Test
    void testLogoutEndpointIsAccessible() throws Exception {
        // Act: Send POST request to logout endpoint without JWT token
        MvcResult result = mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        String responseBody = result.getResponse().getContentAsString();

        // Assert: Verify the request is NOT blocked with 401 and empty body
        boolean isBugCondition = (status == 401 && responseBody.isEmpty());
        
        assertFalse(isBugCondition,
            "COUNTEREXAMPLE FOUND: POST /api/v1/auth/logout returns 401 Unauthorized with empty response body. " +
            "This confirms the bug: public auth endpoints are blocked before reaching AuthController. " +
            "Expected: Request should reach controller and return 200 OK."
        );

        // Logout should be accessible without authentication
        assertEquals(200, status,
            String.format("Logout endpoint should be accessible without authentication, but got %d", status)
        );
    }

    /**
     * Property 1: Authentication Endpoints Should Be Accessible Without JWT Token
     * 
     * This property-based test verifies that authentication endpoints (/api/v1/auth/**)
     * are accessible without authentication and return appropriate responses.
     * 
     * Test Strategy:
     * - Generate various authentication endpoint requests (login, logout, register)
     * - Verify requests reach the controller (not blocked with 401 before controller)
     * - Verify responses are appropriate (200 OK for valid operations, proper errors for failures)
     * 
     * Bug Condition:
     * - input.requestURI matches '/api/v1/auth/**'
     * - responseStatus == 401
     * - responseBody.isEmpty()
     * - NOT controllerReached
     * 
     * Expected Behavior:
     * - Requests reach controller
     * - Return 200 OK for valid operations or proper error messages for failures
     * 
     * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS
     * - Login requests return 401 with empty body
     * - This proves the bug exists
     * 
     * EXPECTED OUTCOME ON FIXED CODE: Test PASSES
     * - Login requests return 200 OK with JWT tokens
     * - This proves the bug is fixed
     */
    @Property(tries = 10)
    void authenticationEndpointsShouldBeAccessibleWithoutJWT(
            @ForAll("authEndpoints") String endpoint,
            @ForAll("validCredentials") LoginRequest credentials) throws Exception {
        
        // Act: Send POST request to authentication endpoint without JWT token
        MvcResult result = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andReturn();

        int status = result.getResponse().getStatus();
        String responseBody = result.getResponse().getContentAsString();

        // Assert: Verify the request is NOT blocked with 401 and empty body
        // This assertion will FAIL on unfixed code (status will be 401, body will be empty)
        // This assertion will PASS on fixed code (status will be 200 or proper error)
        
        boolean isBugCondition = (status == 401 && responseBody.isEmpty());
        
        if (isBugCondition) {
            throw new AssertionError(
                String.format("COUNTEREXAMPLE FOUND: POST %s with credentials {email='%s', password='***'} " +
                    "returns 401 Unauthorized with empty response body. " +
                    "This confirms the bug: authentication requests are blocked before reaching AuthController. " +
                    "Expected: Request should reach controller and return 200 OK with JWT tokens or proper error message.",
                    endpoint, credentials.getEmail())
            );
        }

        // Additional verification: For login endpoint with valid credentials, expect 200 OK
        if (endpoint.equals("/api/v1/auth/login") && 
            credentials.getEmail().equals(TEST_EMAIL) && 
            credentials.getPassword().equals(TEST_PASSWORD)) {
            
            if (status != 200) {
                throw new AssertionError(
                    String.format("Login with valid credentials should return 200 OK, but got %d. " +
                        "Response body: %s", status, responseBody)
                );
            }
            
            if (responseBody.isEmpty()) {
                throw new AssertionError(
                    "Login response body should contain JWT tokens, but body is empty"
                );
            }
        }
    }

    /**
     * Provides authentication endpoints for property-based testing
     */
    @Provide
    Arbitrary<String> authEndpoints() {
        return Arbitraries.of(
            "/api/v1/auth/login",
            "/api/v1/auth/logout"
        );
    }

    /**
     * Provides valid credentials for property-based testing
     */
    @Provide
    Arbitrary<LoginRequest> validCredentials() {
        return Arbitraries.just(createLoginRequest(TEST_EMAIL, TEST_PASSWORD));
    }

    /**
     * Helper method to create LoginRequest with email and password
     */
    private LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
