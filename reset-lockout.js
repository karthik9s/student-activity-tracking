const { MongoClient } = require('mongodb');

const uri = 'mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0';

async function resetLockout() {
  const client = new MongoClient(uri);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    
    // Clear all brute force protection records
    const result = await db.collection('brute_force_attempts').deleteMany({});
    
    console.log(`✅ Cleared ${result.deletedCount} brute force protection records`);
    console.log('All accounts are now unlocked!');
    console.log('\nYou can now login with:');
    console.log('  Email: admin@college.com');
    console.log('  Password: admin123');
    
  } catch (error) {
    console.error('Error resetting lockout:', error);
  } finally {
    await client.close();
    console.log('\nDisconnected from MongoDB');
  }
}

resetLockout();
