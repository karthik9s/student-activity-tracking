const { MongoClient } = require('mongodb');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';

async function updateAllocations() {
  const client = new MongoClient(uri);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    
    // Get Rajesh's faculty record
    const faculty = await db.collection('faculties').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    const newFacultyId = faculty._id.toString();
    
    console.log('Rajesh Faculty ID:', newFacultyId);
    
    // Update ALL allocations to use Rajesh's faculty ID
    const result = await db.collection('classAllocations').updateMany(
      {},  // Update all allocations
      { $set: { facultyId: newFacultyId } }
    );
    
    console.log('\n✅ Updated', result.modifiedCount, 'allocations');
    
    // Verify
    const allocations = await db.collection('classAllocations').find({ 
      facultyId: newFacultyId,
      isActive: true 
    }).toArray();
    
    console.log('\n=== RAJESH\'S ALLOCATIONS ===');
    console.log('Total:', allocations.length);
    allocations.slice(0, 5).forEach(a => {
      console.log(`- Year ${a.year} Section ${a.section}, Semester ${a.semester}`);
    });
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
  }
}

updateAllocations();
