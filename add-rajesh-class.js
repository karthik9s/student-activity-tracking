// Quick script to add a class allocation for Rajesh Kumar and mark attendance
const { MongoClient, ObjectId } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

async function addClassAndAttendance() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB\n');
    
    const db = client.db('student_tracker');
    
    // Get Rajesh Kumar's faculty record
    const rajesh = await db.collection('faculty').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    if (!rajesh) {
      console.log('❌ Rajesh Kumar not found');
      return;
    }
    console.log('✓ Found Rajesh Kumar');
    
    // Get CSE course
    const course = await db.collection('courses').findOne({ code: 'CSE' });
    if (!course) {
      console.log('❌ CSE course not found');
      return;
    }
    console.log('✓ Found CSE course');
    
    // Get a first semester subject
    const subject = await db.collection('subjects').findOne({ 
      courseId: course._id.toString(),
      semester: 1
    });
    if (!subject) {
      console.log('❌ Subject not found');
      return;
    }
    console.log('✓ Found subject:', subject.name);
    
    // Get CSE Year 1 Semester 1 Section A students
    const students = await db.collection('students').find({
      courseId: course._id.toString(),
      year: 1,
      semester: 1,
      section: 'A'
    }).toArray();
    console.log(`✓ Found ${students.length} students\n`);
    
    // Create class allocation
    const allocation = {
      facultyId: rajesh._id,
      facultyName: rajesh.name,
      subjectId: subject._id,
      subjectName: subject.name,
      courseId: course._id,
      courseName: course.name,
      year: 1,
      semester: 1,
      section: 'A',
      academicYear: '2023-2024',
      createdAt: new Date(),
      updatedAt: new Date()
    };
    
    // Check if allocation already exists
    const existingAllocation = await db.collection('class_allocations').findOne({
      facultyId: rajesh._id,
      subjectId: subject._id,
      year: 1,
      semester: 1,
      section: 'A'
    });
    
    let allocationId;
    if (existingAllocation) {
      console.log('✓ Class allocation already exists');
      allocationId = existingAllocation._id;
    } else {
      const result = await db.collection('class_allocations').insertOne(allocation);
      allocationId = result.insertedId;
      console.log('✓ Created new class allocation');
    }
    
    // Mark attendance for today
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const attendanceRecords = [];
    for (const student of students) {
      // Check if attendance already marked for today
      const existing = await db.collection('attendance').findOne({
        studentId: student._id,
        subjectId: subject._id,
        date: today
      });
      
      if (!existing) {
        attendanceRecords.push({
          studentId: student._id,
          studentName: student.name,
          studentRollNumber: student.rollNumber,
          facultyId: rajesh._id,
          facultyName: rajesh.name,
          subjectId: subject._id,
          subjectName: subject.name,
          courseId: course._id,
          date: today,
          status: 'PRESENT', // Mark all as present
          markedAt: new Date(),
          academicYear: '2023-2024',
          year: 1,
          semester: 1,
          section: 'A'
        });
      }
    }
    
    if (attendanceRecords.length > 0) {
      await db.collection('attendance').insertMany(attendanceRecords);
      console.log(`✓ Marked attendance for ${attendanceRecords.length} students\n`);
    } else {
      console.log('✓ Attendance already marked for today\n');
    }
    
    console.log('=== Summary ===');
    console.log(`Faculty: ${rajesh.name}`);
    console.log(`Subject: ${subject.name}`);
    console.log(`Class: Year 1, Semester 1, Section A`);
    console.log(`Students: ${students.length}`);
    console.log(`Date: ${today.toDateString()}`);
    console.log('\n✅ Done! You can now login as Rajesh Kumar to view this data.');
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
    console.log('\nMongoDB connection closed');
  }
}

addClassAndAttendance();
