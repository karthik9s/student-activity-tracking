// Script to check all faculty and their allocations
const { MongoClient, ObjectId } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

async function checkAllFaculty() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB\n');
    
    const db = client.db('student_tracker');
    
    const allFaculty = await db.collection('faculty').find({}).toArray();
    console.log(`Total Faculty: ${allFaculty.length}\n`);
    
    for (const faculty of allFaculty) {
      const user = await db.collection('users').findOne({ _id: new ObjectId(faculty.userId) });
      const allocations = await db.collection('classAllocations').find({ 
        facultyId: faculty._id.toString() 
      }).toArray();
      
      console.log(`${faculty.firstName} ${faculty.lastName}`);
      console.log(`  Email: ${faculty.email}`);
      console.log(`  User ID: ${faculty.userId}`);
      console.log(`  Faculty ID: ${faculty._id}`);
      console.log(`  Allocations: ${allocations.length}`);
      
      if (allocations.length > 0) {
        for (const alloc of allocations) {
          const subject = await db.collection('subjects').findOne({ _id: new ObjectId(alloc.subjectId) });
          console.log(`    - ${subject ? subject.name : 'Unknown Subject'}`);
        }
      }
      console.log('');
    }
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
  }
}

checkAllFaculty();
