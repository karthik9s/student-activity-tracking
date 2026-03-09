const { MongoClient } = require('mongodb');

const uri = "mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0";

async function checkUsers() {
    const client = new MongoClient(uri);
    
    try {
        await client.connect();
        console.log("Connected to MongoDB");
        
        const database = client.db('student_tracker');
        const users = database.collection('users');
        
        // Count all users
        const count = await users.countDocuments();
        console.log(`\nTotal users in database: ${count}`);
        
        // Find all users
        const allUsers = await users.find({}).toArray();
        
        console.log("\n=== All Users ===");
        allUsers.forEach(user => {
            console.log(`\nEmail: ${user.email}`);
            console.log(`Role: ${user.role}`);
            console.log(`IsActive: ${user.isActive}`);
            console.log(`Password hash: ${user.password ? user.password.substring(0, 20) + '...' : 'NONE'}`);
        });
        
    } catch (error) {
        console.error("Error:", error);
    } finally {
        await client.close();
    }
}

checkUsers();
