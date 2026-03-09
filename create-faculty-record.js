const { MongoClient, ObjectId } = require('mongodb');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';

async function createFacultyRecord() {
  const client = new MongoClient(uri);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    
    // Get the faculty user
    const user = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    if (!user) {
      console.log('User not found!');
      return;
    }
    
    console.log('Found user:', user.email, 'with ID:', user._id.toString());
    
    // Check if faculty already exists
    const existingFaculty = await db.collection('faculties').findOne({ userId: user._id.toString() });
    if (existingFaculty) {
      console.log('Faculty record already exists!');
      return;
    }
    
    // Create faculty record
    const faculty = {
      _id: new ObjectId(),
      userId: user._id.toString(),
      employeeId: 'FAC001',
      firstName: 'Rajesh',
      lastName: 'Kumar',
      email: 'rajesh.kumar@cvr.ac.in',
      phone: '+91-9876543210',
      department: 'Computer Science',
      designation: 'Assistant Professor',
      profileImage: null,
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date(),
      deletedAt: null
    };
    
    const result = await db.collection('faculties').insertOne(faculty);
    console.log('\n✅ Faculty record created successfully!');
    console.log('Faculty ID:', faculty._id.toString());
    console.log('Employee ID:', faculty.employeeId);
    console.log('Name:', faculty.firstName, faculty.lastName);
    console.log('User ID:', faculty.userId);
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
  }
}

createFacultyRecord();
