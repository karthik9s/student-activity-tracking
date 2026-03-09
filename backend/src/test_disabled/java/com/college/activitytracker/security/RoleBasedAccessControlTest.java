package com.college.activitytracker.security;

import com.college.activitytracker.dto.LoginRequest;
import com.college.activitytracker.dto.RegisterRequest;
import com.college.activitytracker.model.User;
import com.college.activitytracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for role-based access control enforcement.
 * Tests Requirements 2.2 and 2.3:
 * - System must verify user role on every protected endpoint request
 * - System must return 403 Forbidden for insufficient privileges
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Role-Based Access Control Tests")
class RoleBasedAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String facultyToken;
    private String studentToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up existing test users
        userRepository.deleteAll();

        // Create test users with different roles
        createTestUser("admin@test.com", "Admin123!", "ROLE_ADMIN");
        createTestUser("faculty@test.com", "Faculty123!", "ROLE_FACULTY");
        createTestUser("student@test.com", "Student123!", "ROLE_STUDENT");

        // Obtain tokens for each role
        adminToken = loginAndGetToken("admin@test.com", "Admin123!");
        facultyToken = loginAndGetToken("faculty@test.com", "Faculty123!");
        studentToken = loginAndGetToken("student@test.com", "Student123!");
    }

    private void createTestUser(String email, String password, String role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    // ==================== Admin Endpoint Tests ====================

    @Test
    @DisplayName("Admin can access admin endpoints")
    void adminCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Faculty cannot access admin endpoints - returns 403")
    void facultyCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("Student cannot access admin endpoints - returns 403")
    void studentCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("Unauthenticated user cannot access admin endpoints - returns 401")
    void unauthenticatedUserCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/students"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Faculty Endpoint Tests ====================

    @Test
    @DisplayName("Faculty can access faculty endpoints")
    void facultyCanAccessFacultyEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/faculty/allocations")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin cannot access faculty endpoints - returns 403")
    void adminCannotAccessFacultyEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/faculty/allocations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("Student cannot access faculty endpoints - returns 403")
    void studentCannotAccessFacultyEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/faculty/allocations")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("Unauthenticated user cannot access faculty endpoints - returns 401")
    void unauthenticatedUserCannotAccessFacultyEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/faculty/allocations"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Student Endpoint Tests ====================

    @Test
    @DisplayName("Student can access student endpoints")
    void studentCanAccessStudentEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/student/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin cannot access student endpoints - returns 403")
    void adminCannotAccessStudentEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/student/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("Faculty cannot access student endpoints - returns 403")
    void facultyCannotAccessStudentEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/student/profile")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("Unauthenticated user cannot access student endpoints - returns 401")
    void unauthenticatedUserCannotAccessStudentEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/student/profile"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Auth Endpoint Tests ====================

    @Test
    @DisplayName("Anyone can access login endpoint")
    void anyoneCanAccessLoginEndpoint() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@test.com");
        loginRequest.setPassword("Admin123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Only admin can access register endpoint")
    void onlyAdminCanAccessRegisterEndpoint() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setRole("ROLE_STUDENT");

        // Admin can register
        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Faculty cannot register new users - returns 403")
    void facultyCannotRegisterUsers() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser2@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setRole("ROLE_STUDENT");

        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("Student cannot register new users - returns 403")
    void studentCannotRegisterUsers() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser3@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setRole("ROLE_STUDENT");

        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    // ==================== Cross-Role Access Tests ====================

    @Test
    @DisplayName("Verify all admin endpoints require ADMIN role")
    void verifyAdminEndpointsRequireAdminRole() throws Exception {
        String[] adminEndpoints = {
                "/api/v1/admin/students",
                "/api/v1/admin/faculty",
                "/api/v1/admin/courses",
                "/api/v1/admin/subjects",
                "/api/v1/admin/allocations",
                "/api/v1/admin/dashboard/stats"
        };

        for (String endpoint : adminEndpoints) {
            // Faculty should get 403
            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + facultyToken))
                    .andExpect(status().isForbidden());

            // Student should get 403
            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());

            // Admin should succeed
            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Verify all faculty endpoints require FACULTY role")
    void verifyFacultyEndpointsRequireFacultyRole() throws Exception {
        String[] facultyEndpoints = {
                "/api/v1/faculty/allocations"
        };

        for (String endpoint : facultyEndpoints) {
            // Admin should get 403
            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());

            // Student should get 403
            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());

            // Faculty should succeed
            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + facultyToken))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Verify all student endpoints require STUDENT role")
    void verifyStudentEndpointsRequireStudentRole() throws Exception {
        String[] studentEndpoints = {
                "/api/v1/student/profile",
                "/api/v1/student/attendance",
                "/api/v1/student/performance",
                "/api/v1/student/dashboard/stats"
        };

        for (String endpoint : studentEndpoints) {
            // Admin should get 403
            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());

            // Faculty should get 403
            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + facultyToken))
                    .andExpect(status().isForbidden());

            // Student should succeed (may return 404 or other errors, but not 403)
            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isNot(403));
        }
    }
}
