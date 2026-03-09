const { MongoClient } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

async function recreateAllocation() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    const db = client.db('student_tracker');
    
    console.log('=== Recreating Rajesh Kumar\'s Class Allocation ===\n');
    
    // Get Rajesh's user record
    const user = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    if (!user) {
      console.log('❌ User not found');
      return;
    }
    console.log(`✓ Found user: ${user._id}\n`);
    
    // Delete old allocations for Rajesh
    const deleteResult = await db.collection('class_allocations').deleteMany({ 
      facultyId: user._id.toString() 
    });
    console.log(`✓ Deleted ${deleteResult.deletedCount} old allocation(s)\n`);
    
    // Get CSE course
    const course = await db.collection('courses').findOne({ code: 'CSE' });
    if (!course) {
      console.log('❌ CSE course not found');
      return;
    }
    console.log(`✓ Found course: ${course.name} (${course._id})`);
    
    // Get a first semester subject
    const subject = await db.collection('subjects').findOne({ 
      courseId: course._id.toString(),
      semester: 1
    });
    if (!subject) {
      console.log('❌ Subject not found');
      return;
    }
    console.log(`✓ Found subject: ${subject.name} (${subject._id})\n`);
    
    // Get students
    const students = await db.collection('students').find({
      courseId: course._id.toString(),
      year: 1,
      semester: 1,
      section: 'A'
    }).toArray();
    console.log(`✓ Found ${students.length} students\n`);
    
    // Create new allocation with correct IDs
    const allocation = {
      facultyId: user._id.toString(),
      facultyName: 'Rajesh Kumar',
      subjectId: subject._id.toString(),
      subjectName: subject.name,
      courseId: course._id.toString(),
      courseName: course.name,
      year: 1,
      semester: '1',
      section: 'A',
      academicYear: '2023-2024',
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date()
    };
    
    const result = await db.collection('class_allocations').insertOne(allocation);
    console.log(`✅ Created new allocation: ${result.insertedId}\n`);
    
    // Create attendance records for today
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    // Delete old attendance for this class
    await db.collection('attendance').deleteMany({
      facultyId: user._id.toString(),
      subjectId: subject._id.toString(),
      date: today
    });
    
    const attendanceRecords = students.map(student => ({
      studentId: student._id.toString(),
      studentName: student.name,
      studentRollNumber: student.rollNumber,
      facultyId: user._id.toString(),
      facultyName: 'Rajesh Kumar',
      subjectId: subject._id.toString(),
      subjectName: subject.name,
      courseId: course._id.toString(),
      date: today,
      status: 'PRESENT',
      markedAt: new Date(),
      academicYear: '2023-2024',
      year: 1,
      semester: 1,
      section: 'A'
    }));
    
    if (attendanceRecords.length > 0) {
      await db.collection('attendance').insertMany(attendanceRecords);
      console.log(`✅ Created ${attendanceRecords.length} attendance records\n`);
    }
    
    console.log('=== Summary ===');
    console.log(`Faculty: Rajesh Kumar`);
    console.log(`Subject: ${subject.name}`);
    console.log(`Course: ${course.name}`);
    console.log(`Class: Year 1, Semester 1, Section A`);
    console.log(`Students: ${students.length}`);
    console.log(`\n✅ Done! Rajesh can now see "${subject.name}" in the dropdown.`);
    console.log(`\nPlease logout and login again as Rajesh to see the changes.`);
    
  } finally {
    await client.close();
  }
}

recreateAllocation();
