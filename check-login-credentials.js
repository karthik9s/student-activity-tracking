// Check login credentials for faculty members
const { MongoClient } = require('mongodb');

const MONGO_URI = 'mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0';
const DB_NAME = 'student_tracker';

async function checkCredentials() {
    const client = new MongoClient(MONGO_URI);
    
    try {
        await client.connect();
        console.log('Connected to MongoDB\n');
        
        const db = client.db(DB_NAME);
        
        // Get all users with FACULTY role
        const users = await db.collection('users').find({ role: 'FACULTY' }).toArray();
        console.log(`Found ${users.length} faculty users\n`);
        
        console.log('=== FACULTY LOGIN CREDENTIALS ===\n');
        
        for (const user of users) {
            console.log(`Email: ${user.email}`);
            console.log(`  User ID: ${user._id}`);
            console.log(`  Role: ${user.role}`);
            console.log(`  Active: ${user.isActive}`);
            console.log(`  Password (hashed): ${user.password ? 'EXISTS' : 'MISSING'}`);
            console.log(`  Note: Password should be "password123" (hashed with BCrypt)`);
            console.log('');
        }
        
        // Check if passwords are properly hashed
        console.log('=== PASSWORD CHECK ===\n');
        const bcrypt = require('bcryptjs');
        
        for (const user of users) {
            if (user.password) {
                // Check if it looks like a BCrypt hash
                const isBcryptHash = user.password.startsWith('$2a$') || user.password.startsWith('$2b$');
                console.log(`${user.email}: ${isBcryptHash ? '✓ BCrypt hash' : '⚠️  NOT a BCrypt hash'}`);
                
                if (!isBcryptHash) {
                    console.log(`  Current value: ${user.password}`);
                }
            } else {
                console.log(`${user.email}: ⚠️  NO PASSWORD`);
            }
        }
        
        console.log('\n=== LOGIN INSTRUCTIONS ===\n');
        console.log('To log in as a faculty member:');
        console.log('1. Use any of the emails listed above');
        console.log('2. Password: password123');
        console.log('3. If login fails, passwords may need to be reset');
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

checkCredentials();
