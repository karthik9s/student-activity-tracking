# Subjects Data Seeding Guide

This guide explains how to populate your database with all courses and subjects organized by semester.

## Overview

The system now supports:
- **3 Courses**: CSE, CSE-AIML, CSE-DS
- **8 Semesters** per course (4 years × 2 semesters)
- **Subject-Semester mapping** for each course

## Prerequisites

1. Node.js installed
2. MongoDB connection string
3. `mongodb` npm package

## Installation

```bash
npm install mongodb
```

## Configuration

1. Open `seed-subjects-data.js`
2. Update the MongoDB connection string on line 6:

```javascript
const MONGODB_URI = 'mongodb+srv://your-username:your-password@your-cluster.mongodb.net/student_tracker?retryWrites=true&w=majority';
```

Replace with your actual MongoDB Atlas connection string.

## Running the Script

```bash
node seed-subjects-data.js
```

## What Gets Created

### Courses
1. **CSE** - Computer Science and Engineering
2. **CSE-AIML** - CSE with AI & ML specialization
3. **CSE-DS** - CSE with Data Science specialization

### Subjects by Course

#### CSE (33 subjects)
- Semester 1: 5 subjects
- Semester 2: 6 subjects
- Semester 3: 5 subjects
- Semester 4: 5 subjects
- Semester 5: 4 subjects
- Semester 6: 4 subjects
- Semester 7: 2 subjects
- Semester 8: 2 subjects

#### CSE-AIML (37 subjects)
- Semester 1: 6 subjects
- Semester 2: 5 subjects
- Semester 3: 5 subjects
- Semester 4: 5 subjects
- Semester 5: 5 subjects
- Semester 6: 4 subjects
- Semester 7: 3 subjects
- Semester 8: 4 subjects

#### CSE-DS (36 subjects)
- Semester 1: 6 subjects
- Semester 2: 5 subjects
- Semester 3: 5 subjects
- Semester 4: 5 subjects
- Semester 5: 4 subjects
- Semester 6: 4 subjects
- Semester 7: 3 subjects
- Semester 8: 4 subjects

## Subject Structure

Each subject includes:
- `code`: Unique subject code (e.g., CSE101, AIML501)
- `name`: Subject name
- `semester`: Semester number (1-8)
- `credits`: Credit hours (2-4)
- `type`: THEORY or LAB
- `courseId`: Reference to parent course
- `description`: Auto-generated description
- `isActive`: Active status (true)

## Workflow After Seeding

### 1. Admin Creates Courses (Already Done by Script)
The script automatically creates all three courses.

### 2. Admin Adds Subjects (Already Done by Script)
All subjects are pre-populated with correct semester assignments.

### 3. Admin Assigns Faculty to Subjects
Use the "Assign Subjects" feature in Faculty Management:
- Select faculty member
- Choose course, year, section, and semester
- Select subject from the filtered list (only shows subjects for that semester)
- Set academic year

### 4. Students Enroll
Students are assigned to:
- A specific course (CSE, CSE-AIML, or CSE-DS)
- A year (1-4)
- A section (A, B, C, etc.)

### 5. Faculty Access
Faculty can:
- View their assigned subjects filtered by semester
- Mark attendance for students in their classes
- Enter performance/grades
- Generate reports

## Verification

After running the script, verify in MongoDB:

```javascript
// Check courses
db.courses.find().count()  // Should return 3

// Check subjects
db.subjects.find().count()  // Should return 106 total

// Check subjects by course
db.subjects.find({courseId: "CSE_COURSE_ID"}).count()  // Should return 33
db.subjects.find({courseId: "AIML_COURSE_ID"}).count()  // Should return 37
db.subjects.find({courseId: "DS_COURSE_ID"}).count()  // Should return 36

// Check subjects by semester
db.subjects.find({semester: 1}).count()  // All semester 1 subjects
db.subjects.find({semester: 8}).count()  // All semester 8 subjects
```

## Features Enabled

### Subject Form
- Semester field limited to 1-8
- Subjects automatically linked to courses
- Validation ensures semester is within range

### Class Allocation
- Faculty can be assigned to specific semester subjects
- Allocation considers: Course → Year → Semester → Subject
- Each allocation is unique per faculty-subject-section combination

### Student View
- Students see subjects for their current semester
- Performance and attendance tracked per subject
- Subjects organized by semester

## Troubleshooting

### Connection Error
- Verify MongoDB connection string
- Check network access in MongoDB Atlas
- Ensure IP address is whitelisted

### Duplicate Key Error
- Clear existing data first
- The script automatically clears old data before inserting

### Missing Subjects
- Verify the script completed successfully
- Check console output for any errors
- Manually verify in MongoDB

## Next Steps

1. Run the seeding script
2. Login as admin
3. Navigate to Faculty Management
4. Assign subjects to faculty members
5. Create student accounts with proper course/year/section
6. Faculty can start marking attendance and performance

## Notes

- Each course has a unique curriculum
- Subjects are semester-specific
- The system supports multiple sections per year
- Faculty can teach multiple subjects across different semesters
- Students progress through semesters sequentially
