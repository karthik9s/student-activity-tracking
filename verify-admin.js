const { MongoClient } = require('mongodb');
const bcrypt = require('bcryptjs');

const uri = "mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0";

async function verifyAdmin() {
    const client = new MongoClient(uri);
    
    try {
        await client.connect();
        console.log("Connected to MongoDB");
        
        const database = client.db('student_tracker');
        const users = database.collection('users');
        
        // Find the admin user
        const admin = await users.findOne({ email: 'admin@college.com' });
        
        if (!admin) {
            console.log("Admin user NOT found!");
            return;
        }
        
        console.log("\n=== Admin User Found ===");
        console.log("Email:", admin.email);
        console.log("Role:", admin.role);
        console.log("IsActive:", admin.isActive);
        console.log("Password Hash:", admin.password);
        
        // Test password
        const testPassword = 'admin123';
        const isMatch = await bcrypt.compare(testPassword, admin.password);
        console.log("\nPassword 'admin123' matches:", isMatch);
        
        if (!isMatch) {
            console.log("\n⚠️  Password doesn't match! Updating password...");
            
            // Generate new hash with 10 rounds (Spring Security default)
            const newHash = await bcrypt.hash(testPassword, 10);
            
            await users.updateOne(
                { email: 'admin@college.com' },
                { 
                    $set: { 
                        password: newHash,
                        updatedAt: new Date()
                    } 
                }
            );
            
            console.log("✅ Password updated successfully!");
            console.log("New hash:", newHash);
        }
        
    } catch (error) {
        console.error("Error:", error);
    } finally {
        await client.close();
    }
}

verifyAdmin();
