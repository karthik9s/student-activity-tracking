# Complete Demo Data Seeding Guide

This guide explains how to populate your database with a complete demo dataset including users, students, faculty, class allocations, and attendance records.

## What Gets Created

### 1. Admin User (1)
- Username: `admin`
- Password: `admin123`
- Role: ADMIN
- Full system access

### 2. Faculty Members (8)
All faculty use password: `faculty123`

| Name | Username | Email | Department | Specialization |
|------|----------|-------|------------|----------------|
| Dr. Rajesh Kumar | rajesh.kumar.cse | rajesh.kumar.cse@gmail.com | CSE | Data Structures & Algorithms |
| Prof. Priya Sharma | priya.sharma.cse | priya.sharma.cse@gmail.com | CSE | Database Management |
| Dr. Amit Patel | amit.patel.cse | amit.patel.cse@gmail.com | CSE | Web Technologies |
| Prof. Sneha Reddy | sneha.reddy.aiml | sneha.reddy.aiml@gmail.com | CSE-AIML | Machine Learning |
| Dr. Vikram Singh | vikram.singh.aiml | vikram.singh.aiml@gmail.com | CSE-AIML | Deep Learning |
| Prof. Anita Desai | anita.desai.ds | anita.desai.ds@gmail.com | CSE-DS | Data Analytics |
| Dr. Karthik Iyer | karthik.iyer.ds | karthik.iyer.ds@gmail.com | CSE-DS | Big Data |
| Prof. Meera Nair | meera.nair.cse | meera.nair.cse@gmail.com | CSE | Operating Systems |

### 3. Students (20)
All students use password: `student123`

#### CSE Students (8)
**Year 1, Semester 1, Section A (5 students)**
- cse21a001 - Aarav Sharma
- cse21a002 - Diya Patel
- cse21a003 - Arjun Reddy
- cse21a004 - Ananya Kumar
- cse21a005 - Rohan Gupta

**Year 2, Semester 3, Section A (3 students)**
- cse20a001 - Ishaan Verma
- cse20a002 - Kavya Singh
- cse20a003 - Aditya Joshi

#### CSE-AIML Students (6)
**Year 1, Semester 1, Section A (4 students)**
- aiml21a001 - Sai Krishna
- aiml21a002 - Priya Menon
- aiml21a003 - Rahul Nair
- aiml21a004 - Sneha Iyer

**Year 2, Semester 3, Section A (2 students)**
- aiml20a001 - Kiran Kumar
- aiml20a002 - Divya Rao

#### CSE-DS Students (6)
**Year 1, Semester 1, Section A (4 students)**
- ds21a001 - Varun Reddy
- ds21a002 - Lakshmi Devi
- ds21a003 - Harish Babu
- ds21a004 - Pooja Reddy

**Year 2, Semester 3, Section A (2 students)**
- ds20a001 - Naveen Kumar
- ds20a002 - Swathi Sharma

### 4. Class Allocations
- Faculty assigned to subjects for Semester 1 and Semester 3
- Each course (CSE, CSE-AIML, CSE-DS) has 3 subjects allocated per semester
- Total: ~18 class allocations
- Academic Year: 2023-2024

### 5. Attendance Records
- 15 attendance sessions per allocation
- Realistic attendance patterns (mostly present, some absent/late)
- Dates spread over the past 30 days
- Total: ~5,400 attendance records

## Prerequisites

1. Node.js installed
2. MongoDB connection string
3. Courses and subjects already seeded (run `seed-subjects-data.js` first)

## Installation

```bash
npm install mongodb bcryptjs
```

## Step-by-Step Setup

### Step 1: Seed Courses and Subjects
```bash
# First, update MongoDB URI in seed-subjects-data.js
node seed-subjects-data.js
```

This creates:
- 3 courses (CSE, CSE-AIML, CSE-DS)
- 106 subjects across 8 semesters

### Step 2: Seed Complete Demo Data
```bash
# Update MongoDB URI in seed-complete-demo-data.js
node seed-complete-demo-data.js
```

This creates:
- 1 admin user
- 8 faculty members
- 20 students
- ~18 class allocations
- ~5,400 attendance records

## Configuration

Open `seed-complete-demo-data.js` and update line 8:

```javascript
const MONGODB_URI = 'mongodb+srv://your-username:your-password@your-cluster.mongodb.net/student_tracker?retryWrites=true&w=majority';
```

## What You Can Test After Seeding

### As Admin (admin / admin123)
1. View all students, faculty, courses, subjects
2. Create new class allocations
3. View attendance reports
4. Manage users
5. View audit logs

### As Faculty (e.g., rajesh.kumar / faculty123)
1. View assigned subjects
2. View students in assigned classes
3. Mark attendance for students
4. Enter performance/grades
5. Generate reports for assigned subjects
6. View attendance history

### As Student (e.g., cse21a001 / student123)
1. View enrolled subjects for current semester
2. View attendance records
3. View performance/grades
4. See attendance percentage
5. View notifications

## Sample Workflows to Test

### 1. Faculty Marks Attendance
```
Login as: rajesh.kumar.cse / faculty123
Navigate to: Attendance Marking
Select: Subject, Date, Section
Mark: Present/Absent/Late for each student
Submit
```

### 2. Student Views Attendance
```
Login as: cse21a001 / student123
Navigate to: My Attendance
View: Subject-wise attendance
See: Attendance percentage, dates, status
```

### 3. Admin Creates New Allocation
```
Login as: admin / admin123
Navigate to: Class Allocation Management
Create: New allocation
Select: Faculty, Subject, Course, Year, Section, Semester
Save
```

### 4. Faculty Enters Performance
```
Login as: priya.sharma.cse / faculty123
Navigate to: Performance Entry
Select: Subject, Assessment Type
Enter: Marks for each student
Submit
```

### 5. Admin Views Reports
```
Login as: admin / admin123
Navigate to: Reports
Select: Attendance Report / Performance Report
Filter: By course, year, section, date range
Generate: PDF/Excel report
```

## Database Verification

After running the script, verify in MongoDB:

```javascript
// Check users
db.users.countDocuments()  // Should return 29 (1 admin + 8 faculty + 20 students)

// Check by role
db.users.countDocuments({role: 'ADMIN'})    // 1
db.users.countDocuments({role: 'FACULTY'})  // 8
db.users.countDocuments({role: 'STUDENT'})  // 20

// Check students
db.students.countDocuments()  // 20

// Check faculty
db.faculty.countDocuments()  // 8

// Check class allocations
db.classAllocations.countDocuments()  // ~18

// Check attendance
db.attendance.countDocuments()  // ~5,400

// Check attendance for a specific student
db.attendance.countDocuments({studentId: "STUDENT_ID"})

// Check allocations for a faculty
db.classAllocations.find({facultyId: "FACULTY_ID"})
```

## Data Characteristics

### Attendance Patterns
- **80% Present**: Most students attend regularly
- **15% Absent**: Some absences
- **5% Late**: Occasional late arrivals
- Random distribution over 15 sessions per subject

### Class Structure
- Section A for all courses
- Year 1 (Semester 1) and Year 2 (Semester 3) covered
- 3 subjects allocated per semester per course
- Multiple faculty teaching different subjects

### Realistic Data
- Indian names for students and faculty
- Proper roll number format (COURSE+YEAR+SECTION+NUMBER)
- Valid email addresses
- Phone numbers in Indian format
- Proper date ranges (admission dates, attendance dates)

## Troubleshooting

### Error: "No courses found"
**Solution**: Run `seed-subjects-data.js` first to create courses and subjects.

### Error: "Cannot find module 'bcryptjs'"
**Solution**: Run `npm install bcryptjs`

### Connection Error
**Solution**: 
- Verify MongoDB connection string
- Check network access in MongoDB Atlas
- Ensure IP address is whitelisted

### Duplicate Key Error
**Solution**: The script automatically deletes existing records before inserting. If you still get errors, manually clear collections:
```javascript
db.users.deleteMany({})
db.students.deleteMany({})
db.faculty.deleteMany({})
db.classAllocations.deleteMany({})
db.attendance.deleteMany({})
```

## Extending the Demo Data

### Add More Students
Edit the `studentData` array in the script and add more entries:
```javascript
{ name: 'New Student', rollNo: 'CSE21A006', course: 'CSE', year: 1, section: 'A', semester: 1 }
```

### Add More Faculty
Edit the `facultyData` array:
```javascript
{ name: 'Dr. New Faculty', email: 'new.faculty@college.edu', dept: 'CSE', specialization: 'AI' }
```

### Add More Sections
Duplicate allocations and change section from 'A' to 'B':
```javascript
section: 'B'
```

### Add More Semesters
Change the semester filter in subject queries:
```javascript
semester: { $in: [1, 2, 3, 4] }  // Add more semesters
```

## Next Steps After Seeding

1. **Start Backend Server**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Start Frontend Server**
   ```bash
   cd frontend
   npm run dev
   ```

3. **Login and Test**
   - Admin: admin / admin123
   - Faculty: rajesh.kumar / faculty123
   - Student: cse21a001 / student123

4. **Test Key Features**
   - Attendance marking and viewing
   - Performance entry and viewing
   - Report generation
   - User management
   - Class allocation management

## Security Notes

- All passwords are hashed using bcrypt
- Default passwords are for demo purposes only
- Change passwords in production
- Use environment variables for sensitive data
- Enable proper authentication and authorization

## Performance Considerations

- Script creates ~5,400 attendance records
- May take 30-60 seconds to complete
- Indexes should be created (see mongodb-indexes.js)
- Consider batch operations for large datasets

## Support

If you encounter issues:
1. Check MongoDB connection
2. Verify courses and subjects exist
3. Check console output for specific errors
4. Verify all npm packages are installed
5. Check MongoDB Atlas network access

---

**Happy Testing! 🚀**
