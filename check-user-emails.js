// Script to check user emails in database
const { MongoClient } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

async function checkUserEmails() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB\n');
    
    const db = client.db('student_tracker');
    
    // Check Rajesh Kumar
    const rajesh = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
    console.log('Rajesh Kumar user:', rajesh ? 'Found' : 'Not found');
    if (rajesh) {
      console.log(`  Email: ${rajesh.email}`);
      console.log(`  Username: ${rajesh.username}`);
      console.log(`  Role: ${rajesh.role}`);
      console.log(`  Active: ${rajesh.isActive}`);
    }
    
    // Check Priya Sharma
    const priya = await db.collection('users').findOne({ email: 'priya.sharma@cvr.ac.in' });
    console.log('\nPriya Sharma user:', priya ? 'Found' : 'Not found');
    if (priya) {
      console.log(`  Email: ${priya.email}`);
      console.log(`  Username: ${priya.username}`);
      console.log(`  Role: ${priya.role}`);
      console.log(`  Active: ${priya.isActive}`);
    }
    
    // Check Anita Desai
    const anita = await db.collection('users').findOne({ email: 'anita.desai@cvr.ac.in' });
    console.log('\nAnita Desai user:', anita ? 'Found' : 'Not found');
    if (anita) {
      console.log(`  Email: ${anita.email}`);
      console.log(`  Username: ${anita.username}`);
      console.log(`  Role: ${anita.role}`);
      console.log(`  Active: ${anita.isActive}`);
    }
    
    // List all faculty users
    console.log('\n=== All Faculty Users ===');
    const facultyUsers = await db.collection('users').find({ role: 'ROLE_FACULTY' }).toArray();
    facultyUsers.forEach(user => {
      console.log(`${user.username} - ${user.email}`);
    });
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await client.close();
  }
}

checkUserEmails();
