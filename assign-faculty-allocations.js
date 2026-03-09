// Assign class allocations to current faculty members
const { MongoClient, ObjectId } = require('mongodb');

const MONGO_URI = 'mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0';
const DB_NAME = 'student_tracker';

async function assignAllocations() {
    const client = new MongoClient(MONGO_URI);
    
    try {
        await client.connect();
        console.log('Connected to MongoDB\n');
        
        const db = client.db(DB_NAME);
        
        // Get faculty and subjects
        const faculties = await db.collection('faculty').find({}).toArray();
        const subjects = await db.collection('subjects').find({}).toArray();
        const courses = await db.collection('courses').find({}).toArray();
        
        console.log(`Found ${faculties.length} faculty members`);
        console.log(`Found ${subjects.length} subjects`);
        console.log(`Found ${courses.length} courses\n`);
        
        if (subjects.length === 0) {
            console.log('⚠️  No subjects found. Please create subjects first.');
            return;
        }
        
        if (courses.length === 0) {
            console.log('⚠️  No courses found. Please create courses first.');
            return;
        }
        
        // Clear old invalid allocations
        const deleteResult = await db.collection('class_allocations').deleteMany({});
        console.log(`Deleted ${deleteResult.deletedCount} old allocations\n`);
        
        // Assign allocations to faculty members
        console.log('=== ASSIGNING ALLOCATIONS ===\n');
        
        const allocations = [];
        const sections = ['A', 'B', 'C'];
        
        // Assign 2-3 subjects to each faculty member
        for (let i = 0; i < faculties.length && i < subjects.length; i++) {
            const faculty = faculties[i];
            const course = courses[0]; // Use first course
            
            // Assign 2 subjects to this faculty
            for (let j = 0; j < 2 && (i * 2 + j) < subjects.length; j++) {
                const subject = subjects[i * 2 + j];
                const section = sections[j % sections.length];
                
                const allocation = {
                    facultyId: faculty._id.toString(),
                    subjectId: subject._id.toString(),
                    courseId: course._id.toString(),
                    year: subject.semester ? Math.ceil(subject.semester / 2) : 2,
                    section: section,
                    academicYear: '2024-2025',
                    semester: 'Fall',
                    isActive: true,
                    createdAt: new Date(),
                    updatedAt: new Date()
                };
                
                allocations.push(allocation);
                
                console.log(`✓ Assigned to ${faculty.firstName} ${faculty.lastName}:`);
                console.log(`  Subject: ${subject.name} (${subject.code})`);
                console.log(`  Year: ${allocation.year}, Section: ${section}`);
                console.log(`  Faculty ID: ${faculty._id}`);
                console.log(`  User ID: ${faculty.userId}\n`);
            }
        }
        
        // Insert all allocations
        if (allocations.length > 0) {
            const result = await db.collection('class_allocations').insertMany(allocations);
            console.log(`\n✓ Created ${result.insertedCount} class allocations\n`);
        }
        
        // Verify the assignments
        console.log('=== VERIFICATION ===\n');
        for (const faculty of faculties) {
            const count = await db.collection('class_allocations').countDocuments({
                facultyId: faculty._id.toString()
            });
            console.log(`${faculty.firstName} ${faculty.lastName}: ${count} allocations`);
        }
        
        console.log('\n✓ Done! Restart your backend and refresh the faculty dashboard.');
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

assignAllocations();
