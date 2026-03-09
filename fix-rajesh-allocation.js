const { MongoClient } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

async function fixAllocation() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    const db = client.db('student_tracker');
    
    console.log('=== Fixing Rajesh Kumar\'s Class Allocation ===\n');
    
    // Get Rajesh's user and faculty records
    const user = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    const faculty = await db.collection('faculty').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    
    if (!user || !faculty) {
      console.log('❌ User or faculty record not found');
      return;
    }
    
    console.log(`User ID: ${user._id}`);
    console.log(`Faculty ID: ${faculty._id}`);
    console.log(`Faculty userId field: ${faculty.userId}\n`);
    
    // Update all allocations to use userId instead of facultyId
    const result = await db.collection('class_allocations').updateMany(
      { facultyId: faculty._id.toString() },
      { $set: { facultyId: user._id.toString() } }
    );
    
    console.log(`✓ Updated ${result.modifiedCount} allocation(s)`);
    
    // Verify the update
    const allocations = await db.collection('class_allocations').find({ 
      facultyId: user._id.toString() 
    }).toArray();
    
    console.log(`\n✓ Verified: ${allocations.length} allocation(s) now use user ID`);
    
    if (allocations.length > 0) {
      console.log('\nAllocation details:');
      allocations.forEach(alloc => {
        console.log(`  - ${alloc.subjectName} (Year ${alloc.year}, Sem ${alloc.semester}, Sec ${alloc.section})`);
      });
    }
    
    console.log('\n✅ Done! Rajesh can now see his allocations in the dropdown.');
    
  } finally {
    await client.close();
  }
}

fixAllocation();
