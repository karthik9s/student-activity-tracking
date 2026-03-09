const { MongoClient } = require('mongodb');
const bcrypt = require('bcryptjs');

// MongoDB connection string from your application.properties
const uri = "mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0";

async function createAdminUser() {
    const client = new MongoClient(uri);
    
    try {
        await client.connect();
        console.log("Connected to MongoDB");
        
        const database = client.db('student_tracker');
        const users = database.collection('users');
        
        // Check if admin already exists
        const existingAdmin = await users.findOne({ email: 'admin@college.com' });
        
        if (existingAdmin) {
            console.log("Admin user already exists!");
            console.log("Email: admin@college.com");
            return;
        }
        
        // Hash the password (same way Spring Security does it with BCrypt)
        const hashedPassword = await bcrypt.hash('admin123', 10);
        
        // Create admin user
        const adminUser = {
            email: 'admin@college.com',
            password: hashedPassword,
            role: 'ROLE_ADMIN',
            isActive: true,
            createdAt: new Date(),
            updatedAt: new Date()
        };
        
        const result = await users.insertOne(adminUser);
        console.log("Admin user created successfully!");
        console.log("Email: admin@college.com");
        console.log("Password: admin123");
        console.log("User ID:", result.insertedId);
        
    } catch (error) {
        console.error("Error creating admin user:", error);
    } finally {
        await client.close();
    }
}

createAdminUser();
