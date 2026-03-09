# Bugfix Requirements Document

## Introduction

The Mark Attendance page fails to display students when a class is selected, showing "Failed to fetch students" error and "No students found for this class" message. The root cause is that the student fetching logic filters only by `courseId` without considering the `year` and `section` from the class allocation. This results in either fetching students from all years/sections of a course (which may not match the allocation's year/section) or returning no students when the filter criteria don't align with the class allocation details.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a faculty selects a class allocation (e.g., "Computer Science and Engineering - Engineering Chemistry - Year 1 - Section A") on the Mark Attendance page THEN the system fetches students filtered only by `courseId` without considering `year` and `section`

1.2 WHEN the student fetch query uses only `courseId` as a filter parameter THEN the system returns students from all years and sections of that course, not matching the specific class allocation

1.3 WHEN the returned students don't match the allocation's year and section THEN the frontend displays "No students found for this class" even though students exist for that specific year and section

### Expected Behavior (Correct)

2.1 WHEN a faculty selects a class allocation on the Mark Attendance page THEN the system SHALL fetch students filtered by `courseId`, `year`, AND `section` from the selected allocation

2.2 WHEN the student fetch query includes `courseId`, `year`, and `section` as filter parameters THEN the system SHALL return only students enrolled in that specific course, year, and section combination

2.3 WHEN students exist for the selected class allocation's course, year, and section THEN the system SHALL display those students in the attendance marking table

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a faculty views their class allocations in the dropdown THEN the system SHALL CONTINUE TO display all allocated classes correctly

3.2 WHEN a faculty marks attendance for students in a class THEN the system SHALL CONTINUE TO save attendance records with correct student, subject, faculty, course, year, and section information

3.3 WHEN a faculty selects a date for attendance marking THEN the system SHALL CONTINUE TO check for existing attendance records and display them if present

3.4 WHEN a faculty uses bulk actions (Mark All Present/Absent) THEN the system SHALL CONTINUE TO update attendance status for all displayed students

3.5 WHEN the student list is empty after applying correct filters THEN the system SHALL CONTINUE TO display "No students found for this class" message
