// Master Seeding Script - Runs all seeding operations in sequence
// This script will:
// 1. Seed courses and subjects
// 2. Seed users (admin, faculty, students)
// 3. Create class allocations
// 4. Mark sample attendance

const { MongoClient, ObjectId } = require('mongodb');
const bcrypt = require('bcryptjs');

// MongoDB connection string - UPDATE THIS
const MONGODB_URI = 'mongodb+srv://your-username:your-password@your-cluster.mongodb.net/student_tracker?retryWrites=true&w=majority';

// Import data structures
const coursesData = [
  {
    code: 'CSE',
    name: 'Computer Science and Engineering',
    description: 'Bachelor of Technology in Computer Science and Engineering',
    duration: 4,
    department: 'CSE'
  },
  {
    code: 'CSE-AIML',
    name: 'Computer Science and Engineering - Artificial Intelligence and Machine Learning',
    description: 'Bachelor of Technology in CSE with specialization in AI & ML',
    duration: 4,
    department: 'CSE-AIML'
  },
  {
    code: 'CSE-DS',
    name: 'Computer Science and Engineering - Data Science',
    description: 'Bachelor of Technology in CSE with specialization in Data Science',
    duration: 4,
    department: 'CSE-DS'
  }
];

// Helper functions
async function hashPassword(password) {
  return await bcrypt.hash(password, 10);
}

function getRandomPastDate(daysAgo = 30) {
  const date = new Date();
  date.setDate(date.getDate() - Math.floor(Math.random() * daysAgo));
  return date;
}

function getRandomAttendanceStatus() {
  const statuses = ['PRESENT', 'PRESENT', 'PRESENT', 'PRESENT', 'ABSENT', 'LATE'];
  return statuses[Math.floor(Math.random() * statuses.length)];
}

async function seedAll() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    console.log('✓ Connected to MongoDB\n');
    
    const db = client.db('student_tracker');
    
    console.log('╔════════════════════════════════════════════════════════╗');
    console.log('║   Student Activity Tracker - Complete Data Seeding    ║');
    console.log('╚════════════════════════════════════════════════════════╝\n');
    
    // Clear all existing data
    console.log('🗑️  Clearing existing data...');
    await db.collection('courses').deleteMany({});
    await db.collection('subjects').deleteMany({});
    await db.collection('users').deleteMany({});
    await db.collection('students').deleteMany({});
    await db.collection('faculty').deleteMany({});
    await db.collection('classAllocations').deleteMany({});
    await db.collection('attendance').deleteMany({});
    console.log('✓ Existing data cleared\n');
    
    // PHASE 1: Courses and Subjects
    console.log('📚 PHASE 1: Seeding Courses and Subjects');
    console.log('─────────────────────────────────────────');
    
    const courseResult = await db.collection('courses').insertMany(coursesData);
    console.log(`✓ Inserted ${courseResult.insertedCount} courses`);
    
    const courses = await db.collection('courses').find({}).toArray();
    const courseMap = {};
    courses.forEach(course => {
      courseMap[course.code] = course._id.toString();
    });
    
    // Load subjects data (abbreviated for brevity - use full data from seed-subjects-data.js)
    const { subjectsData } = require('./seed-subjects-data.js');
    
    let totalSubjects = 0;
    for (const [courseCode, subjects] of Object.entries(subjectsData)) {
      const courseId = courseMap[courseCode];
      const subjectsWithCourseId = subjects.map(subject => ({
        ...subject,
        courseId: courseId,
        description: `${subject.name} for ${courseCode}`,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date()
      }));
      
      const result = await db.collection('subjects').insertMany(subjectsWithCourseId);
      totalSubjects += result.insertedCount;
    }
    console.log(`✓ Inserted ${totalSubjects} subjects\n`);
    
    // PHASE 2: Users
    console.log('👥 PHASE 2: Creating Users');
    console.log('─────────────────────────────────────────');
    
    // Admin
    const adminPassword = await hashPassword('admin123');
    await db.collection('users').insertOne({
      username: 'admin',
      email: 'admin@college.edu',
      password: adminPassword,
      role: 'ADMIN',
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date()
    });
    console.log('✓ Admin user created');
    
    // Faculty
    const facultyData = [
      { name: 'Dr. Rajesh Kumar', email: 'rajesh.kumar@college.edu', dept: 'CSE', specialization: 'Data Structures & Algorithms' },
      { name: 'Prof. Priya Sharma', email: 'priya.sharma@college.edu', dept: 'CSE', specialization: 'Database Management' },
      { name: 'Dr. Amit Patel', email: 'amit.patel@college.edu', dept: 'CSE', specialization: 'Web Technologies' },
      { name: 'Prof. Sneha Reddy', email: 'sneha.reddy@college.edu', dept: 'CSE-AIML', specialization: 'Machine Learning' },
      { name: 'Dr. Vikram Singh', email: 'vikram.singh@college.edu', dept: 'CSE-AIML', specialization: 'Deep Learning' },
      { name: 'Prof. Anita Desai', email: 'anita.desai@college.edu', dept: 'CSE-DS', specialization: 'Data Analytics' },
      { name: 'Dr. Karthik Iyer', email: 'karthik.iyer@college.edu', dept: 'CSE-DS', specialization: 'Big Data' },
      { name: 'Prof. Meera Nair', email: 'meera.nair@college.edu', dept: 'CSE', specialization: 'Operating Systems' }
    ];
    
    const facultyPassword = await hashPassword('faculty123');
    const facultyRecords = [];
    
    for (const faculty of facultyData) {
      const username = faculty.email.split('@')[0];
      const userResult = await db.collection('users').insertOne({
        username: username,
        email: faculty.email,
        password: facultyPassword,
        role: 'FACULTY',
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date()
      });
      
      const facultyResult = await db.collection('faculty').insertOne({
        userId: userResult.insertedId.toString(),
        name: faculty.name,
        email: faculty.email,
        department: faculty.dept,
        specialization: faculty.specialization,
        phone: `+91-98${Math.floor(Math.random() * 100000000)}`,
        joiningDate: new Date('2020-07-01'),
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date()
      });
      
      facultyRecords.push({ _id: facultyResult.insertedId, ...faculty });
    }
    console.log(`✓ Created ${facultyRecords.length} faculty members`);
    
    // Students
    const studentData = [
      { name: 'Aarav Sharma', rollNo: 'CSE21A001', course: 'CSE', year: 1, section: 'A', semester: 1 },
      { name: 'Diya Patel', rollNo: 'CSE21A002', course: 'CSE', year: 1, section: 'A', semester: 1 },
      { name: 'Arjun Reddy', rollNo: 'CSE21A003', course: 'CSE', year: 1, section: 'A', semester: 1 },
      { name: 'Ananya Kumar', rollNo: 'CSE21A004', course: 'CSE', year: 1, section: 'A', semester: 1 },
      { name: 'Rohan Gupta', rollNo: 'CSE21A005', course: 'CSE', year: 1, section: 'A', semester: 1 },
      { name: 'Ishaan Verma', rollNo: 'CSE20A001', course: 'CSE', year: 2, section: 'A', semester: 3 },
      { name: 'Kavya Singh', rollNo: 'CSE20A002', course: 'CSE', year: 2, section: 'A', semester: 3 },
      { name: 'Aditya Joshi', rollNo: 'CSE20A003', course: 'CSE', year: 2, section: 'A', semester: 3 },
      { name: 'Sai Krishna', rollNo: 'AIML21A001', course: 'CSE-AIML', year: 1, section: 'A', semester: 1 },
      { name: 'Priya Menon', rollNo: 'AIML21A002', course: 'CSE-AIML', year: 1, section: 'A', semester: 1 },
      { name: 'Rahul Nair', rollNo: 'AIML21A003', course: 'CSE-AIML', year: 1, section: 'A', semester: 1 },
      { name: 'Sneha Iyer', rollNo: 'AIML21A004', course: 'CSE-AIML', year: 1, section: 'A', semester: 1 },
      { name: 'Kiran Kumar', rollNo: 'AIML20A001', course: 'CSE-AIML', year: 2, section: 'A', semester: 3 },
      { name: 'Divya Rao', rollNo: 'AIML20A002', course: 'CSE-AIML', year: 2, section: 'A', semester: 3 },
      { name: 'Varun Reddy', rollNo: 'DS21A001', course: 'CSE-DS', year: 1, section: 'A', semester: 1 },
      { name: 'Lakshmi Devi', rollNo: 'DS21A002', course: 'CSE-DS', year: 1, section: 'A', semester: 1 },
      { name: 'Harish Babu', rollNo: 'DS21A003', course: 'CSE-DS', year: 1, section: 'A', semester: 1 },
      { name: 'Pooja Reddy', rollNo: 'DS21A004', course: 'CSE-DS', year: 1, section: 'A', semester: 1 },
      { name: 'Naveen Kumar', rollNo: 'DS20A001', course: 'CSE-DS', year: 2, section: 'A', semester: 3 },
      { name: 'Swathi Sharma', rollNo: 'DS20A002', course: 'CSE-DS', year: 2, section: 'A', semester: 3 }
    ];
    
    const studentPassword = await hashPassword('student123');
    const studentRecords = [];
    
    for (const student of studentData) {
      const username = student.rollNo.toLowerCase();
      const email = `${username}@student.college.edu`;
      
      const userResult = await db.collection('users').insertOne({
        username: username,
        email: email,
        password: studentPassword,
        role: 'STUDENT',
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date()
      });
      
      const studentResult = await db.collection('students').insertOne({
        userId: userResult.insertedId.toString(),
        rollNumber: student.rollNo,
        name: student.name,
        email: email,
        courseId: courseMap[student.course],
        year: student.year,
        section: student.section,
        semester: student.semester,
        phone: `+91-98${Math.floor(Math.random() * 100000000)}`,
        dateOfBirth: new Date('2003-05-15'),
        admissionDate: new Date('2021-08-01'),
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date()
      });
      
      studentRecords.push({ _id: studentResult.insertedId, ...student, courseId: courseMap[student.course] });
    }
    console.log(`✓ Created ${studentRecords.length} students\n`);
    
    // PHASE 3: Class Allocations
    console.log('📋 PHASE 3: Creating Class Allocations');
    console.log('─────────────────────────────────────────');
    
    const subjects = await db.collection('subjects').find({}).toArray();
    const allocations = [];
    
    // Create allocations for each course/semester combination
    for (const course of ['CSE', 'CSE-AIML', 'CSE-DS']) {
      for (const semester of [1, 3]) {
        const courseSubjects = subjects.filter(s => 
          s.courseId === courseMap[course] && s.semester === semester
        );
        
        for (let i = 0; i < Math.min(courseSubjects.length, 3); i++) {
          allocations.push({
            facultyId: facultyRecords[i % facultyRecords.length]._id.toString(),
            subjectId: courseSubjects[i]._id.toString(),
            courseId: courseMap[course],
            year: semester === 1 ? 1 : 2,
            section: 'A',
            semester: semester,
            academicYear: '2023-2024',
            isActive: true,
            createdAt: new Date(),
            updatedAt: new Date()
          });
        }
      }
    }
    
    const allocationResult = await db.collection('classAllocations').insertMany(allocations);
    console.log(`✓ Created ${allocationResult.insertedCount} class allocations\n`);
    
    // PHASE 4: Attendance
    console.log('✅ PHASE 4: Marking Sample Attendance');
    console.log('─────────────────────────────────────────');
    
    const insertedAllocations = await db.collection('classAllocations').find({}).toArray();
    const attendanceRecords = [];
    
    for (const allocation of insertedAllocations) {
      const studentsForAllocation = studentRecords.filter(s => 
        s.courseId === allocation.courseId &&
        s.year === allocation.year &&
        s.section === allocation.section &&
        s.semester === allocation.semester
      );
      
      for (let day = 0; day < 15; day++) {
        const attendanceDate = getRandomPastDate(30);
        
        for (const student of studentsForAllocation) {
          attendanceRecords.push({
            studentId: student._id.toString(),
            subjectId: allocation.subjectId,
            facultyId: allocation.facultyId,
            date: attendanceDate,
            status: getRandomAttendanceStatus(),
            remarks: Math.random() > 0.8 ? 'Medical leave' : '',
            markedBy: allocation.facultyId,
            createdAt: attendanceDate,
            updatedAt: attendanceDate
          });
        }
      }
    }
    
    if (attendanceRecords.length > 0) {
      const attendanceResult = await db.collection('attendance').insertMany(attendanceRecords);
      console.log(`✓ Created ${attendanceResult.insertedCount} attendance records\n`);
    }
    
    // Summary
    console.log('\n╔════════════════════════════════════════════════════════╗');
    console.log('║              Seeding Complete! 🎉                      ║');
    console.log('╚════════════════════════════════════════════════════════╝\n');
    
    console.log('📊 Summary:');
    console.log(`   Courses: ${courses.length}`);
    console.log(`   Subjects: ${totalSubjects}`);
    console.log(`   Admin: 1`);
    console.log(`   Faculty: ${facultyRecords.length}`);
    console.log(`   Students: ${studentRecords.length}`);
    console.log(`   Class Allocations: ${allocations.length}`);
    console.log(`   Attendance Records: ${attendanceRecords.length}`);
    
    console.log('\n🔐 Login Credentials:');
    console.log('   Admin: admin / admin123');
    console.log('   Faculty: rajesh.kumar / faculty123');
    console.log('   Student: cse21a001 / student123');
    
    console.log('\n📖 See DEMO_CREDENTIALS.md for complete list\n');
    
  } catch (error) {
    console.error('❌ Error seeding data:', error);
  } finally {
    await client.close();
    console.log('✓ MongoDB connection closed\n');
  }
}

// Run the seeding
seedAll();
