const { MongoClient } = require('mongodb');
const bcrypt = require('bcryptjs');

const uri = "mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0";

async function fixAdminPassword() {
    const client = new MongoClient(uri);
    
    try {
        await client.connect();
        console.log("Connected to MongoDB");
        
        const database = client.db('student_tracker');
        const users = database.collection('users');
        
        // Generate password hash with 12 rounds to match Spring Security config
        const password = 'admin123';
        const hashedPassword = await bcrypt.hash(password, 12);
        
        console.log("Generated password hash with 12 rounds:", hashedPassword);
        
        // Update the admin user
        const result = await users.updateOne(
            { email: 'admin@college.com' },
            { 
                $set: { 
                    password: hashedPassword,
                    updatedAt: new Date()
                } 
            }
        );
        
        if (result.matchedCount === 0) {
            console.log("❌ Admin user not found!");
        } else {
            console.log("✅ Admin password updated successfully!");
            console.log("Email: admin@college.com");
            console.log("Password: admin123");
        }
        
    } catch (error) {
        console.error("Error:", error);
    } finally {
        await client.close();
    }
}

fixAdminPassword();
