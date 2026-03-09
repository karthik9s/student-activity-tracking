const { MongoClient } = require('mongodb');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';

async function checkRajeshAllocations() {
  const client = new MongoClient(uri);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    
    // Get Rajesh's faculty record
    const faculty = await db.collection('faculties').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    if (!faculty) {
      console.log('Faculty record not found!');
      return;
    }
    
    console.log('\n=== FACULTY RECORD ===');
    console.log('Faculty ID:', faculty._id.toString());
    console.log('Name:', faculty.firstName, faculty.lastName);
    console.log('User ID:', faculty.userId);
    
    // Check allocations by faculty ID
    const allocations = await db.collection('classAllocations').find({ 
      facultyId: faculty._id.toString(),
      isActive: true 
    }).toArray();
    
    console.log('\n=== CLASS ALLOCATIONS ===');
    console.log('Total allocations:', allocations.length);
    
    if (allocations.length === 0) {
      console.log('NO ALLOCATIONS FOUND!');
      console.log('\nChecking all allocations in database...');
      const allAllocations = await db.collection('classAllocations').find({}).toArray();
      console.log('Total allocations in DB:', allAllocations.length);
      
      if (allAllocations.length > 0) {
        console.log('\nSample allocation:');
        console.log(JSON.stringify(allAllocations[0], null, 2));
      }
    } else {
      allocations.forEach(a => {
        console.log(`\nAllocation ID: ${a._id.toString()}`);
        console.log(`Course: ${a.courseName}`);
        console.log(`Subject: ${a.subjectName}`);
        console.log(`Year: ${a.year}, Section: ${a.section}`);
      });
    }
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
  }
}

checkRajeshAllocations();
