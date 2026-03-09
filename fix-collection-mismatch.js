// Fix Collection Mismatch - Consolidate class allocation data
// The app expects: class_allocations (plural with underscore)
// Current state: data split between class_allocation and classAllocation

const { MongoClient } = require('mongodb');

const MONGO_URI = 'mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0';
const DB_NAME = 'student_tracker';

async function fixCollectionMismatch() {
    const client = new MongoClient(MONGO_URI);
    
    try {
        await client.connect();
        console.log('Connected to MongoDB');
        
        const db = client.db(DB_NAME);
        
        // Check what collections exist
        const collections = await db.listCollections().toArray();
        console.log('\nExisting collections:');
        collections.forEach(col => console.log(`  - ${col.name}`));
        
        // Count documents in each collection
        const classAllocationCount = await db.collection('class_allocation').countDocuments();
        const classAllocationsCount = await db.collection('class_allocations').countDocuments();
        const classAllocationCamelCount = await db.collection('classAllocation').countDocuments();
        
        console.log('\nDocument counts:');
        console.log(`  class_allocation: ${classAllocationCount}`);
        console.log(`  class_allocations: ${classAllocationsCount}`);
        console.log(`  classAllocation: ${classAllocationCamelCount}`);
        
        // The app expects: class_allocations (plural)
        console.log('\n✓ Application expects: class_allocations');
        
        // Consolidate data into class_allocations
        console.log('\nConsolidating data into class_allocations...');
        
        // Copy from class_allocation (2 documents)
        if (classAllocationCount > 0) {
            const docs1 = await db.collection('class_allocation').find({}).toArray();
            console.log(`\nFound ${docs1.length} documents in class_allocation:`);
            docs1.forEach(doc => {
                console.log(`  - Faculty: ${doc.facultyId}, Subject: ${doc.subjectId}, Section: ${doc.section}`);
            });
            
            for (const doc of docs1) {
                // Check if already exists in class_allocations
                const exists = await db.collection('class_allocations').findOne({ _id: doc._id });
                if (!exists) {
                    await db.collection('class_allocations').insertOne(doc);
                    console.log(`  ✓ Copied document ${doc._id}`);
                } else {
                    console.log(`  - Document ${doc._id} already exists in class_allocations`);
                }
            }
        }
        
        // Copy from classAllocation (28 documents)
        if (classAllocationCamelCount > 0) {
            const docs2 = await db.collection('classAllocation').find({}).toArray();
            console.log(`\nFound ${docs2.length} documents in classAllocation:`);
            
            for (const doc of docs2) {
                // Check if already exists in class_allocations
                const exists = await db.collection('class_allocations').findOne({ _id: doc._id });
                if (!exists) {
                    await db.collection('class_allocations').insertOne(doc);
                    console.log(`  ✓ Copied document ${doc._id}`);
                } else {
                    console.log(`  - Document ${doc._id} already exists in class_allocations`);
                }
            }
        }
        
        // Final count
        const finalCount = await db.collection('class_allocations').countDocuments();
        console.log(`\n✓ Final count in class_allocations: ${finalCount}`);
        
        // Show sample data
        console.log('\nSample allocations in class_allocations:');
        const samples = await db.collection('class_allocations').find({}).limit(5).toArray();
        samples.forEach(doc => {
            console.log(`  - ID: ${doc._id}`);
            console.log(`    Faculty: ${doc.facultyId}`);
            console.log(`    Subject: ${doc.subjectId}`);
            console.log(`    Year: ${doc.year}, Section: ${doc.section}`);
        });
        
        console.log('\n✓ Data consolidation complete!');
        console.log('\nNext steps:');
        console.log('1. Restart your backend application');
        console.log('2. Log in as a faculty member');
        console.log('3. Check if allocations now appear in the dashboard');
        console.log('\nOptional: You can delete the old collections after verifying:');
        console.log('  db.class_allocation.drop()');
        console.log('  db.classAllocation.drop()');
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

fixCollectionMismatch();
