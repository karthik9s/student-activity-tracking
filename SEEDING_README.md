# Database Seeding Guide

Complete guide for populating the Student Activity Tracking System with demo data.

## 📋 Overview

This project includes comprehensive seeding scripts to populate your MongoDB database with:
- 3 Courses (CSE, CSE-AIML, CSE-DS)
- 106 Subjects across 8 semesters
- 1 Admin user
- 8 Faculty members
- 20 Students
- ~18 Class allocations
- ~5,400 Attendance records

## 🚀 Quick Start

### 1. Install Dependencies
```bash
npm install
```

This installs:
- `mongodb` - MongoDB driver
- `bcryptjs` - Password hashing

### 2. Configure MongoDB Connection

Edit the MongoDB URI in your seeding script(s):

```javascript
const MONGODB_URI = 'mongodb+srv://username:password@cluster.mongodb.net/student_tracker?retryWrites=true&w=majority';
```

### 3. Run Seeding Scripts

#### Option A: Seed Everything at Once (Recommended)
```bash
npm run seed:all
```

This runs both scripts in sequence:
1. Creates courses and subjects
2. Creates users, allocations, and attendance

#### Option B: Seed Step by Step
```bash
# Step 1: Seed courses and subjects
npm run seed:subjects

# Step 2: Seed demo data (users, allocations, attendance)
npm run seed:demo
```

## 📁 Seeding Scripts

### 1. `seed-subjects-data.js`
Creates courses and subjects.

**What it creates:**
- 3 courses
- 106 subjects with proper semester assignments

**Run:**
```bash
node seed-subjects-data.js
# or
npm run seed:subjects
```

### 2. `seed-complete-demo-data.js`
Creates users, allocations, and attendance.

**What it creates:**
- 1 admin user
- 8 faculty members
- 20 students
- Class allocations
- Sample attendance records

**Run:**
```bash
node seed-complete-demo-data.js
# or
npm run seed:demo
```

**Note:** Must run after `seed-subjects-data.js`

### 3. `seed-all.js` (Coming Soon)
Combined script that runs everything in one go.

## 📊 What Gets Created

### Courses (3)
| Code | Name | Duration |
|------|------|----------|
| CSE | Computer Science and Engineering | 4 years |
| CSE-AIML | CSE - AI & Machine Learning | 4 years |
| CSE-DS | CSE - Data Science | 4 years |

### Subjects (106)
- CSE: 33 subjects
- CSE-AIML: 37 subjects
- CSE-DS: 36 subjects

All subjects properly mapped to semesters 1-8.

### Users (29)

#### Admin (1)
- Username: `admin`
- Password: `admin123`

#### Faculty (8)
All use password: `faculty123`
- rajesh.kumar
- priya.sharma
- amit.patel
- sneha.reddy
- vikram.singh
- anita.desai
- karthik.iyer
- meera.nair

#### Students (20)
All use password: `student123`

**CSE (8 students)**
- Year 1: cse21a001 to cse21a005
- Year 2: cse20a001 to cse20a003

**CSE-AIML (6 students)**
- Year 1: aiml21a001 to aiml21a004
- Year 2: aiml20a001 to aiml20a002

**CSE-DS (6 students)**
- Year 1: ds21a001 to ds21a004
- Year 2: ds20a001 to ds20a002

### Class Allocations (~18)
- Faculty assigned to subjects
- Covers Semester 1 and Semester 3
- Section A for all courses
- Academic Year: 2023-2024

### Attendance Records (~5,400)
- 15 sessions per allocation
- Realistic patterns (80% present, 15% absent, 5% late)
- Dates spread over past 30 days

## 🔐 Demo Credentials

See `DEMO_CREDENTIALS.md` for complete list of all usernames and passwords.

**Quick Access:**
- Admin: `admin` / `admin123`
- Faculty: `rajesh.kumar` / `faculty123`
- Student: `cse21a001` / `student123`

## ✅ Verification

After seeding, verify in MongoDB:

```javascript
// Connect to your database
use student_tracker

// Check counts
db.courses.countDocuments()           // Should be 3
db.subjects.countDocuments()          // Should be 106
db.users.countDocuments()             // Should be 29
db.students.countDocuments()          // Should be 20
db.faculty.countDocuments()           // Should be 8
db.classAllocations.countDocuments()  // Should be ~18
db.attendance.countDocuments()        // Should be ~5,400

// Check by role
db.users.countDocuments({role: 'ADMIN'})    // 1
db.users.countDocuments({role: 'FACULTY'})  // 8
db.users.countDocuments({role: 'STUDENT'})  // 20

// Check subjects by course
db.subjects.countDocuments({courseId: ObjectId("CSE_ID")})      // 33
db.subjects.countDocuments({courseId: ObjectId("AIML_ID")})     // 37
db.subjects.countDocuments({courseId: ObjectId("DS_ID")})       // 36
```

## 🧪 Testing After Seeding

### 1. Start Backend
```bash
cd backend
mvn spring-boot:run
```

### 2. Start Frontend
```bash
cd frontend
npm run dev
```

### 3. Test Login
Try logging in with different roles:
- Admin: Full system access
- Faculty: View assigned subjects, mark attendance
- Student: View attendance and performance

### 4. Test Features
- ✅ Attendance marking (Faculty)
- ✅ Attendance viewing (Student)
- ✅ Class allocation management (Admin)
- ✅ Report generation (Faculty/Admin)
- ✅ User management (Admin)

## 🔄 Re-seeding

To clear and re-seed the database:

```bash
# This will delete all existing data and create fresh demo data
npm run seed:all
```

The scripts automatically clear existing data before inserting new records.

## 🛠️ Customization

### Add More Students

Edit `seed-complete-demo-data.js` and add to `studentData` array:

```javascript
{ 
  name: 'New Student', 
  rollNo: 'CSE21A006', 
  course: 'CSE', 
  year: 1, 
  section: 'A', 
  semester: 1 
}
```

### Add More Faculty

Edit `facultyData` array:

```javascript
{ 
  name: 'Dr. New Faculty', 
  email: 'new.faculty@college.edu', 
  dept: 'CSE', 
  specialization: 'AI' 
}
```

### Add More Sections

Change section from 'A' to 'B' in allocations:

```javascript
section: 'B'
```

### Modify Attendance Patterns

Edit `getRandomAttendanceStatus()` function to change distribution:

```javascript
function getRandomAttendanceStatus() {
  const statuses = ['PRESENT', 'PRESENT', 'ABSENT'];  // 66% present, 33% absent
  return statuses[Math.floor(Math.random() * statuses.length)];
}
```

## 📚 Documentation Files

- `SUBJECTS_SEEDING_GUIDE.md` - Detailed guide for subjects seeding
- `COMPLETE_DEMO_DATA_GUIDE.md` - Complete demo data guide
- `DEMO_CREDENTIALS.md` - Quick reference for all credentials
- `SUBJECTS_DATA_VERIFICATION.md` - Complete subject breakdown

## ⚠️ Important Notes

1. **Run Order**: Always run `seed-subjects-data.js` before `seed-complete-demo-data.js`
2. **MongoDB URI**: Update connection string in all scripts
3. **Network Access**: Ensure your IP is whitelisted in MongoDB Atlas
4. **Dependencies**: Run `npm install` before seeding
5. **Data Clearing**: Scripts automatically clear existing data
6. **Passwords**: All passwords are hashed using bcrypt
7. **Demo Only**: These credentials are for demo purposes only

## 🐛 Troubleshooting

### Error: "Cannot find module 'mongodb'"
```bash
npm install mongodb bcryptjs
```

### Error: "No courses found"
Run subjects seeding first:
```bash
npm run seed:subjects
```

### Connection Timeout
- Check MongoDB URI
- Verify network access in MongoDB Atlas
- Ensure IP is whitelisted

### Duplicate Key Error
Clear collections manually:
```javascript
db.users.deleteMany({})
db.students.deleteMany({})
db.faculty.deleteMany({})
db.classAllocations.deleteMany({})
db.attendance.deleteMany({})
```

Then re-run seeding scripts.

## 📞 Support

For issues or questions:
1. Check MongoDB connection
2. Verify all dependencies installed
3. Review console output for specific errors
4. Check MongoDB Atlas network settings

## 🎯 Next Steps

After successful seeding:
1. ✅ Start backend server
2. ✅ Start frontend server
3. ✅ Login with demo credentials
4. ✅ Test all features
5. ✅ Customize as needed

---

**Happy Seeding! 🌱**
