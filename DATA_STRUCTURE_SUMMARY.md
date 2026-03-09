# Data Structure Summary

Visual overview of the complete database structure after seeding.

## 🏗️ Database Architecture

```
student_tracker (Database)
│
├── courses (3 documents)
│   ├── CSE
│   ├── CSE-AIML
│   └── CSE-DS
│
├── subjects (106 documents)
│   ├── CSE (33 subjects)
│   │   ├── Semester 1 (5)
│   │   ├── Semester 2 (6)
│   │   ├── Semester 3 (5)
│   │   ├── Semester 4 (5)
│   │   ├── Semester 5 (4)
│   │   ├── Semester 6 (4)
│   │   ├── Semester 7 (2)
│   │   └── Semester 8 (2)
│   │
│   ├── CSE-AIML (37 subjects)
│   │   ├── Semester 1 (6)
│   │   ├── Semester 2 (5)
│   │   ├── Semester 3 (5)
│   │   ├── Semester 4 (5)
│   │   ├── Semester 5 (5)
│   │   ├── Semester 6 (4)
│   │   ├── Semester 7 (3)
│   │   └── Semester 8 (4)
│   │
│   └── CSE-DS (36 subjects)
│       ├── Semester 1 (6)
│       ├── Semester 2 (5)
│       ├── Semester 3 (5)
│       ├── Semester 4 (5)
│       ├── Semester 5 (4)
│       ├── Semester 6 (4)
│       ├── Semester 7 (3)
│       └── Semester 8 (4)
│
├── users (29 documents)
│   ├── ADMIN (1)
│   │   └── admin
│   │
│   ├── FACULTY (8)
│   │   ├── rajesh.kumar
│   │   ├── priya.sharma
│   │   ├── amit.patel
│   │   ├── sneha.reddy
│   │   ├── vikram.singh
│   │   ├── anita.desai
│   │   ├── karthik.iyer
│   │   └── meera.nair
│   │
│   └── STUDENT (20)
│       ├── CSE (8)
│       │   ├── Year 1 (5): cse21a001-005
│       │   └── Year 2 (3): cse20a001-003
│       │
│       ├── CSE-AIML (6)
│       │   ├── Year 1 (4): aiml21a001-004
│       │   └── Year 2 (2): aiml20a001-002
│       │
│       └── CSE-DS (6)
│           ├── Year 1 (4): ds21a001-004
│           └── Year 2 (2): ds20a001-002
│
├── students (20 documents)
│   └── Linked to users via userId
│
├── faculty (8 documents)
│   └── Linked to users via userId
│
├── classAllocations (~18 documents)
│   ├── CSE Semester 1 (3 allocations)
│   ├── CSE Semester 3 (3 allocations)
│   ├── AIML Semester 1 (3 allocations)
│   ├── AIML Semester 3 (3 allocations)
│   ├── DS Semester 1 (3 allocations)
│   └── DS Semester 3 (3 allocations)
│
└── attendance (~5,400 documents)
    └── 15 sessions × ~18 allocations × ~20 students
```

## 📊 Data Relationships

```
Course
  ↓ (has many)
Subject
  ↓ (assigned to)
ClassAllocation
  ↓ (links)
Faculty ←→ Student
  ↓ (tracks)
Attendance
```

## 🔗 Entity Relationships

### User → Student/Faculty
```
User (Authentication)
  ├── userId → Student (Profile)
  └── userId → Faculty (Profile)
```

### Course → Subject → Allocation
```
Course
  └── courseId → Subject
                   └── subjectId → ClassAllocation
                                     ├── facultyId → Faculty
                                     ├── courseId → Course
                                     └── (year, section, semester)
```

### Attendance Tracking
```
ClassAllocation
  ├── facultyId ──┐
  ├── subjectId ──┼──→ Attendance Record
  └── students ───┘      ├── studentId
                         ├── date
                         ├── status (PRESENT/ABSENT/LATE)
                         └── remarks
```

## 📈 Data Distribution

### Students by Course
```
CSE:      ████████ (8 students - 40%)
CSE-AIML: ██████ (6 students - 30%)
CSE-DS:   ██████ (6 students - 30%)
```

### Students by Year
```
Year 1: █████████████ (13 students - 65%)
Year 2: ███████ (7 students - 35%)
```

### Subjects by Course
```
CSE:      ████████████████ (33 subjects - 31%)
CSE-AIML: ██████████████████ (37 subjects - 35%)
CSE-DS:   █████████████████ (36 subjects - 34%)
```

### Attendance Status Distribution
```
PRESENT: ████████████████ (80%)
ABSENT:  ███ (15%)
LATE:    █ (5%)
```

## 🎯 Key Metrics

| Metric | Count |
|--------|-------|
| Total Users | 29 |
| Total Courses | 3 |
| Total Subjects | 106 |
| Total Students | 20 |
| Total Faculty | 8 |
| Class Allocations | ~18 |
| Attendance Records | ~5,400 |
| Attendance Sessions | 15 per allocation |
| Sections | 1 (Section A) |
| Semesters Covered | 2 (Sem 1 & 3) |
| Academic Year | 2023-2024 |

## 📅 Semester Coverage

### Semester 1 (Year 1)
- **CSE**: 5 students, 3 subjects allocated
- **CSE-AIML**: 4 students, 3 subjects allocated
- **CSE-DS**: 4 students, 3 subjects allocated

### Semester 3 (Year 2)
- **CSE**: 3 students, 3 subjects allocated
- **CSE-AIML**: 2 students, 3 subjects allocated
- **CSE-DS**: 2 students, 3 subjects allocated

## 🔢 Sample Data Patterns

### Roll Number Format
```
[COURSE][YEAR][SECTION][NUMBER]
  CSE    21     A        001

Examples:
- CSE21A001  (CSE, Year 2021, Section A, Student 1)
- AIML21A001 (AIML, Year 2021, Section A, Student 1)
- DS21A001   (DS, Year 2021, Section A, Student 1)
```

### Subject Code Format
```
[COURSE][SEMESTER][NUMBER]
  CSE     1        01

Examples:
- CSE101  (CSE, Semester 1, Subject 1)
- AIML501 (AIML, Semester 5, Subject 1)
- DS801   (DS, Semester 8, Subject 1)
```

### Email Format
```
Admin: admin.college@gmail.com
Faculty: [firstname.lastname.dept]@gmail.com
Student: [rollnumber]@gmail.com

Examples:
- rajesh.kumar.cse@gmail.com
- cse21a001@gmail.com
```

## 🗂️ Collection Schemas

### User
```javascript
{
  _id: ObjectId,
  username: String,
  email: String,
  password: String (hashed),
  role: Enum ['ADMIN', 'FACULTY', 'STUDENT'],
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

### Student
```javascript
{
  _id: ObjectId,
  userId: String,
  rollNumber: String,
  name: String,
  email: String,
  courseId: String,
  year: Number (1-4),
  section: String,
  semester: Number (1-8),
  phone: String,
  dateOfBirth: Date,
  admissionDate: Date,
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

### Faculty
```javascript
{
  _id: ObjectId,
  userId: String,
  name: String,
  email: String,
  department: String,
  specialization: String,
  phone: String,
  joiningDate: Date,
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

### ClassAllocation
```javascript
{
  _id: ObjectId,
  facultyId: String,
  subjectId: String,
  courseId: String,
  year: Number,
  section: String,
  semester: Number,
  academicYear: String,
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

### Attendance
```javascript
{
  _id: ObjectId,
  studentId: String,
  subjectId: String,
  facultyId: String,
  date: Date,
  status: Enum ['PRESENT', 'ABSENT', 'LATE'],
  remarks: String,
  markedBy: String,
  createdAt: Date,
  updatedAt: Date
}
```

## 🎨 Data Visualization

### User Role Distribution
```
     ADMIN (3%)
       ●
       
    FACULTY (28%)
    ● ● ● ● ● ● ● ●
    
    STUDENT (69%)
    ● ● ● ● ● ● ● ● ● ● 
    ● ● ● ● ● ● ● ● ● ●
```

### Course Enrollment
```
CSE:      [████████] 8 students
CSE-AIML: [██████] 6 students
CSE-DS:   [██████] 6 students
```

### Faculty Distribution
```
CSE:      [████] 4 faculty
CSE-AIML: [██] 2 faculty
CSE-DS:   [██] 2 faculty
```

## 🔍 Query Examples

### Get all students in CSE Year 1
```javascript
db.students.find({
  courseId: "CSE_COURSE_ID",
  year: 1
})
```

### Get faculty teaching in Semester 1
```javascript
db.classAllocations.aggregate([
  { $match: { semester: 1 } },
  { $lookup: {
      from: "faculty",
      localField: "facultyId",
      foreignField: "_id",
      as: "facultyDetails"
  }}
])
```

### Get attendance percentage for a student
```javascript
db.attendance.aggregate([
  { $match: { studentId: "STUDENT_ID" } },
  { $group: {
      _id: "$status",
      count: { $sum: 1 }
  }}
])
```

### Get subjects for a course and semester
```javascript
db.subjects.find({
  courseId: "COURSE_ID",
  semester: 1
})
```

---

**This structure provides a complete, realistic dataset for testing and development! 🎉**
