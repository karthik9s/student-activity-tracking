const { MongoClient } = require('mongodb');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';

async function checkStudentAttendance() {
  const client = new MongoClient(uri);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    
    // Get a sample student
    const student = await db.collection('students').findOne({ email: 'cse21a001@cvr.ac.in' });
    console.log('\n=== Sample Student ===');
    console.log('Student ID:', student._id.toString());
    console.log('User ID:', student.userId);
    console.log('Name:', student.firstName, student.lastName);
    console.log('Email:', student.email);
    
    // Check attendance records for this student
    console.log('\n=== Attendance Records (by Student ID) ===');
    const attendanceByStudentId = await db.collection('attendance')
      .find({ studentId: student._id.toString() })
      .limit(5)
      .toArray();
    console.log('Count:', attendanceByStudentId.length);
    if (attendanceByStudentId.length > 0) {
      console.log('Sample:', JSON.stringify(attendanceByStudentId[0], null, 2));
    }
    
    // Check if attendance uses userId instead
    console.log('\n=== Attendance Records (by User ID) ===');
    const attendanceByUserId = await db.collection('attendance')
      .find({ studentId: student.userId })
      .limit(5)
      .toArray();
    console.log('Count:', attendanceByUserId.length);
    if (attendanceByUserId.length > 0) {
      console.log('Sample:', JSON.stringify(attendanceByUserId[0], null, 2));
    }
    
    // Check all attendance to see what studentId format is used
    console.log('\n=== All Attendance Records (sample) ===');
    const allAttendance = await db.collection('attendance')
      .find({})
      .limit(3)
      .toArray();
    console.log('Total attendance records:', await db.collection('attendance').countDocuments());
    allAttendance.forEach((att, idx) => {
      console.log(`\nRecord ${idx + 1}:`);
      console.log('  Student ID:', att.studentId);
      console.log('  Subject ID:', att.subjectId);
      console.log('  Date:', att.date);
      console.log('  Status:', att.status);
    });
    
    // Check subjects
    console.log('\n=== Subjects ===');
    const subjects = await db.collection('subjects').find({}).limit(3).toArray();
    console.log('Total subjects:', await db.collection('subjects').countDocuments());
    subjects.forEach((subj, idx) => {
      console.log(`\nSubject ${idx + 1}:`);
      console.log('  ID:', subj._id.toString());
      console.log('  Name:', subj.name);
      console.log('  Code:', subj.code);
    });
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
  }
}

checkStudentAttendance();
