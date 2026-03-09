const { MongoClient } = require('mongodb');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';

async function checkUserRoles() {
    const client = new MongoClient(uri);
    
    try {
        await client.connect();
        const db = client.db('student_tracker');
        
        console.log('=== ALL USERS ===\n');
        const users = await db.collection('users').find({}).toArray();
        
        users.forEach(user => {
            console.log(`Email: ${user.email}`);
            console.log(`Role: ${user.role}`);
            console.log(`Active: ${user.isActive}`);
            console.log('---');
        });
        
        console.log('\n=== ROLE SUMMARY ===');
        const roleAdmin = await db.collection('users').countDocuments({ role: 'ROLE_ADMIN' });
        const roleStudent = await db.collection('users').countDocuments({ role: 'ROLE_STUDENT' });
        const roleFaculty = await db.collection('users').countDocuments({ role: 'ROLE_FACULTY' });
        
        const admin = await db.collection('users').countDocuments({ role: 'ADMIN' });
        const student = await db.collection('users').countDocuments({ role: 'STUDENT' });
        const faculty = await db.collection('users').countDocuments({ role: 'FACULTY' });
        
        console.log(`ROLE_ADMIN: ${roleAdmin}`);
        console.log(`ROLE_STUDENT: ${roleStudent}`);
        console.log(`ROLE_FACULTY: ${roleFaculty}`);
        console.log(`\nADMIN: ${admin}`);
        console.log(`STUDENT: ${student}`);
        console.log(`FACULTY: ${faculty}`);
        
        console.log('\n=== DEMO CREDENTIALS TEST ===');
        const adminUser = await db.collection('users').findOne({ email: 'admin@cvr.ac.in' });
        if (adminUser) {
            console.log('✓ Admin user exists');
            console.log(`  Email: ${adminUser.email}`);
            console.log(`  Role: ${adminUser.role}`);
        }
        
        const facultyUser = await db.collection('users').findOne({ email: 'rajesh.kumar@cvr.ac.in' });
        if (facultyUser) {
            console.log('✓ Faculty user (rajesh.kumar) exists');
            console.log(`  Email: ${facultyUser.email}`);
            console.log(`  Role: ${facultyUser.role}`);
        }
        
        const studentUser = await db.collection('users').findOne({ email: 'cse21a001@cvr.ac.in' });
        if (studentUser) {
            console.log('✓ Student user (cse21a001) exists');
            console.log(`  Email: ${studentUser.email}`);
            console.log(`  Role: ${studentUser.role}`);
        }
        
    } catch (error) {
        console.error('Error:', error.message);
    } finally {
        await client.close();
    }
}

checkUserRoles();
