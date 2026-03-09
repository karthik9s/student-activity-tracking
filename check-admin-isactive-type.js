// Check the exact type and value of isActive field for admin user
const { MongoClient } = require('mongodb');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';
const dbName = 'student_tracker';

async function checkIsActiveType() {
    const client = new MongoClient(uri);
    
    try {
        await client.connect();
        console.log('Connected to MongoDB\n');
        
        const db = client.db(dbName);
        const usersCollection = db.collection('users');
        
        const adminUser = await usersCollection.findOne({ email: 'admin@cvr.ac.in' });
        
        if (!adminUser) {
            console.log('❌ Admin user not found');
            return;
        }
        
        console.log('=== Admin User isActive Field Analysis ===\n');
        console.log('Raw isActive value:', adminUser.isActive);
        console.log('Type of isActive:', typeof adminUser.isActive);
        console.log('isActive === true:', adminUser.isActive === true);
        console.log('isActive == true:', adminUser.isActive == true);
        console.log('Boolean(isActive):', Boolean(adminUser.isActive));
        
        // Check if it's a string
        if (typeof adminUser.isActive === 'string') {
            console.log('\n⚠️  WARNING: isActive is stored as STRING instead of BOOLEAN');
            console.log('String value:', `"${adminUser.isActive}"`);
            console.log('This will cause findByEmailAndIsActiveTrue() to fail!');
        }
        
        // Check if it's undefined or null
        if (adminUser.isActive === undefined) {
            console.log('\n❌ ERROR: isActive field is UNDEFINED');
        }
        if (adminUser.isActive === null) {
            console.log('\n❌ ERROR: isActive field is NULL');
        }
        
        console.log('\n=== Full Admin User Document ===');
        console.log(JSON.stringify(adminUser, null, 2));
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

checkIsActiveType();
