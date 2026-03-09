// Verification Script - Check if data was seeded correctly
// Run: node verify-seeded-data.js

const { MongoClient } = require('mongodb');

// MongoDB connection string - UPDATE THIS
const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';
async function verifyData() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    console.log('✓ Connected to MongoDB\n');
    
    const db = client.db('student_tracker');
    
    console.log('=== Verification Report ===\n');
    
    // Check Users
    const usersCount = await db.collection('users').countDocuments();
    console.log(`Users: ${usersCount} (Expected: 29)`);
    
    if (usersCount > 0) {
      // Check admin
      const admin = await db.collection('users').findOne({ email: 'admin@cvr.ac.in' });
      console.log(`  Admin exists: ${admin ? '✓ YES' : '✗ NO'}`);
      if (admin) {
        console.log(`    - Email: ${admin.email}`);
        console.log(`    - Role: ${admin.role}`);
        console.log(`    - Active: ${admin.isActive}`);
      }
      
      // Check Rajesh Kumar
      const rajesh = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
      console.log(`  Rajesh Kumar exists: ${rajesh ? '✓ YES' : '✗ NO'}`);
      if (rajesh) {
        console.log(`    - Email: ${rajesh.email}`);
        console.log(`    - Username: ${rajesh.username}`);
        console.log(`    - Role: ${rajesh.role}`);
        console.log(`    - Active: ${rajesh.isActive}`);
        console.log(`    - Password Hash: ${rajesh.password ? 'Present' : 'Missing'}`);
      }
      
      // Check a student
      const student = await db.collection('users').findOne({ email: 'cse21a001@cvr.ac.in' });
      console.log(`  Student (cse21a001) exists: ${student ? '✓ YES' : '✗ NO'}`);
      if (student) {
        console.log(`    - Email: ${student.email}`);
        console.log(`    - Role: ${student.role}`);
      }
      
      // List all faculty emails
      console.log('\n  Faculty emails in database:');
      const faculty = await db.collection('users').find({ role: 'FACULTY' }).toArray();
      faculty.forEach(f => console.log(`    - ${f.email}`));
    }
    
    // Check Students
    const studentsCount = await db.collection('students').countDocuments();
    console.log(`\nStudents: ${studentsCount} (Expected: 20)`);
    
    // Check Faculty
    const facultyCount = await db.collection('faculty').countDocuments();
    console.log(`Faculty: ${facultyCount} (Expected: 8)`);
    
    // Check Courses
    const coursesCount = await db.collection('courses').countDocuments();
    console.log(`Courses: ${coursesCount} (Expected: 3)`);
    
    // Check Subjects
    const subjectsCount = await db.collection('subjects').countDocuments();
    console.log(`Subjects: ${subjectsCount} (Expected: 106)`);
    
    // Check Class Allocations
    const allocationsCount = await db.collection('classAllocations').countDocuments();
    console.log(`Class Allocations: ${allocationsCount} (Expected: ~18)`);
    
    // Check Attendance
    const attendanceCount = await db.collection('attendance').countDocuments();
    console.log(`Attendance Records: ${attendanceCount} (Expected: ~5,400)`);
    
    console.log('\n=== Summary ===');
    if (usersCount === 29 && studentsCount === 20 && facultyCount === 8) {
      console.log('✓ Data seeding appears successful!');
      console.log('\nYou can login with:');
      console.log('  Admin: admin@cvr.ac.in / admin123');
      console.log('  Faculty: rajesh.kumar@cvr.ac.in / faculty123');
      console.log('  Student: cse21a001@cvr.ac.in / student123');
    } else {
      console.log('✗ Data seeding incomplete or failed!');
      console.log('\nPlease run the seeding scripts:');
      console.log('  npm run seed:subjects');
      console.log('  npm run seed:demo');
    }
    
  } catch (error) {
    console.error('❌ Error:', error.message);
    console.log('\nTroubleshooting:');
    console.log('1. Check MongoDB connection string');
    console.log('2. Verify network access in MongoDB Atlas');
    console.log('3. Ensure IP is whitelisted');
  } finally {
    await client.close();
    console.log('\n✓ Connection closed');
  }
}

verifyData();
