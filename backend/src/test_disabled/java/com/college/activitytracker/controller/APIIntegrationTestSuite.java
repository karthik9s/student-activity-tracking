package com.college.activitytracker.controller;

import com.college.activitytracker.dto.*;
import com.college.activitytracker.model.*;
import com.college.activitytracker.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for API endpoints
 * Tests the full request-response cycle including security
 */
@SpringBootTest
@AutoConfigureMockMvc
public class APIIntegrationTestSuite {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    public void setup() {
        // Clean up test data before each test
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
        courseRepository.deleteAll();
    }

    // ==================== Authentication Tests ====================

    @Test
    public void testLogin_WithValidCredentials_ReturnsToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@test.com");
        loginRequest.setPassword("password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    public void testLogin_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("invalid@test.com");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Student Endpoint Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllStudents_AsAdmin_ReturnsStudentList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/students")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testGetAllStudents_AsStudent_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateStudent_WithValidData_ReturnsCreated() throws Exception {
        // Arrange
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setRollNumber("S001");
        studentDTO.setName("John Doe");
        studentDTO.setEmail("john@test.com");
        studentDTO.setPhone("1234567890");

        // Act & Assert
        mockMvc.perform(post("/api/admin/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rollNumber").value("S001"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateStudent_WithInvalidData_ReturnsBadRequest() throws Exception {
        // Arrange - Missing required fields
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setRollNumber("S002");
        // Missing name and email

        // Act & Assert
        mockMvc.perform(post("/api/admin/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Faculty Endpoint Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllFaculty_AsAdmin_ReturnsFacultyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/faculty")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateFaculty_WithValidData_ReturnsCreated() throws Exception {
        // Arrange
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setEmployeeId("F001");
        facultyDTO.setName("Dr. Smith");
        facultyDTO.setEmail("smith@test.com");
        facultyDTO.setPhone("9876543210");
        facultyDTO.setDepartment("Computer Science");

        // Act & Assert
        mockMvc.perform(post("/api/admin/faculty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value("F001"))
                .andExpect(jsonPath("$.name").value("Dr. Smith"));
    }

    // ==================== Course Endpoint Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllCourses_AsAdmin_ReturnsCourseList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateCourse_WithValidData_ReturnsCreated() throws Exception {
        // Arrange
        Course course = new Course();
        course.setCode("CS101");
        course.setName("Computer Science");
        course.setDuration(4);

        // Act & Assert
        mockMvc.perform(post("/api/admin/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CS101"))
                .andExpect(jsonPath("$.name").value("Computer Science"));
    }

    // ==================== Attendance Endpoint Tests ====================

    @Test
    @WithMockUser(roles = "FACULTY")
    public void testMarkAttendance_AsFaculty_ReturnsCreated() throws Exception {
        // Arrange
        Attendance attendance = new Attendance();
        attendance.setStudentId("student-id");
        attendance.setSubjectId("subject-id");
        attendance.setPresent(true);

        // Act & Assert
        mockMvc.perform(post("/api/faculty/attendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attendance)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testMarkAttendance_AsStudent_ReturnsForbidden() throws Exception {
        // Arrange
        Attendance attendance = new Attendance();
        attendance.setStudentId("student-id");
        attendance.setSubjectId("subject-id");
        attendance.setPresent(true);

        // Act & Assert
        mockMvc.perform(post("/api/faculty/attendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attendance)))
                .andExpect(status().isForbidden());
    }

    // ==================== Performance Endpoint Tests ====================

    @Test
    @WithMockUser(roles = "FACULTY")
    public void testCreatePerformance_AsFaculty_ReturnsCreated() throws Exception {
        // Arrange
        Performance performance = new Performance();
        performance.setStudentId("student-id");
        performance.setSubjectId("subject-id");
        performance.setObtainedMarks(85.0);
        performance.setMaxMarks(100.0);
        performance.setAssessmentType("MIDTERM");

        // Act & Assert
        mockMvc.perform(post("/api/faculty/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(performance)))
                .andExpect(status().isCreated());
    }

    // ==================== Student Data Access Tests ====================

    @Test
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    public void testGetStudentAttendance_AsStudent_ReturnsOwnData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/student/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    public void testGetStudentPerformance_AsStudent_ReturnsOwnData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/student/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
