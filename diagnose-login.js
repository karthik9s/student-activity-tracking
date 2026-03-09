const { MongoClient } = require('mongodb');
const bcrypt = require('bcryptjs');

const BACKEND_URI = "mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0";

async function diagnose() {
    const client = new MongoClient(BACKEND_URI);
    
    try {
        await client.connect();
        console.log("✅ Connected to MongoDB\n");
        
        const db = client.db('student_tracker');
        const users = db.collection('users');
        
        console.log("=== Diagnostic Report ===\n");
        
        // 1. Check total users
        const totalUsers = await users.countDocuments();
        console.log(`1. Total users in database: ${totalUsers}`);
        
        // 2. Check admin user
        const admin = await users.findOne({ email: 'admin@cvr.ac.in' });
        if (admin) {
            console.log("\n2. ✅ Admin user found:");
            console.log(`   Email: ${admin.email}`);
            console.log(`   Role: ${admin.role}`);
            console.log(`   IsActive: ${admin.isActive} (type: ${typeof admin.isActive})`);
            console.log(`   Has password: ${admin.password ? 'Yes' : 'No'}`);
            
            // Test password
            if (admin.password) {
                const passwordMatch = await bcrypt.compare('admin123', admin.password);
                console.log(`   Password 'admin123' matches: ${passwordMatch ? '✅ YES' : '❌ NO'}`);
            }
        } else {
            console.log("\n2. ❌ Admin user NOT found!");
        }
        
        // 3. Check if backend can find user with isActive filter
        const adminWithActive = await users.findOne({ email: 'admin@cvr.ac.in', isActive: true });
        console.log(`\n3. User found with isActive=true filter: ${adminWithActive ? '✅ YES' : '❌ NO'}`);
        
        // 4. List all user emails and roles
        console.log("\n4. All users in database:");
        const allUsers = await users.find({}, { projection: { email: 1, role: 1, isActive: 1 } }).limit(5).toArray();
        allUsers.forEach(u => {
            console.log(`   - ${u.email} (${u.role}, active: ${u.isActive})`);
        });
        console.log(`   ... and ${totalUsers - 5} more`);
        
        console.log("\n=== Recommendations ===");
        if (!admin) {
            console.log("❌ Admin user is missing. Run: node seed-complete-demo-data.js");
        } else if (!adminWithActive) {
            console.log("❌ Admin user exists but isActive filter fails. Check data type.");
        } else {
            console.log("✅ Database looks good!");
            console.log("\nIf login still fails, check:");
            console.log("1. Backend logs for authentication errors");
            console.log("2. Frontend console (F12) for network errors");
            console.log("3. CORS settings in backend");
            console.log("4. Make sure you're using: admin@cvr.ac.in / admin123");
        }
        
    } catch (error) {
        console.error("❌ Error:", error.message);
    } finally {
        await client.close();
    }
}

diagnose();
