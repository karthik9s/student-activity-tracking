package com.college.activitytracker.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for role hierarchy configuration.
 * Tests Requirement 2.5: System must enforce role hierarchy (ADMIN > FACULTY > STUDENT)
 * where higher roles can access lower role endpoints.
 * 
 * This test verifies the role hierarchy without requiring full Spring context.
 */
class RoleHierarchyUnitTest {

    private RoleHierarchy roleHierarchy;

    @BeforeEach
    void setUp() {
        // Create the same role hierarchy as configured in SecurityConfig
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        String hierarchyString = "ROLE_ADMIN > ROLE_FACULTY \n ROLE_FACULTY > ROLE_STUDENT";
        hierarchy.setHierarchy(hierarchyString);
        this.roleHierarchy = hierarchy;
    }

    @Test
    void testAdminInheritsAllRoles() {
        // Given: A user with ROLE_ADMIN
        GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        
        // When: Getting reachable authorities through hierarchy
        Collection<? extends GrantedAuthority> reachableAuthorities = 
            roleHierarchy.getReachableGrantedAuthorities(List.of(adminAuthority));
        
        // Then: Admin should have access to all three roles
        assertTrue(containsRole(reachableAuthorities, "ROLE_ADMIN"),
            "Admin should have ROLE_ADMIN");
        assertTrue(containsRole(reachableAuthorities, "ROLE_FACULTY"),
            "Admin should inherit ROLE_FACULTY");
        assertTrue(containsRole(reachableAuthorities, "ROLE_STUDENT"),
            "Admin should inherit ROLE_STUDENT");
        assertEquals(3, reachableAuthorities.size(), 
            "Admin should have exactly 3 roles (ADMIN, FACULTY, STUDENT)");
    }

    @Test
    void testFacultyInheritsStudentRole() {
        // Given: A user with ROLE_FACULTY
        GrantedAuthority facultyAuthority = new SimpleGrantedAuthority("ROLE_FACULTY");
        
        // When: Getting reachable authorities through hierarchy
        Collection<? extends GrantedAuthority> reachableAuthorities = 
            roleHierarchy.getReachableGrantedAuthorities(List.of(facultyAuthority));
        
        // Then: Faculty should have FACULTY and STUDENT roles, but not ADMIN
        assertTrue(containsRole(reachableAuthorities, "ROLE_FACULTY"),
            "Faculty should have ROLE_FACULTY");
        assertTrue(containsRole(reachableAuthorities, "ROLE_STUDENT"),
            "Faculty should inherit ROLE_STUDENT");
        assertFalse(containsRole(reachableAuthorities, "ROLE_ADMIN"),
            "Faculty should NOT have ROLE_ADMIN");
        assertEquals(2, reachableAuthorities.size(), 
            "Faculty should have exactly 2 roles (FACULTY, STUDENT)");
    }

    @Test
    void testStudentHasNoInheritedRoles() {
        // Given: A user with ROLE_STUDENT
        GrantedAuthority studentAuthority = new SimpleGrantedAuthority("ROLE_STUDENT");
        
        // When: Getting reachable authorities through hierarchy
        Collection<? extends GrantedAuthority> reachableAuthorities = 
            roleHierarchy.getReachableGrantedAuthorities(List.of(studentAuthority));
        
        // Then: Student should only have ROLE_STUDENT
        assertTrue(containsRole(reachableAuthorities, "ROLE_STUDENT"),
            "Student should have ROLE_STUDENT");
        assertFalse(containsRole(reachableAuthorities, "ROLE_FACULTY"),
            "Student should NOT have ROLE_FACULTY");
        assertFalse(containsRole(reachableAuthorities, "ROLE_ADMIN"),
            "Student should NOT have ROLE_ADMIN");
        assertEquals(1, reachableAuthorities.size(), 
            "Student should have exactly 1 role (STUDENT)");
    }

    @Test
    void testRoleHierarchyTransitivity() {
        // Given: A user with ROLE_ADMIN
        GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        
        // When: Getting reachable authorities
        Collection<? extends GrantedAuthority> reachableAuthorities = 
            roleHierarchy.getReachableGrantedAuthorities(List.of(adminAuthority));
        
        // Then: Admin should transitively inherit STUDENT through FACULTY
        // ADMIN > FACULTY > STUDENT means ADMIN gets both FACULTY and STUDENT
        assertTrue(containsRole(reachableAuthorities, "ROLE_STUDENT"),
            "Admin should transitively inherit ROLE_STUDENT through ROLE_FACULTY");
    }

    /**
     * Helper method to check if a collection of authorities contains a specific role.
     */
    private boolean containsRole(Collection<? extends GrantedAuthority> authorities, String role) {
        return authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals(role));
    }
}
