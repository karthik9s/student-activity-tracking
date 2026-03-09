const { MongoClient } = require('mongodb');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';

async function checkDatabaseData() {
    const client = new MongoClient(uri);
    
    try {
        console.log('Connecting to MongoDB...');
        await client.connect();
        console.log('✓ Connected successfully!\n');
        
        const db = client.db('student_tracker');
        
        // Get all collections
        const collections = await db.listCollections().toArray();
        console.log('=== DATABASE COLLECTIONS ===');
        console.log(`Found ${collections.length} collections:\n`);
        
        // Check each collection
        for (const collection of collections) {
            const collName = collection.name;
            const count = await db.collection(collName).countDocuments();
            console.log(`📁 ${collName}: ${count} documents`);
            
            // Show sample data for each collection
            if (count > 0) {
                const sample = await db.collection(collName).findOne();
                console.log(`   Sample: ${JSON.stringify(sample, null, 2).substring(0, 200)}...`);
            }
            console.log('');
        }
        
        // Check specific collections needed for the app
        console.log('\n=== REQUIRED COLLECTIONS CHECK ===');
        const requiredCollections = [
            'users',
            'students', 
            'faculty',
            'courses',
            'subjects',
            'classAllocations',
            'attendance',
            'performance'
        ];
        
        for (const collName of requiredCollections) {
            const exists = collections.some(c => c.name === collName);
            const count = exists ? await db.collection(collName).countDocuments() : 0;
            const status = exists ? (count > 0 ? '✓' : '⚠') : '✗';
            console.log(`${status} ${collName}: ${exists ? `${count} documents` : 'MISSING'}`);
        }
        
        // Check for admin user
        console.log('\n=== ADMIN USER CHECK ===');
        const adminUser = await db.collection('users').findOne({ role: 'ADMIN' });
        if (adminUser) {
            console.log('✓ Admin user found:');
            console.log(`   Email: ${adminUser.email}`);
            console.log(`   Username: ${adminUser.username}`);
            console.log(`   Role: ${adminUser.role}`);
        } else {
            console.log('✗ No admin user found!');
        }
        
        // Check for sample students
        console.log('\n=== SAMPLE USERS CHECK ===');
        const studentCount = await db.collection('users').countDocuments({ role: 'STUDENT' });
        const facultyCount = await db.collection('users').countDocuments({ role: 'FACULTY' });
        console.log(`Students: ${studentCount}`);
        console.log(`Faculty: ${facultyCount}`);
        
        if (studentCount > 0) {
            const sampleStudent = await db.collection('users').findOne({ role: 'STUDENT' });
            console.log(`\nSample student: ${sampleStudent.email}`);
        }
        
        if (facultyCount > 0) {
            const sampleFaculty = await db.collection('users').findOne({ role: 'FACULTY' });
            console.log(`Sample faculty: ${sampleFaculty.email}`);
        }
        
    } catch (error) {
        console.error('❌ Error:', error.message);
        if (error.message.includes('authentication')) {
            console.error('\n⚠ Authentication failed! Check your username and password.');
        }
    } finally {
        await client.close();
        console.log('\n✓ Connection closed');
    }
}

checkDatabaseData();
