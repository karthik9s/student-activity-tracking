const { MongoClient } = require('mongodb');
const bcrypt = require('bcryptjs');

const uri = "mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0";

async function testUserQuery() {
    const client = new MongoClient(uri);
    
    try {
        await client.connect();
        console.log("Connected to MongoDB\n");
        
        const database = client.db('student_tracker');
        const users = database.collection('users');
        
        // Test the exact query Spring uses
        const email = 'admin@cvr.ac.in';
        console.log(`Testing query: { email: '${email}', isActive: true }\n`);
        
        const user = await users.findOne({ email: email, isActive: true });
        
        if (!user) {
            console.log("❌ User NOT found with isActive: true");
            
            // Try without isActive
            const userWithoutActive = await users.findOne({ email: email });
            if (userWithoutActive) {
                console.log("\n✅ User found WITHOUT isActive filter:");
                console.log("Email:", userWithoutActive.email);
                console.log("Role:", userWithoutActive.role);
                console.log("IsActive:", userWithoutActive.isActive);
                console.log("IsActive type:", typeof userWithoutActive.isActive);
            }
        } else {
            console.log("✅ User found!");
            console.log("Email:", user.email);
            console.log("Role:", user.role);
            console.log("IsActive:", user.isActive);
            
            // Test password
            const testPassword = 'admin123';
            const isMatch = await bcrypt.compare(testPassword, user.password);
            console.log(`\nPassword '${testPassword}' matches:`, isMatch);
        }
        
    } catch (error) {
        console.error("Error:", error);
    } finally {
        await client.close();
    }
}

testUserQuery();
