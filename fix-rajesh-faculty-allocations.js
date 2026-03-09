const { MongoClient } = require('mongodb');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';

async function fixRajeshAllocations() {
  const client = new MongoClient(uri);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    
    // Get Rajesh's user ID
    const user = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    const oldFacultyId = user._id.toString();
    
    // Get Rajesh's faculty record
    const faculty = await db.collection('faculties').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    const newFacultyId = faculty._id.toString();
    
    console.log('Old Faculty ID (User ID):', oldFacultyId);
    console.log('New Faculty ID (Faculty Record):', newFacultyId);
    
    // Update all allocations
    const result = await db.collection('classAllocations').updateMany(
      { facultyId: oldFacultyId },
      { $set: { facultyId: newFacultyId } }
    );
    
    console.log('\n✅ Updated', result.modifiedCount, 'allocations');
    
    // Verify
    const updatedAllocations = await db.collection('classAllocations').find({ 
      facultyId: newFacultyId 
    }).toArray();
    
    console.log('\n=== UPDATED ALLOCATIONS ===');
    updatedAllocations.forEach(a => {
      console.log(`Course: ${a.courseName || 'N/A'}, Subject: ${a.subjectName || 'N/A'}, Year: ${a.year}, Section: ${a.section}`);
    });
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
  }
}

fixRajeshAllocations();
