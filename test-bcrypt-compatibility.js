const bcrypt = require('bcryptjs');

async function testBcryptCompatibility() {
    const password = 'admin123';
    
    // Hash with 10 rounds (like seeding script)
    const hash10 = await bcrypt.hash(password, 10);
    console.log('Hash with 10 rounds:', hash10);
    
    // Hash with 12 rounds (like backend)
    const hash12 = await bcrypt.hash(password, 12);
    console.log('Hash with 12 rounds:', hash12);
    
    // Test if password matches hash10
    const match10 = await bcrypt.compare(password, hash10);
    console.log('\nPassword matches hash10:', match10);
    
    // Test if password matches hash12
    const match12 = await bcrypt.compare(password, hash12);
    console.log('Password matches hash12:', match12);
    
    // Test cross-compatibility
    console.log('\n--- Cross Compatibility Test ---');
    console.log('Can verify 10-round hash with bcrypt.compare:', await bcrypt.compare(password, hash10));
    console.log('Can verify 12-round hash with bcrypt.compare:', await bcrypt.compare(password, hash12));
}

testBcryptCompatibility();
