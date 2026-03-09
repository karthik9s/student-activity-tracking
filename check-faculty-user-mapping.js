const { MongoClient } = require('mongodb');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';

async function checkFacultyUserMapping() {
  const client = new MongoClient(uri);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    
    // Get the faculty user (Rajesh Kumar)
    const user = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    console.log('\n=== USER RECORD ===');
    console.log('User ID:', user._id.toString());
    console.log('Email:', user.email);
    console.log('Role:', user.role);
    
    // Try to find faculty by user ID
    const facultyByUserId = await db.collection('faculties').findOne({ userId: user._id.toString() });
    console.log('\n=== FACULTY BY USER ID ===');
    if (facultyByUserId) {
      console.log('Faculty ID:', facultyByUserId._id.toString());
      console.log('Name:', facultyByUserId.firstName, facultyByUserId.lastName);
      console.log('User ID field:', facultyByUserId.userId);
    } else {
      console.log('NOT FOUND by userId');
    }
    
    // Try to find faculty by email
    const facultyByEmail = await db.collection('faculties').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    console.log('\n=== FACULTY BY EMAIL ===');
    if (facultyByEmail) {
      console.log('Faculty ID:', facultyByEmail._id.toString());
      console.log('Name:', facultyByEmail.firstName, facultyByEmail.lastName);
      console.log('User ID field:', facultyByEmail.userId);
    } else {
      console.log('NOT FOUND by email');
    }
    
    // List all faculties
    const allFaculties = await db.collection('faculties').find({}).toArray();
    console.log('\n=== ALL FACULTIES ===');
    allFaculties.forEach(f => {
      console.log(`ID: ${f._id.toString()}, Name: ${f.firstName} ${f.lastName}, Email: ${f.email}, UserID: ${f.userId}`);
    });
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
  }
}

checkFacultyUserMapping();
