// Check which faculty members have allocations and verify User ID mapping
const { MongoClient } = require('mongodb');

const MONGO_URI = 'mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0';
const DB_NAME = 'student_tracker';

async function checkMapping() {
    const client = new MongoClient(MONGO_URI);
    
    try {
        await client.connect();
        console.log('Connected to MongoDB\n');
        
        const db = client.db(DB_NAME);
        
        // Get all allocations
        const allocations = await db.collection('class_allocations').find({}).toArray();
        console.log(`Total allocations: ${allocations.length}\n`);
        
        // Get all faculty
        const faculties = await db.collection('faculty').find({}).toArray();
        console.log(`Total faculty: ${faculties.length}\n`);
        
        // Get all users
        const users = await db.collection('users').find({}).toArray();
        
        // Map allocations to faculty
        console.log('=== ALLOCATION TO FACULTY MAPPING ===\n');
        
        for (const allocation of allocations) {
            const faculty = faculties.find(f => f._id.toString() === allocation.facultyId);
            
            if (faculty) {
                const user = users.find(u => u._id.toString() === faculty.userId);
                
                console.log(`Allocation ID: ${allocation._id}`);
                console.log(`  Faculty ID: ${allocation.facultyId}`);
                console.log(`  Faculty Name: ${faculty.firstName} ${faculty.lastName}`);
                console.log(`  Faculty Email: ${faculty.email}`);
                console.log(`  Faculty User ID: ${faculty.userId}`);
                if (user) {
                    console.log(`  User Email: ${user.email}`);
                    console.log(`  User Role: ${user.role}`);
                } else {
                    console.log(`  ⚠️  WARNING: No user found with ID ${faculty.userId}`);
                }
                console.log(`  Subject ID: ${allocation.subjectId}`);
                console.log(`  Year: ${allocation.year}, Section: ${allocation.section}`);
                console.log('');
            } else {
                console.log(`⚠️  Allocation ${allocation._id} has no matching faculty (facultyId: ${allocation.facultyId})`);
                console.log('');
            }
        }
        
        // Show faculty members WITHOUT allocations
        console.log('=== FACULTY WITHOUT ALLOCATIONS ===\n');
        for (const faculty of faculties) {
            const hasAllocations = allocations.some(a => a.facultyId === faculty._id.toString());
            if (!hasAllocations) {
                const user = users.find(u => u._id.toString() === faculty.userId);
                console.log(`Faculty: ${faculty.firstName} ${faculty.lastName}`);
                console.log(`  Email: ${faculty.email}`);
                console.log(`  Faculty ID: ${faculty._id}`);
                console.log(`  User ID: ${faculty.userId}`);
                if (user) {
                    console.log(`  User Email: ${user.email}`);
                } else {
                    console.log(`  ⚠️  WARNING: No user found`);
                }
                console.log('');
            }
        }
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

checkMapping();
