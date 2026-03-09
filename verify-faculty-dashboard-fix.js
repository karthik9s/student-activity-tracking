// Verify Faculty Dashboard Fix - Test that allocations appear correctly
const { MongoClient } = require('mongodb');

const MONGO_URI = 'mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0';
const DB_NAME = 'student_tracker';

async function verifyFix() {
    const client = new MongoClient(MONGO_URI);
    
    try {
        await client.connect();
        console.log('Connected to MongoDB\n');
        
        const db = client.db(DB_NAME);
        
        // Get all faculty with their users
        const faculties = await db.collection('faculty').find({}).toArray();
        const users = await db.collection('users').find({}).toArray();
        const subjects = await db.collection('subjects').find({}).toArray();
        
        console.log('=== FACULTY ALLOCATION VERIFICATION ===\n');
        
        for (const faculty of faculties) {
            const user = users.find(u => u._id.toString() === faculty.userId);
            
            // Query allocations by Faculty ID (correct way)
            const allocationsByFacultyId = await db.collection('class_allocations')
                .find({ facultyId: faculty._id.toString() })
                .toArray();
            
            // Query allocations by User ID (old buggy way)
            const allocationsByUserId = await db.collection('class_allocations')
                .find({ facultyId: faculty.userId })
                .toArray();
            
            console.log(`Faculty: ${faculty.firstName} ${faculty.lastName}`);
            console.log(`  Email: ${faculty.email}`);
            console.log(`  Faculty ID: ${faculty._id}`);
            console.log(`  User ID: ${faculty.userId}`);
            if (user) {
                console.log(`  User Email: ${user.email}`);
            }
            console.log(`  Allocations (by Faculty ID): ${allocationsByFacultyId.length} ✓`);
            console.log(`  Allocations (by User ID): ${allocationsByUserId.length} ${allocationsByUserId.length > 0 ? '⚠️' : '✓'}`);
            
            if (allocationsByFacultyId.length > 0) {
                console.log(`  Subjects assigned:`);
                for (const alloc of allocationsByFacultyId) {
                    const subject = subjects.find(s => s._id.toString() === alloc.subjectId);
                    console.log(`    - ${subject ? subject.name : 'Unknown'} (Year ${alloc.year}, Section ${alloc.section})`);
                }
            }
            console.log('');
        }
        
        console.log('=== SUMMARY ===\n');
        const totalAllocations = await db.collection('class_allocations').countDocuments();
        console.log(`Total allocations in database: ${totalAllocations}`);
        
        const facultyWithAllocations = faculties.filter(f => 
            allocationsByFacultyId = db.collection('class_allocations')
                .find({ facultyId: f._id.toString() })
                .toArray().length > 0
        );
        
        console.log(`Faculty with allocations: ${faculties.filter(f => {
            const count = db.collection('class_allocations').countDocuments({ facultyId: f._id.toString() });
            return count > 0;
        }).length}/${faculties.length}`);
        
        console.log('\n✓ Verification complete!');
        console.log('\nTo test the fix:');
        console.log('1. Restart your backend: cd backend && mvn spring-boot:run');
        console.log('2. Log in as any faculty member (e.g., rajesh.kumar@cvr.ac.in)');
        console.log('3. Navigate to Faculty Dashboard');
        console.log('4. You should see 2 class allocations displayed');
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

verifyFix();
