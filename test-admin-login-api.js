// Test admin login via API
const axios = require('axios');

async function testAdminLogin() {
    try {
        console.log('Testing admin login at http://localhost:8080/api/v1/auth/login\n');
        
        const response = await axios.post('http://localhost:8080/api/v1/auth/login', {
            email: 'admin@cvr.ac.in',
            password: 'admin123'
        }, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        console.log('✅ Login successful!');
        console.log('Status:', response.status);
        console.log('Response data:', JSON.stringify(response.data, null, 2));
        
    } catch (error) {
        if (error.response) {
            console.log('❌ Login failed');
            console.log('Status:', error.response.status);
            console.log('Error data:', JSON.stringify(error.response.data, null, 2));
        } else if (error.code === 'ECONNREFUSED') {
            console.log('❌ Cannot connect to backend server');
            console.log('Error: Backend server is not running on http://localhost:8080');
            console.log('\nPlease start the backend server first:');
            console.log('  cd backend');
            console.log('  mvn spring-boot:run');
        } else {
            console.log('❌ Error:', error.message);
        }
    }
}

testAdminLogin();
