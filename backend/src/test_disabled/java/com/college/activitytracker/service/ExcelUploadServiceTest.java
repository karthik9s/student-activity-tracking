package com.college.activitytracker.service;

import com.college.activitytracker.dto.BulkUploadResultDTO;
import com.college.activitytracker.dto.StudentDTO;
import com.college.activitytracker.model.Course;
import com.college.activitytracker.repository.CourseRepository;
import com.college.activitytracker.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelUploadServiceTest {

    @Mock
    private StudentService studentService;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ExcelUploadService excelUploadService;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setId("course1");
        testCourse.setCode("BTECH-CSE");
        testCourse.setName("B.Tech Computer Science");
    }

    @Test
    void testUploadValidExcelFile() throws IOException {
        // Create valid Excel file
        MockMultipartFile file = createValidExcelFile();

        // Mock repository responses
        when(courseRepository.findByCode("BTECH-CSE")).thenReturn(Optional.of(testCourse));
        when(studentRepository.existsByRollNumber(anyString())).thenReturn(false);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentService.createStudent(any(StudentDTO.class))).thenAnswer(invocation -> {
            StudentDTO dto = invocation.getArgument(0);
            dto.setId("student-" + dto.getRollNumber());
            return dto;
        });

        // Execute
        BulkUploadResultDTO result = excelUploadService.uploadStudentsFromExcel(file);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getErrorCount());
        assertTrue(result.getErrors().isEmpty());

        verify(studentService, times(2)).createStudent(any(StudentDTO.class));
    }

    @Test
    void testUploadExcelWithDuplicateRollNumber() throws IOException {
        // Create Excel file with duplicate roll number
        MockMultipartFile file = createExcelWithDuplicateRollNumber();

        // Mock repository responses
        when(courseRepository.findByCode("BTECH-CSE")).thenReturn(Optional.of(testCourse));
        when(studentRepository.existsByRollNumber("2024001")).thenReturn(true);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);

        // Execute
        BulkUploadResultDTO result = excelUploadService.uploadStudentsFromExcel(file);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getErrorCount());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrors().contains("Roll number already exists in system"));

        verify(studentService, never()).createStudent(any(StudentDTO.class));
    }

    @Test
    void testUploadExcelWithInvalidCourseCode() throws IOException {
        // Create Excel file with invalid course code
        MockMultipartFile file = createExcelWithInvalidCourse();

        // Mock repository responses
        when(courseRepository.findByCode("INVALID")).thenReturn(Optional.empty());
        when(studentRepository.existsByRollNumber(anyString())).thenReturn(false);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);

        // Execute
        BulkUploadResultDTO result = excelUploadService.uploadStudentsFromExcel(file);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getErrorCount());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrors().stream()
                .anyMatch(e -> e.contains("Invalid course code")));

        verify(studentService, never()).createStudent(any(StudentDTO.class));
    }

    @Test
    void testUploadExcelWithInvalidEmail() throws IOException {
        // Create Excel file with invalid email
        MockMultipartFile file = createExcelWithInvalidEmail();

        // Mock repository responses
        when(courseRepository.findByCode("BTECH-CSE")).thenReturn(Optional.of(testCourse));
        when(studentRepository.existsByRollNumber(anyString())).thenReturn(false);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);

        // Execute
        BulkUploadResultDTO result = excelUploadService.uploadStudentsFromExcel(file);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).getErrors().contains("Invalid email format"));

        verify(studentService, never()).createStudent(any(StudentDTO.class));
    }

    @Test
    void testUploadExcelWithInvalidYear() throws IOException {
        // Create Excel file with invalid year
        MockMultipartFile file = createExcelWithInvalidYear();

        // Mock repository responses
        when(courseRepository.findByCode("BTECH-CSE")).thenReturn(Optional.of(testCourse));
        when(studentRepository.existsByRollNumber(anyString())).thenReturn(false);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);

        // Execute
        BulkUploadResultDTO result = excelUploadService.uploadStudentsFromExcel(file);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).getErrors().contains("Year must be between 1 and 4"));

        verify(studentService, never()).createStudent(any(StudentDTO.class));
    }

    @Test
    void testUploadEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> {
            excelUploadService.uploadStudentsFromExcel(file);
        });
    }

    @Test
    void testUploadNonExcelFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> {
            excelUploadService.uploadStudentsFromExcel(file);
        });
    }

    @Test
    void testUploadExcelWithMissingRequiredFields() throws IOException {
        // Create Excel file with missing required fields
        MockMultipartFile file = createExcelWithMissingFields();

        // Mock repository responses
        when(courseRepository.findByCode("BTECH-CSE")).thenReturn(Optional.of(testCourse));

        // Execute
        BulkUploadResultDTO result = excelUploadService.uploadStudentsFromExcel(file);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getErrorCount());
        assertFalse(result.getErrors().isEmpty());

        verify(studentService, never()).createStudent(any(StudentDTO.class));
    }

    // Helper methods to create test Excel files

    private MockMultipartFile createValidExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Roll Number", "First Name", "Last Name", "Email", "Phone",
                "Date of Birth", "Course Code", "Year", "Section"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Create data rows
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("2024001");
        row1.createCell(1).setCellValue("John");
        row1.createCell(2).setCellValue("Doe");
        row1.createCell(3).setCellValue("john.doe@example.com");
        row1.createCell(4).setCellValue("9876543210");
        row1.createCell(5).setCellValue("2005-05-15");
        row1.createCell(6).setCellValue("BTECH-CSE");
        row1.createCell(7).setCellValue(1);
        row1.createCell(8).setCellValue("A");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("2024002");
        row2.createCell(1).setCellValue("Jane");
        row2.createCell(2).setCellValue("Smith");
        row2.createCell(3).setCellValue("jane.smith@example.com");
        row2.createCell(4).setCellValue("9876543211");
        row2.createCell(5).setCellValue("2005-06-20");
        row2.createCell(6).setCellValue("BTECH-CSE");
        row2.createCell(7).setCellValue(1);
        row2.createCell(8).setCellValue("A");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );
    }

    private MockMultipartFile createExcelWithDuplicateRollNumber() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Roll Number", "First Name", "Last Name", "Email", "Phone",
                "Date of Birth", "Course Code", "Year", "Section"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Create data row with existing roll number
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("2024001");
        row1.createCell(1).setCellValue("John");
        row1.createCell(2).setCellValue("Doe");
        row1.createCell(3).setCellValue("john.doe@example.com");
        row1.createCell(4).setCellValue("9876543210");
        row1.createCell(5).setCellValue("2005-05-15");
        row1.createCell(6).setCellValue("BTECH-CSE");
        row1.createCell(7).setCellValue(1);
        row1.createCell(8).setCellValue("A");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );
    }

    private MockMultipartFile createExcelWithInvalidCourse() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Roll Number", "First Name", "Last Name", "Email", "Phone",
                "Date of Birth", "Course Code", "Year", "Section"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Create data row with invalid course
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("2024001");
        row1.createCell(1).setCellValue("John");
        row1.createCell(2).setCellValue("Doe");
        row1.createCell(3).setCellValue("john.doe@example.com");
        row1.createCell(4).setCellValue("9876543210");
        row1.createCell(5).setCellValue("2005-05-15");
        row1.createCell(6).setCellValue("INVALID");
        row1.createCell(7).setCellValue(1);
        row1.createCell(8).setCellValue("A");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );
    }

    private MockMultipartFile createExcelWithInvalidEmail() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Roll Number", "First Name", "Last Name", "Email", "Phone",
                "Date of Birth", "Course Code", "Year", "Section"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Create data row with invalid email
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("2024001");
        row1.createCell(1).setCellValue("John");
        row1.createCell(2).setCellValue("Doe");
        row1.createCell(3).setCellValue("invalid-email");
        row1.createCell(4).setCellValue("9876543210");
        row1.createCell(5).setCellValue("2005-05-15");
        row1.createCell(6).setCellValue("BTECH-CSE");
        row1.createCell(7).setCellValue(1);
        row1.createCell(8).setCellValue("A");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );
    }

    private MockMultipartFile createExcelWithInvalidYear() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Roll Number", "First Name", "Last Name", "Email", "Phone",
                "Date of Birth", "Course Code", "Year", "Section"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Create data row with invalid year
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("2024001");
        row1.createCell(1).setCellValue("John");
        row1.createCell(2).setCellValue("Doe");
        row1.createCell(3).setCellValue("john.doe@example.com");
        row1.createCell(4).setCellValue("9876543210");
        row1.createCell(5).setCellValue("2005-05-15");
        row1.createCell(6).setCellValue("BTECH-CSE");
        row1.createCell(7).setCellValue(5);
        row1.createCell(8).setCellValue("A");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );
    }

    private MockMultipartFile createExcelWithMissingFields() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Roll Number", "First Name", "Last Name", "Email", "Phone",
                "Date of Birth", "Course Code", "Year", "Section"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Create data row with missing required fields
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("2024001");
        row1.createCell(1).setCellValue("");  // Missing first name
        row1.createCell(2).setCellValue("Doe");
        row1.createCell(3).setCellValue("john.doe@example.com");
        row1.createCell(4).setCellValue("9876543210");
        row1.createCell(5).setCellValue("2005-05-15");
        row1.createCell(6).setCellValue("BTECH-CSE");
        row1.createCell(7).setCellValue(1);
        row1.createCell(8).setCellValue("A");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );
    }
}
