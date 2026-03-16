package com.college.activitytracker.controller;

import com.college.activitytracker.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/students")
@PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
@Tag(name = "Student Search", description = "Student search endpoints")
public class StudentSearchController {

    private final StudentService studentService;

    public StudentSearchController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search students by roll number", description = "Partial match search on roll number")
    public ResponseEntity<List<Map<String, Object>>> searchByRollNumber(
            @RequestParam String rollNumber) {
        return ResponseEntity.ok(studentService.searchByRollNumber(rollNumber));
    }
}
