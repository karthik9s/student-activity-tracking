const { MongoClient } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

async function checkData() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    const db = client.db('student_tracker');
    
    console.log('=== Database Check ===\n');
    
    const rajesh = await db.collection('faculty').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    console.log('Rajesh Kumar:', rajesh ? `Found - Course: ${rajesh.courseName}` : 'Not found');
    
    const subjectCount = await db.collection('subjects').countDocuments();
    console.log(`\nTotal subjects: ${subjectCount}`);
    
    const sampleSubject = await db.collection('subjects').findOne({ semester: 1 });
    console.log('\nSample subject structure:');
    console.log(JSON.stringify(sampleSubject, null, 2));
    
    const allocations = await db.collection('class_allocations').find({ facultyId: rajesh?._id }).toArray();
    console.log(`\nRajesh's allocations: ${allocations.length}`);
    if (allocations.length > 0) {
      console.log('Subjects assigned:');
      allocations.forEach(a => console.log(`  - ${a.subjectName}`));
    }
    
  } finally {
    await client.close();
  }
}

checkData();
