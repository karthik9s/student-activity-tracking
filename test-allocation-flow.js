const { MongoClient } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

async function testAllocationFlow() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    const db = client.db('student_tracker');
    
    console.log('=== Testing Class Allocation Flow ===\n');
    
    // Step 1: Get Rajesh's records
    const user = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    const faculty = await db.collection('faculty').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    
    if (!user || !faculty) {
      console.log('❌ User or faculty record not found');
      return;
    }
    
    console.log('Step 1: Rajesh Kumar Records');
    console.log(`  User ID: ${user._id}`);
    console.log(`  Faculty ID: ${faculty._id}`);
    console.log(`  Faculty.userId: ${faculty.userId}`);
    console.log(`  User Role: ${user.role}\n`);
    
    // Step 2: Check what the backend expects
    console.log('Step 2: Backend Expectation');
    console.log(`  The FacultyController uses authentication.getName() which returns: ${user._id}`);
    console.log(`  So allocations must have facultyId = ${user._id}\n`);
    
    // Step 3: Check current allocations
    const allocationsByUserId = await db.collection('class_allocations').find({ 
      facultyId: user._id.toString() 
    }).toArray();
    
    const allocationsByFacultyId = await db.collection('class_allocations').find({ 
      facultyId: faculty._id.toString() 
    }).toArray();
    
    console.log('Step 3: Current Allocations');
    console.log(`  Allocations with User ID (${user._id}): ${allocationsByUserId.length}`);
    console.log(`  Allocations with Faculty ID (${faculty._id}): ${allocationsByFacultyId.length}\n`);
    
    if (allocationsByUserId.length > 0) {
      console.log('✅ Allocations found with correct User ID!');
      console.log('\nAllocation Details:');
      allocationsByUserId.forEach((alloc, i) => {
        console.log(`\n  Allocation ${i + 1}:`);
        console.log(`    ID: ${alloc._id}`);
        console.log(`    Subject: ${alloc.subjectName || 'N/A'}`);
        console.log(`    Subject ID: ${alloc.subjectId}`);
        console.log(`    Course: ${alloc.courseName || 'N/A'}`);
        console.log(`    Year: ${alloc.year}, Semester: ${alloc.semester}, Section: ${alloc.section}`);
        console.log(`    Faculty ID in allocation: ${alloc.facultyId}`);
      });
      
      // Step 4: Verify subject details
      console.log('\n\nStep 4: Verifying Subject Details');
      for (const alloc of allocationsByUserId) {
        const subject = await db.collection('subjects').findOne({ 
          _id: alloc.subjectId 
        });
        
        if (subject) {
          console.log(`  ✅ Subject found: ${subject.name}`);
          console.log(`     Code: ${subject.code}`);
          console.log(`     Semester: ${subject.semester}`);
        } else {
          console.log(`  ❌ Subject not found for ID: ${alloc.subjectId}`);
        }
      }
      
      // Step 5: Check students in the class
      console.log('\n\nStep 5: Students in the Class');
      const firstAlloc = allocationsByUserId[0];
      const students = await db.collection('students').find({
        courseId: firstAlloc.courseId,
        year: firstAlloc.year,
        semester: firstAlloc.semester,
        section: firstAlloc.section
      }).toArray();
      
      console.log(`  Found ${students.length} students`);
      if (students.length > 0) {
        console.log('  Students:');
        students.forEach(s => {
          console.log(`    - ${s.name} (${s.rollNumber})`);
        });
      }
      
    } else if (allocationsByFacultyId.length > 0) {
      console.log('❌ Problem Found: Allocations use Faculty ID instead of User ID!');
      console.log('\nFixing now...\n');
      
      const result = await db.collection('class_allocations').updateMany(
        { facultyId: faculty._id.toString() },
        { $set: { facultyId: user._id.toString() } }
      );
      
      console.log(`✅ Fixed ${result.modifiedCount} allocation(s)`);
      console.log('\nPlease refresh the page and try again!');
      
    } else {
      console.log('❌ No allocations found at all!');
      console.log('\nYou need to create a class allocation first.');
      console.log('Run: node add-rajesh-class.js');
    }
    
    console.log('\n\n=== Summary ===');
    console.log(`Total allocations for Rajesh: ${allocationsByUserId.length}`);
    console.log(`Rajesh should see ${allocationsByUserId.length} subject(s) in the dropdown`);
    
  } finally {
    await client.close();
  }
}

testAllocationFlow();
