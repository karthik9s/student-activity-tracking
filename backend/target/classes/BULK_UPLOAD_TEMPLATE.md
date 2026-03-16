# Student Bulk Upload Template

## Excel File Format

The Excel file must have the following columns in order:

| Column | Name | Type | Required | Validation |
|--------|------|------|----------|------------|
| A | Roll Number | Text | Yes | Must be unique |
| B | First Name | Text | Yes | 2-50 characters |
| C | Last Name | Text | Yes | 2-50 characters |
| D | Email | Text | Yes | Valid email format, must be unique |
| E | Phone | Text | No | 10 digits |
| F | Date of Birth | Date | No | Must be in the past (format: YYYY-MM-DD or Excel date) |
| G | Course Code | Text | Yes | Must match existing course code |
| H | Year | Number | Yes | 1-4 |
| I | Section | Text | Yes | Single uppercase letter (A-Z) |

## Example Data

```
Roll Number | First Name | Last Name | Email | Phone | Date of Birth | Course Code | Year | Section
2024001 | John | Doe | john.doe@example.com | 9876543210 | 2005-05-15 | BTECH-CSE | 1 | A
2024002 | Jane | Smith | jane.smith@example.com | 9876543211 | 2005-06-20 | BTECH-CSE | 1 | A
2024003 | Bob | Johnson | bob.j@example.com | 9876543212 | 2005-07-10 | BTECH-ECE | 1 | B
```

## File Requirements

- File format: .xlsx or .xls
- Maximum file size: 10MB
- Maximum rows: 500 students per file
- First row must contain headers (exact names as shown above)
- Empty rows are ignored

## Validation Rules

1. **Roll Number**: Must be unique in both the file and the database
2. **Email**: Must be unique in both the file and the database
3. **Course Code**: Must exist in the system
4. **Phone**: If provided, must be exactly 10 digits
5. **Date of Birth**: If provided, must be a past date
6. **Year**: Must be between 1 and 4
7. **Section**: Must be a single uppercase letter (A-Z)

## Error Handling

- If any validation errors are found, NO students will be imported
- A detailed error report will be returned showing:
  - Row number
  - Roll number (if available)
  - List of validation errors for that row
- Fix all errors and re-upload the file

## API Endpoint

```
POST /api/v1/admin/students/bulk-upload
Content-Type: multipart/form-data
Authorization: Bearer <admin-token>

Form Data:
- file: <excel-file>
```

## Response Format

```json
{
  "totalRows": 3,
  "successCount": 3,
  "errorCount": 0,
  "errors": []
}
```

Or with errors:

```json
{
  "totalRows": 3,
  "successCount": 0,
  "errorCount": 2,
  "errors": [
    {
      "rowNumber": 2,
      "rollNumber": "2024001",
      "errors": [
        "Roll number already exists in system",
        "Email already exists in system"
      ]
    },
    {
      "rowNumber": 3,
      "rollNumber": "2024002",
      "errors": [
        "Invalid course code: INVALID"
      ]
    }
  ]
}
```
