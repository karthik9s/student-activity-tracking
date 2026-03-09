// Complete Demo Data Seeding Script
// This script creates: Users, Students, Faculty, Class Allocations, and Attendance
// Run after seed-subjects-data.js

const { MongoClient, ObjectId } = require('mongodb');
const bcrypt = require('bcryptjs');

// MongoDB connection string - UPDATE THIS
const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

// Helper function to hash passwords
async function hashPassword(password) {
  return await bcrypt.hash(password, 10);
}

// Helper function to get random date in the past 30 days
function getRandomPastDate(daysAgo = 30) {
  const date = new Date();
  date.setDate(date.getDate() - Math.floor(Math.random() * daysAgo));
  return date;
}

// Helper function to get random attendance status
function getRandomAttendanceStatus() {
  const statuses = ['PRESENT', 'PRESENT', 'PRESENT', 'PRESENT', 'ABSENT', 'LATE'];
  return statuses[Math.floor(Math.random() * statuses.length)];
}

async function seedCompleteData() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    
    // Get collections
    const usersCollection = db.collection('users');
    const studentsCollection = db.collection('students');
    const facultyCollection = db.collection('faculty');
    const coursesCollection = db.collection('courses');
    const subjectsCollection = db.collection('subjects');
    const classAllocationsCollection = db.collection('classAllocations');
    const attendanceCollection = db.collection('attendance');
    
    // Get existing courses
    const courses = await coursesCollection.find({}).toArray();
    if (courses.length === 0) {
      console.error('No courses found! Please run seed-subjects-data.js first.');
      return;
    }
    
    const courseMap = {};
    courses.forEach(course => {
      courseMap[course.code] = course._id.toString();
    });
    
    console.log('\n=== Starting Complete Demo Data Seeding ===\n');
    
    // ==================== STEP 0: Clear Existing Data ====================
    console.log('Step 0: Clearing existing data...');
    await usersCollection.deleteMany({});
    await studentsCollection.deleteMany({});
    await facultyCollection.deleteMany({});
    await classAllocationsCollection.deleteMany({});
    await attendanceCollection.deleteMany({});
    console.log('✓ All existing data cleared\n');
    
    // ==================== STEP 1: Create Admin User ====================
    console.log('Step 1: Creating Admin User...');
    
    const adminPassword = await hashPassword('admin123');
    const adminUser = {
      username: 'admin',
      email: 'admin@cvr.ac.in',
      password: adminPassword,
      role: 'ROLE_ADMIN',
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date()
    };
    
    const adminResult = await usersCollection.insertOne(adminUser);
    console.log(`✓ Admin user created: admin / admin123`);
    
    // ==================== STEP 2: Create Faculty Users ====================
    console.log('\nStep 2: Creating Faculty Users...');
    
    const facultyData = [
      { firstName: 'Rajesh', lastName: 'Kumar', email: 'rajesh.kumar@cvr.ac.in', employeeId: 'FAC001', dept: 'CSE', designation: 'Professor' },
      { firstName: 'Priya', lastName: 'Sharma', email: 'priya.sharma@cvr.ac.in', employeeId: 'FAC002', dept: 'CSE', designation: 'Associate Professor' },
      { firstName: 'Amit', lastName: 'Patel', email: 'amit.patel@cvr.ac.in', employeeId: 'FAC003', dept: 'CSE', designation: 'Assistant Professor' },
      { firstName: 'Sneha', lastName: 'Reddy', email: 'sneha.reddy@cvr.ac.in', employeeId: 'FAC004', dept: 'CSE-AIML', designation: 'Professor' },
      { firstName: 'Vikram', lastName: 'Singh', email: 'vikram.singh@cvr.ac.in', employeeId: 'FAC005', dept: 'CSE-AIML', designation: 'Associate Professor' },
      { firstName: 'Anita', lastName: 'Desai', email: 'anita.desai@cvr.ac.in', employeeId: 'FAC006', dept: 'CSE-DS', designation: 'Professor' },
      { firstName: 'Karthik', lastName: 'Iyer', email: 'karthik.iyer@cvr.ac.in', employeeId: 'FAC007', dept: 'CSE-DS', designation: 'Assistant Professor' },
      { firstName: 'Meera', lastName: 'Nair', email: 'meera.nair@cvr.ac.in', employeeId: 'FAC008', dept: 'CSE', designation: 'Associate Professor' }
    ];
    
    const facultyPassword = await hashPassword('faculty123');
    const facultyUsers = [];
    const facultyRecords = [];
    
    for (const faculty of facultyData) {
      const username = faculty.email.split('@')[0]; // Will be like: rajesh.kumar
      
      // Create user account
      const user = {
        username: username,
        email: faculty.email,
        password: facultyPassword,
        role: 'ROLE_FACULTY',
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date()
      };
      
      const userResult = await usersCollection.insertOne(user);
      facultyUsers.push({ ...user, _id: userResult.insertedId });
      
      // Create faculty record
      const facultyRecord = {
        userId: userResult.insertedId.toString(),
        employeeId: faculty.employeeId,
        firstName: faculty.firstName,
        lastName: faculty.lastName,
        email: faculty.email,
        department: faculty.dept,
        designation: faculty.designation,
        phone: `+91-98${Math.floor(Math.random() * 100000000)}`,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date()
      };
      
      const facultyResult = await facultyCollection.insertOne(facultyRecord);
      facultyRecords.push({ ...facultyRecord, _id: facultyResult.insertedId });
      
      console.log(`✓ Faculty created: ${faculty.firstName} ${faculty.lastName} (${username} / faculty123)`);
    }
    
    // ==================== STEP 3: Create Student Users ====================
    console.log('\nStep 3: Creating Student Users...');
    
    const studentData = [
      // CSE Students - Year 1, Section A
      { name: 'Aarav Sharma', rollNo: 'CSE21A001', course: 'CSE', year: 1, section: 'A', semester: 1 },
      { name: 'Diya Patel', rollNo: 'CSE21A002', course: 'CSE', year: 1, section: 'A', semester: 1 },
      { name: 'Arjun Reddy', rollNo: 'CSE21A003', course: 'CSE', year: 1, section: 'A', semester: 1 },
      { name: 'Ananya Kumar', rollNo: 'CSE21A004', course: 'CSE', year: 1, section: 'A', semester: 1 },
      { name: 'Rohan Gupta', rollNo: 'CSE21A005', course: 'CSE', year: 1, section: 'A', semester: 1 },
      
      // CSE Students - Year 2, Section A
      { name: 'Ishaan Verma', rollNo: 'CSE20A001', course: 'CSE', year: 2, section: 'A', semester: 3 },
      { name: 'Kavya Singh', rollNo: 'CSE20A002', course: 'CSE', year: 2, section: 'A', semester: 3 },
      { name: 'Aditya Joshi', rollNo: 'CSE20A003', course: 'CSE', year: 2, section: 'A', semester: 3 },
      
      // CSE-AIML Students - Year 1, Section A
      { name: 'Sai Krishna', rollNo: 'AIML21A001', course: 'CSE-AIML', year: 1, section: 'A', semester: 1 },
      { name: 'Priya Menon', rollNo: 'AIML21A002', course: 'CSE-AIML', year: 1, section: 'A', semester: 1 },
      { name: 'Rahul Nair', rollNo: 'AIML21A003', course: 'CSE-AIML', year: 1, section: 'A', semester: 1 },
      { name: 'Sneha Iyer', rollNo: 'AIML21A004', course: 'CSE-AIML', year: 1, section: 'A', semester: 1 },
      
      // CSE-AIML Students - Year 2, Section A
      { name: 'Kiran Kumar', rollNo: 'AIML20A001', course: 'CSE-AIML', year: 2, section: 'A', semester: 3 },
      { name: 'Divya Rao', rollNo: 'AIML20A002', course: 'CSE-AIML', year: 2, section: 'A', semester: 3 },
      
      // CSE-DS Students - Year 1, Section A
      { name: 'Varun Reddy', rollNo: 'DS21A001', course: 'CSE-DS', year: 1, section: 'A', semester: 1 },
      { name: 'Lakshmi Devi', rollNo: 'DS21A002', course: 'CSE-DS', year: 1, section: 'A', semester: 1 },
      { name: 'Harish Babu', rollNo: 'DS21A003', course: 'CSE-DS', year: 1, section: 'A', semester: 1 },
      { name: 'Pooja Reddy', rollNo: 'DS21A004', course: 'CSE-DS', year: 1, section: 'A', semester: 1 },
      
      // CSE-DS Students - Year 2, Section A
      { name: 'Naveen Kumar', rollNo: 'DS20A001', course: 'CSE-DS', year: 2, section: 'A', semester: 3 },
      { name: 'Swathi Sharma', rollNo: 'DS20A002', course: 'CSE-DS', year: 2, section: 'A', semester: 3 }
    ];
    
    const studentPassword = await hashPassword('student123');
    const studentUsers = [];
    const studentRecords = [];
    
    for (const student of studentData) {
      const username = student.rollNo.toLowerCase();
      const email = `${username}@cvr.ac.in`;
      
      // Create user account
      const user = {
        username: username,
        email: email,
        password: studentPassword,
        role: 'ROLE_STUDENT',
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date()
      };
      
      const userResult = await usersCollection.insertOne(user);
      studentUsers.push({ ...user, _id: userResult.insertedId });
      
      // Create student record
      const studentRecord = {
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
      };
      
      const studentResult = await studentsCollection.insertOne(studentRecord);
      studentRecords.push({ ...studentRecord, _id: studentResult.insertedId });
      
      console.log(`✓ Student created: ${student.name} (${username} / student123)`);
    }
    
    // ==================== STEP 4: Assign Subjects to Faculty (Class Allocations) ====================
    console.log('\nStep 4: Creating Class Allocations...');
    
    // Get subjects for each course and semester
    const cseSubjects = await subjectsCollection.find({ 
      courseId: courseMap['CSE'], 
      semester: { $in: [1, 3] } 
    }).toArray();
    
    const aimlSubjects = await subjectsCollection.find({ 
      courseId: courseMap['CSE-AIML'], 
      semester: { $in: [1, 3] } 
    }).toArray();
    
    const dsSubjects = await subjectsCollection.find({ 
      courseId: courseMap['CSE-DS'], 
      semester: { $in: [1, 3] } 
    }).toArray();
    
    const allocations = [];
    
    // CSE Semester 1 allocations
    const cseSem1 = cseSubjects.filter(s => s.semester === 1);
    for (let i = 0; i < Math.min(cseSem1.length, 3); i++) {
      allocations.push({
        facultyId: facultyRecords[i % facultyRecords.length]._id.toString(),
        subjectId: cseSem1[i]._id.toString(),
        courseId: courseMap['CSE'],
        year: 1,
        section: 'A',
        semester: 1,
        academicYear: '2023-2024'
      });
    }
    
    // CSE Semester 3 allocations
    const cseSem3 = cseSubjects.filter(s => s.semester === 3);
    for (let i = 0; i < Math.min(cseSem3.length, 3); i++) {
      allocations.push({
        facultyId: facultyRecords[(i + 1) % facultyRecords.length]._id.toString(),
        subjectId: cseSem3[i]._id.toString(),
        courseId: courseMap['CSE'],
        year: 2,
        section: 'A',
        semester: 3,
        academicYear: '2023-2024'
      });
    }
    
    // CSE-AIML Semester 1 allocations
    const aimlSem1 = aimlSubjects.filter(s => s.semester === 1);
    for (let i = 0; i < Math.min(aimlSem1.length, 3); i++) {
      allocations.push({
        facultyId: facultyRecords[(i + 3) % facultyRecords.length]._id.toString(),
        subjectId: aimlSem1[i]._id.toString(),
        courseId: courseMap['CSE-AIML'],
        year: 1,
        section: 'A',
        semester: 1,
        academicYear: '2023-2024'
      });
    }
    
    // CSE-AIML Semester 3 allocations
    const aimlSem3 = aimlSubjects.filter(s => s.semester === 3);
    for (let i = 0; i < Math.min(aimlSem3.length, 3); i++) {
      allocations.push({
        facultyId: facultyRecords[(i + 4) % facultyRecords.length]._id.toString(),
        subjectId: aimlSem3[i]._id.toString(),
        courseId: courseMap['CSE-AIML'],
        year: 2,
        section: 'A',
        semester: 3,
        academicYear: '2023-2024'
      });
    }
    
    // CSE-DS Semester 1 allocations
    const dsSem1 = dsSubjects.filter(s => s.semester === 1);
    for (let i = 0; i < Math.min(dsSem1.length, 3); i++) {
      allocations.push({
        facultyId: facultyRecords[(i + 5) % facultyRecords.length]._id.toString(),
        subjectId: dsSem1[i]._id.toString(),
        courseId: courseMap['CSE-DS'],
        year: 1,
        section: 'A',
        semester: 1,
        academicYear: '2023-2024'
      });
    }
    
    // CSE-DS Semester 3 allocations
    const dsSem3 = dsSubjects.filter(s => s.semester === 3);
    for (let i = 0; i < Math.min(dsSem3.length, 3); i++) {
      allocations.push({
        facultyId: facultyRecords[(i + 6) % facultyRecords.length]._id.toString(),
        subjectId: dsSem3[i]._id.toString(),
        courseId: courseMap['CSE-DS'],
        year: 2,
        section: 'A',
        semester: 3,
        academicYear: '2023-2024'
      });
    }
    
    // Add timestamps and insert
    const allocationsWithTimestamps = allocations.map(alloc => ({
      ...alloc,
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date()
    }));
    
    await classAllocationsCollection.deleteMany({});
    const allocationResult = await classAllocationsCollection.insertMany(allocationsWithTimestamps);
    console.log(`✓ Created ${allocationResult.insertedCount} class allocations`);
    
    // Get inserted allocations with IDs
    const insertedAllocations = await classAllocationsCollection.find({}).toArray();
    
    // ==================== STEP 5: Mark Sample Attendance ====================
    console.log('\nStep 5: Marking Sample Attendance...');
    
    const attendanceRecords = [];
    const attendanceMap = new Map(); // Track unique combinations
    
    // For each allocation, create attendance for the past 15 days
    for (const allocation of insertedAllocations) {
      // Get students for this allocation
      const studentsForAllocation = studentRecords.filter(s => 
        s.courseId === allocation.courseId &&
        s.year === allocation.year &&
        s.section === allocation.section &&
        s.semester === allocation.semester
      );
      
      // Create 15 attendance sessions (one per day)
      for (let day = 0; day < 15; day++) {
        // Use a specific date for each day (not random to avoid duplicates)
        const attendanceDate = new Date();
        attendanceDate.setDate(attendanceDate.getDate() - day);
        attendanceDate.setHours(9, 0, 0, 0); // Set to 9 AM to normalize time
        
        for (const student of studentsForAllocation) {
          // Create unique key to prevent duplicates
          const uniqueKey = `${student._id.toString()}_${allocation.subjectId}_${attendanceDate.toISOString()}`;
          
          if (!attendanceMap.has(uniqueKey)) {
            attendanceMap.set(uniqueKey, true);
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
    }
    
    await attendanceCollection.deleteMany({});
    if (attendanceRecords.length > 0) {
      const attendanceResult = await attendanceCollection.insertMany(attendanceRecords);
      console.log(`✓ Created ${attendanceResult.insertedCount} attendance records`);
    }
    
    // ==================== SUMMARY ====================
    console.log('\n=== Data Seeding Summary ===');
    console.log(`\nUsers Created:`);
    console.log(`  - Admin: 1`);
    console.log(`  - Faculty: ${facultyRecords.length}`);
    console.log(`  - Students: ${studentRecords.length}`);
    console.log(`  - Total Users: ${1 + facultyRecords.length + studentRecords.length}`);
    
    console.log(`\nClass Allocations: ${insertedAllocations.length}`);
    console.log(`Attendance Records: ${attendanceRecords.length}`);
    
    console.log(`\n=== Login Credentials ===`);
    console.log(`\nAdmin:`);
    console.log(`  Username: admin`);
    console.log(`  Password: admin123`);
    
    console.log(`\nSample Faculty:`);
    console.log(`  Username: rajesh.kumar`);
    console.log(`  Password: faculty123`);
    console.log(`  (All faculty use password: faculty123)`);
    
    console.log(`\nSample Students:`);
    console.log(`  Username: cse21a001`);
    console.log(`  Password: student123`);
    console.log(`  (All students use password: student123)`);
    
    console.log(`\n=== Course Distribution ===`);
    const cseStudents = studentRecords.filter(s => s.courseId === courseMap['CSE']);
    const aimlStudents = studentRecords.filter(s => s.courseId === courseMap['CSE-AIML']);
    const dsStudents = studentRecords.filter(s => s.courseId === courseMap['CSE-DS']);
    
    console.log(`CSE: ${cseStudents.length} students`);
    console.log(`CSE-AIML: ${aimlStudents.length} students`);
    console.log(`CSE-DS: ${dsStudents.length} students`);
    
    console.log('\n✓ Complete demo data seeding finished successfully!');
    
  } catch (error) {
    console.error('Error seeding data:', error);
  } finally {
    await client.close();
    console.log('\nMongoDB connection closed');
  }
}

// Run the seeding
seedCompleteData();
