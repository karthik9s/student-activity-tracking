package com.college.activitytracker.service;

import com.college.activitytracker.dto.BulkUploadResultDTO;
import com.college.activitytracker.dto.StudentDTO;
import com.college.activitytracker.exception.ResourceNotFoundException;
import com.college.activitytracker.model.Course;
import com.college.activitytracker.repository.CourseRepository;
import com.college.activitytracker.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ExcelUploadService {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    
    public ExcelUploadService(StudentService studentService, StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }
    
    private static final int MAX_ROWS = 500;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern SECTION_PATTERN = Pattern.compile("^[A-Z]$");
    
    // Expected column headers
    private static final String[] EXPECTED_HEADERS = {
        "Roll Number", "First Name", "Last Name", "Email", "Phone", 
        "Date of Birth", "Course Code", "Year", "Section"
    };

    @Transactional
    public BulkUploadResultDTO uploadStudentsFromExcel(MultipartFile file) throws IOException {
        System.out.println("Starting bulk upload from Excel file: " + file.getOriginalFilename());
        
        // Validate file
        validateFile(file);
        
        // Parse Excel file
        List<StudentDTO> students = parseExcelFile(file);
        
        // Validate all students
        BulkUploadResultDTO result = validateStudents(students);
        
        // If there are validation errors, return without importing
        if (result.getErrorCount() > 0) {
            System.out.println("Validation failed with " + result.getErrorCount() + " errors");
            return result;
        }
        
        // Import students
        importStudents(students, result);
        
        System.out.println("Bulk upload completed: " + result.getSuccessCount() + " success, " + result.getErrorCount() + " errors");
        
        return result;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("File must be an Excel file (.xlsx or .xls)");
        }
        
        // Check file size (10MB limit)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }

    private List<StudentDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<StudentDTO> students = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Validate headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file is empty");
            }
            
            validateHeaders(headerRow);
            
            // Parse data rows
            int rowCount = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                
                rowCount++;
                if (rowCount > MAX_ROWS) {
                    throw new IllegalArgumentException(
                        "File contains more than " + MAX_ROWS + " students. Maximum allowed is " + MAX_ROWS);
                }
                
                StudentDTO student = parseRow(row, i + 1);
                students.add(student);
            }
            
            if (students.isEmpty()) {
                throw new IllegalArgumentException("No student data found in Excel file");
            }
        }
        
        return students;
    }

    private void validateHeaders(Row headerRow) {
        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || !EXPECTED_HEADERS[i].equalsIgnoreCase(getCellValue(cell))) {
                throw new IllegalArgumentException(
                    "Invalid header at column " + (i + 1) + ". Expected: " + EXPECTED_HEADERS[i]);
            }
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private StudentDTO parseRow(Row row, int rowNumber) {
        StudentDTO student = new StudentDTO();
        
        try {
            student.setRollNumber(getCellValue(row.getCell(0)).trim());
            student.setFirstName(getCellValue(row.getCell(1)).trim());
            student.setLastName(getCellValue(row.getCell(2)).trim());
            student.setEmail(getCellValue(row.getCell(3)).trim());
            student.setPhone(getCellValue(row.getCell(4)).trim());
            
            // Parse date of birth
            Cell dobCell = row.getCell(5);
            if (dobCell != null) {
                if (dobCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
                    Date date = dobCell.getDateCellValue();
                    student.setDateOfBirth(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                } else {
                    String dobStr = getCellValue(dobCell).trim();
                    if (!dobStr.isEmpty()) {
                        student.setDateOfBirth(LocalDate.parse(dobStr));
                    }
                }
            }
            
            String courseCode = getCellValue(row.getCell(6)).trim();
            // We'll resolve course ID during validation
            student.setCourseId(courseCode);
            
            String yearStr = getCellValue(row.getCell(7)).trim();
            if (!yearStr.isEmpty()) {
                student.setYear(Integer.parseInt(yearStr));
            }
            
            student.setSection(getCellValue(row.getCell(8)).trim().toUpperCase());
            student.setIsActive(true);
            
        } catch (Exception e) {
            System.err.println("Error parsing row " + rowNumber + ": " + e.getMessage());
            throw new IllegalArgumentException("Error parsing row " + rowNumber + ": " + e.getMessage());
        }
        
        return student;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // Format numeric values without decimal points for whole numbers
                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private BulkUploadResultDTO validateStudents(List<StudentDTO> students) {
        BulkUploadResultDTO result = new BulkUploadResultDTO(students.size(), 0, 0, new ArrayList<>());
        
        Set<String> rollNumbers = new HashSet<>();
        Set<String> emails = new HashSet<>();
        
        for (int i = 0; i < students.size(); i++) {
            StudentDTO student = students.get(i);
            List<String> errors = new ArrayList<>();
            int rowNumber = i + 2; // +2 because Excel is 1-indexed and row 1 is header
            
            // Validate roll number
            if (student.getRollNumber() == null || student.getRollNumber().isEmpty()) {
                errors.add("Roll number is required");
            } else {
                // Check for duplicates in file
                if (rollNumbers.contains(student.getRollNumber())) {
                    errors.add("Duplicate roll number in file");
                } else {
                    rollNumbers.add(student.getRollNumber());
                    // Check if exists in database
                    if (studentRepository.existsByRollNumber(student.getRollNumber())) {
                        errors.add("Roll number already exists in system");
                    }
                }
            }
            
            // Validate first name
            if (student.getFirstName() == null || student.getFirstName().isEmpty()) {
                errors.add("First name is required");
            } else if (student.getFirstName().length() < 2 || student.getFirstName().length() > 50) {
                errors.add("First name must be between 2 and 50 characters");
            }
            
            // Validate last name
            if (student.getLastName() == null || student.getLastName().isEmpty()) {
                errors.add("Last name is required");
            } else if (student.getLastName().length() < 2 || student.getLastName().length() > 50) {
                errors.add("Last name must be between 2 and 50 characters");
            }
            
            // Validate email
            if (student.getEmail() == null || student.getEmail().isEmpty()) {
                errors.add("Email is required");
            } else if (!EMAIL_PATTERN.matcher(student.getEmail()).matches()) {
                errors.add("Invalid email format");
            } else {
                // Check for duplicates in file
                if (emails.contains(student.getEmail())) {
                    errors.add("Duplicate email in file");
                } else {
                    emails.add(student.getEmail());
                    // Check if exists in database
                    if (studentRepository.existsByEmail(student.getEmail())) {
                        errors.add("Email already exists in system");
                    }
                }
            }
            
            // Validate phone
            if (student.getPhone() != null && !student.getPhone().isEmpty()) {
                if (!PHONE_PATTERN.matcher(student.getPhone()).matches()) {
                    errors.add("Phone number must be 10 digits");
                }
            }
            
            // Validate date of birth
            if (student.getDateOfBirth() != null) {
                if (student.getDateOfBirth().isAfter(LocalDate.now())) {
                    errors.add("Date of birth must be in the past");
                }
            }
            
            // Validate and resolve course
            if (student.getCourseId() == null || student.getCourseId().isEmpty()) {
                errors.add("Course code is required");
            } else {
                Optional<Course> course = courseRepository.findByCode(student.getCourseId());
                if (course.isEmpty()) {
                    errors.add("Invalid course code: " + student.getCourseId());
                } else {
                    // Replace course code with course ID
                    student.setCourseId(course.get().getId());
                }
            }
            
            // Validate year
            if (student.getYear() == null) {
                errors.add("Year is required");
            } else if (student.getYear() < 1 || student.getYear() > 4) {
                errors.add("Year must be between 1 and 4");
            }
            
            // Validate section
            if (student.getSection() == null || student.getSection().isEmpty()) {
                errors.add("Section is required");
            } else if (!SECTION_PATTERN.matcher(student.getSection()).matches()) {
                errors.add("Section must be a single uppercase letter (A-Z)");
            }
            
            if (!errors.isEmpty()) {
                BulkUploadResultDTO.RowError rowError = new BulkUploadResultDTO.RowError(rowNumber, student.getRollNumber(), errors);
                result.getErrors().add(rowError);
                result.setErrorCount(result.getErrorCount() + 1);
            }
        }
        
        return result;
    }

    private void importStudents(List<StudentDTO> students, BulkUploadResultDTO result) {
        for (int i = 0; i < students.size(); i++) {
            StudentDTO student = students.get(i);
            int rowNumber = i + 2;
            
            try {
                studentService.createStudent(student);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                System.err.println("Error importing student at row " + rowNumber + ": " + e.getMessage());
                BulkUploadResultDTO.RowError rowError = new BulkUploadResultDTO.RowError(rowNumber, student.getRollNumber(), Collections.singletonList("Import failed: " + e.getMessage()));
                result.getErrors().add(rowError);
                result.setErrorCount(result.getErrorCount() + 1);
            }
        }
    }
}
