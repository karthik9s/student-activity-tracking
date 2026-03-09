const axios = require('axios');

async function testLogin() {
    try {
        console.log('Testing login with admin@cvr.ac.in / admin123...\n');
        
        const response = await axios.post('http://localhost:8080/api/v1/auth/login', {
            email: 'admin@cvr.ac.in',
            password: 'admin123'
        });
        
        console.log('✅ Login successful!');
        console.log('Response:', JSON.stringify(response.data, null, 2));
        
    } catch (error) {
        console.log('❌ Login failed!');
        if (error.response) {
            console.log('Status:', error.response.status);
            console.log('Error:', JSON.stringify(error.response.data, null, 2));
        } else {
            console.log('Error:', error.message);
        }
    }
}

testLogin();
