// Script to verify faculty User IDs and Faculty IDs for testing
const { MongoClient, ObjectId } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

async function verifyFacultyIds() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB\n');
    
    const db = client.db('student_tracker');
    
    // Check Rajesh Kumar
    console.log('=== Rajesh Kumar ===');
    const rajeshUser = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    if (rajeshUser) {
      console.log(`User ID: ${rajeshUser._id}`);
      const rajeshFaculty = await db.collection('faculty').findOne({ userId: rajeshUser._id.toString() });
      if (rajeshFaculty) {
        console.log(`Faculty ID: ${rajeshFaculty._id}`);
        
        // Check allocations
        const allocations = await db.collection('classAllocations').find({ 
          facultyId: rajeshFaculty._id.toString() 
        }).toArray();
        console.log(`Allocations: ${allocations.length}`);
        if (allocations.length > 0) {
          for (const alloc of allocations) {
            const subject = await db.collection('subjects').findOne({ _id: new ObjectId(alloc.subjectId) });
            console.log(`  - ${subject ? subject.name : 'Unknown Subject'}`);
          }
        }
      } else {
        console.log('Faculty record not found!');
      }
    } else {
      console.log('User not found!');
    }
    
    // Check Priya Sharma
    console.log('\n=== Priya Sharma ===');
    const priyaUser = await db.collection('users').findOne({ email: 'priya.sharma@cvr.ac.in' });
    if (priyaUser) {
      console.log(`User ID: ${priyaUser._id}`);
      const priyaFaculty = await db.collection('faculty').findOne({ userId: priyaUser._id.toString() });
      if (priyaFaculty) {
        console.log(`Faculty ID: ${priyaFaculty._id}`);
        
        // Check allocations
        const allocations = await db.collection('classAllocations').find({ 
          facultyId: priyaFaculty._id.toString() 
        }).toArray();
        console.log(`Allocations: ${allocations.length}`);
        if (allocations.length > 0) {
          for (const alloc of allocations) {
            const subject = await db.collection('subjects').findOne({ _id: new ObjectId(alloc.subjectId) });
            console.log(`  - ${subject ? subject.name : 'Unknown Subject'}`);
          }
        }
      } else {
        console.log('Faculty record not found!');
      }
    } else {
      console.log('User not found!');
    }
    
    // Check for faculty with no allocations
    console.log('\n=== Faculty with No Allocations ===');
    const allFaculty = await db.collection('faculty').find({}).toArray();
    for (const faculty of allFaculty) {
      const allocations = await db.collection('classAllocations').find({ 
        facultyId: faculty._id.toString() 
      }).toArray();
      if (allocations.length === 0) {
        const user = await db.collection('users').findOne({ _id: new ObjectId(faculty.userId) });
        console.log(`${faculty.firstName} ${faculty.lastName} (${user ? user.email : 'No user'})`);
        console.log(`  User ID: ${faculty.userId}`);
        console.log(`  Faculty ID: ${faculty._id}`);
        break; // Just show one example
      }
    }
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
  }
}

verifyFacultyIds();
