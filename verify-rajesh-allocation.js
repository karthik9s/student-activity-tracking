const { MongoClient, ObjectId } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

async function verifyAllocation() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    const db = client.db('student_tracker');
    
    console.log('=== Verifying Rajesh Kumar\'s Class Allocation ===\n');
    
    // Get Rajesh's user record
    const user = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    console.log('User record:', user ? `Found - ID: ${user._id}` : 'Not found');
    
    // Get Rajesh's faculty record
    const faculty = await db.collection('faculty').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    console.log('Faculty record:', faculty ? `Found - ID: ${faculty._id}, Name: ${faculty.name}` : 'Not found');
    
    if (!faculty) {
      console.log('\n❌ Faculty record not found!');
      return;
    }
    
    // Check class allocations by faculty ID
    const allocations = await db.collection('class_allocations').find({ 
      facultyId: faculty._id 
    }).toArray();
    
    console.log(`\nClass allocations found: ${allocations.length}`);
    
    if (allocations.length > 0) {
      console.log('\n=== Allocation Details ===');
      allocations.forEach((alloc, index) => {
        console.log(`\nAllocation ${index + 1}:`);
        console.log(`  Subject: ${alloc.subjectName}`);
        console.log(`  Course: ${alloc.courseName}`);
        console.log(`  Year: ${alloc.year}, Semester: ${alloc.semester}, Section: ${alloc.section}`);
        console.log(`  Faculty ID: ${alloc.facultyId}`);
        console.log(`  Subject ID: ${alloc.subjectId}`);
      });
    } else {
      console.log('\n❌ No allocations found!');
      console.log('\nTrying to find allocations with different faculty ID formats...');
      
      // Try with string ID
      const allocsByString = await db.collection('class_allocations').find({ 
        facultyId: faculty._id.toString() 
      }).toArray();
      console.log(`Allocations with string ID: ${allocsByString.length}`);
      
      // Try with ObjectId
      const allocsByObjectId = await db.collection('class_allocations').find({ 
        facultyId: new ObjectId(faculty._id) 
      }).toArray();
      console.log(`Allocations with ObjectId: ${allocsByObjectId.length}`);
      
      // Show all allocations
      const allAllocations = await db.collection('class_allocations').find({}).toArray();
      console.log(`\nTotal allocations in database: ${allAllocations.length}`);
      
      if (allAllocations.length > 0) {
        console.log('\nSample allocation structure:');
        console.log(JSON.stringify(allAllocations[0], null, 2));
      }
    }
    
  } finally {
    await client.close();
  }
}

verifyAllocation();
