// Diagnostic script to check admin user in database
// This script verifies the admin@cvr.ac.in user exists with correct credentials

const { MongoClient } = require('mongodb');
const bcrypt = require('bcryptjs');

const uri = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker';
const dbName = 'student_tracker';

async function checkAdminUser() {
    const client = new MongoClient(uri);
    
    try {
        await client.connect();
        console.log('Connected to MongoDB');
        
        const db = client.db(dbName);
        const usersCollection = db.collection('users');
        
        console.log('\n=== DIAGNOSTIC: Admin User Verification ===\n');
        
        // Test 1: Check if admin user exists
        console.log('Test 1: Checking if admin@cvr.ac.in exists in database...');
        const adminUser = await usersCollection.findOne({ email: 'admin@cvr.ac.in' });
        
        if (!adminUser) {
            console.log('❌ COUNTEREXAMPLE FOUND: Admin user admin@cvr.ac.in does not exist in database');
            console.log('   Root cause: Missing admin user document');
            console.log('   Expected: Admin user should exist in users collection\n');
            return;
        }
        
        console.log('✓ Admin user exists');
        console.log(`   User ID: ${adminUser._id}`);
        console.log(`   Email: ${adminUser.email}`);
        console.log(`   Role: ${adminUser.role}`);
        console.log(`   IsActive: ${adminUser.isActive}`);
        console.log(`   Password Hash: ${adminUser.password ? adminUser.password.substring(0, 20) + '...' : 'null'}\n`);
        
        // Test 2: Check password hash format and verification
        console.log('Test 2: Verifying password hash matches "admin123"...');
        if (!adminUser.password) {
            console.log('❌ COUNTEREXAMPLE FOUND: Admin password hash is null');
            console.log('   Root cause: Missing password hash');
            console.log('   Expected: Password hash should be BCrypt hash of "admin123"\n');
            return;
        }
        
        const passwordMatches = await bcrypt.compare('admin123', adminUser.password);
        if (!passwordMatches) {
            console.log('❌ COUNTEREXAMPLE FOUND: Admin password hash does not match BCrypt hash of "admin123"');
            console.log(`   Stored hash: ${adminUser.password.substring(0, 20)}...`);
            console.log('   Root cause: Incorrect password hash');
            console.log('   Expected: Password hash should match BCrypt hash of "admin123"\n');
            
            // Check if it's plaintext
            if (adminUser.password === 'admin123') {
                console.log('   Note: Password appears to be stored as plaintext instead of hashed');
            }
            return;
        }
        
        console.log('✓ Password hash matches "admin123"');
        console.log(`   Hash format: ${adminUser.password.startsWith('$2a$') ? 'BCrypt' : 'Unknown'}\n`);
        
        // Test 3: Check role field
        console.log('Test 3: Verifying role is "ROLE_ADMIN"...');
        if (adminUser.role !== 'ROLE_ADMIN') {
            console.log(`❌ COUNTEREXAMPLE FOUND: Admin role is "${adminUser.role}" instead of "ROLE_ADMIN"`);
            console.log('   Root cause: Incorrect role assignment');
            console.log('   Expected: Role field should be exactly "ROLE_ADMIN" (with ROLE_ prefix)\n');
            return;
        }
        
        console.log('✓ Role is "ROLE_ADMIN"\n');
        
        // Test 4: Check isActive field
        console.log('Test 4: Verifying isActive is true...');
        if (adminUser.isActive === null || adminUser.isActive === undefined) {
            console.log('❌ COUNTEREXAMPLE FOUND: Admin isActive field is null/undefined');
            console.log('   Root cause: Missing isActive field');
            console.log('   Expected: isActive should be boolean true\n');
            return;
        }
        
        if (adminUser.isActive !== true) {
            console.log(`❌ COUNTEREXAMPLE FOUND: Admin isActive is ${adminUser.isActive} instead of true`);
            console.log('   Root cause: Inactive account status');
            console.log('   Expected: isActive should be boolean true\n');
            return;
        }
        
        console.log('✓ IsActive is true\n');
        
        // Summary
        console.log('=== DIAGNOSTIC SUMMARY ===');
        console.log('✓ All diagnostic checks passed');
        console.log('✓ Admin user exists with correct credentials');
        console.log('✓ Email: admin@cvr.ac.in');
        console.log('✓ Password: admin123 (BCrypt hashed)');
        console.log('✓ Role: ROLE_ADMIN');
        console.log('✓ IsActive: true');
        console.log('\nThe admin user appears to be correctly configured in the database.');
        console.log('If login still fails, the issue may be in the authentication flow or Spring Security configuration.\n');
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

checkAdminUser();
