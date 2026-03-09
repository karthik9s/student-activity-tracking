package com.college.activitytracker.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify role hierarchy enforcement.
 * Tests Requirement 2.5: System must enforce role hierarchy (ADMIN > FACULTY > STUDENT)
 * where higher roles can access lower role endpoints.
 */
@SpringBootTest
class RoleHierarchyTest {

    @Autowired
    private RoleHierarchy roleHierarchy;

    @Test
    void testAdminHasAllRoles() {
        // Given: A user with ROLE_ADMIN
        GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        
        // When: Getting reachable authorities
        Collection<? extends GrantedAuthority> reachableAuthorities = 
            roleHierarchy.getReachableGrantedAuthorities(List.of(adminAuthority));
        
        // Then: Admin should have access to ADMIN, FACULTY, and STUDENT roles
        assertTrue(reachableAuthorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")),
            "Admin should have ROLE_ADMIN");
        assertTrue(reachableAuthorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_FACULTY")),
            "Admin should have ROLE_FACULTY through hierarchy");
        assertTrue(reachableAuthorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")),
            "Admin should have ROLE_STUDENT through hierarchy");
        assertEquals(3, reachableAuthorities.size(), 
            "Admin should have exactly 3 roles");
    }

    @Test
    void testFacultyHasFacultyAndStudentRoles() {
        // Given: A user with ROLE_FACULTY
        GrantedAuthority facultyAuthority = new SimpleGrantedAuthority("ROLE_FACULTY");
        
        // When: Getting reachable authorities
        Collection<? extends GrantedAuthority> reachableAuthorities = 
            roleHierarchy.getReachableGrantedAuthorities(List.of(facultyAuthority));
        
        // Then: Faculty should have access to FACULTY and STUDENT roles, but not ADMIN
        assertTrue(reachableAuthorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_FACULTY")),
            "Faculty should have ROLE_FACULTY");
        assertTrue(reachableAuthorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")),
            "Faculty should have ROLE_STUDENT through hierarchy");
        assertFalse(reachableAuthorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")),
            "Faculty should NOT have ROLE_ADMIN");
        assertEquals(2, reachableAuthorities.size(), 
            "Faculty should have exactly 2 roles");
    }

    @Test
    void testStudentHasOnlyStudentRole() {
        // Given: A user with ROLE_STUDENT
        GrantedAuthority studentAuthority = new SimpleGrantedAuthority("ROLE_STUDENT");
        
        // When: Getting reachable authorities
        Collection<? extends GrantedAuthority> reachableAuthorities = 
            roleHierarchy.getReachableGrantedAuthorities(List.of(studentAuthority));
        
        // Then: Student should only have ROLE_STUDENT
        assertTrue(reachableAuthorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")),
            "Student should have ROLE_STUDENT");
        assertFalse(reachableAuthorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_FACULTY")),
            "Student should NOT have ROLE_FACULTY");
        assertFalse(reachableAuthorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")),
            "Student should NOT have ROLE_ADMIN");
        assertEquals(1, reachableAuthorities.size(), 
            "Student should have exactly 1 role");
    }

    @Test
    void testRoleHierarchyBeanExists() {
        // Verify that the RoleHierarchy bean is properly configured
        assertNotNull(roleHierarchy, "RoleHierarchy bean should be configured");
    }
}
