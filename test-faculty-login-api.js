// Script to test faculty login and allocation retrieval via API
const BASE_URL = 'http://localhost:8080/api/v1';

async function testFacultyLogin(email, password, expectedAllocations) {
  console.log(`\n=== Testing ${email} ===`);
  
  try {
    // Step 1: Login
    console.log('Step 1: Logging in...');
    const loginResponse = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: email,
        password: password
      })
    });
    
    if (loginResponse.ok) {
      const loginData = await loginResponse.json();
      console.log('✅ Login successful');
      console.log(`   Token: ${loginData.token.substring(0, 20)}...`);
      console.log(`   Role: ${loginData.role}`);
      
      const token = loginData.token;
      
      // Step 2: Get allocations
      console.log('\nStep 2: Fetching allocations...');
      const allocationsResponse = await fetch(`${BASE_URL}/faculty/allocations`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (allocationsResponse.ok) {
        const allocations = await allocationsResponse.json();
        console.log(`✅ Allocations retrieved: ${allocations.length} found`);
        
        if (allocations.length === expectedAllocations) {
          console.log(`✅ Expected ${expectedAllocations} allocations, got ${allocations.length} - PASS`);
        } else {
          console.log(`❌ Expected ${expectedAllocations} allocations, got ${allocations.length} - FAIL`);
        }
        
        // Display allocation details
        if (allocations.length > 0) {
          console.log('\nAllocation Details:');
          allocations.forEach((alloc, index) => {
            console.log(`  ${index + 1}. ${alloc.subjectName || 'Unknown Subject'}`);
            console.log(`     Course: ${alloc.courseName || 'N/A'}`);
            console.log(`     Year: ${alloc.year}, Section: ${alloc.section}, Semester: ${alloc.semester}`);
          });
        } else {
          console.log('   No allocations found');
        }
        
        return {
          success: true,
          allocationsCount: allocations.length,
          expectedCount: expectedAllocations,
          passed: allocations.length === expectedAllocations
        };
      } else {
        const errorText = await allocationsResponse.text();
        console.log(`❌ Failed to fetch allocations: ${allocationsResponse.status}`);
        console.log(`   Error: ${errorText}`);
        return { success: false, error: 'Failed to fetch allocations' };
      }
      
    } else {
      const errorText = await loginResponse.text();
      console.log(`❌ Login failed: ${loginResponse.status}`);
      console.log(`   Error: ${errorText}`);
      return { success: false, error: 'Login failed' };
    }
    
  } catch (error) {
    console.log(`❌ Error: ${error.message}`);
    return { success: false, error: error.message };
  }
}

async function runTests() {
  console.log('=================================================');
  console.log('Faculty Allocation Retrieval - API Testing');
  console.log('=================================================');
  
  const results = [];
  
  // Test 1: Rajesh Kumar (2 allocations)
  const rajeshResult = await testFacultyLogin('rajesh.kumar@cvr.ac.in', 'faculty123', 2);
  results.push({ name: 'Rajesh Kumar', ...rajeshResult });
  
  // Test 2: Priya Sharma (2 allocations)
  const priyaResult = await testFacultyLogin('priya.sharma@cvr.ac.in', 'faculty123', 2);
  results.push({ name: 'Priya Sharma', ...priyaResult });
  
  // Test 3: Anita Desai (3 allocations)
  const anitaResult = await testFacultyLogin('anita.desai@cvr.ac.in', 'faculty123', 3);
  results.push({ name: 'Anita Desai', ...anitaResult });
  
  // Summary
  console.log('\n=================================================');
  console.log('Test Summary');
  console.log('=================================================');
  
  const passedTests = results.filter(r => r.passed).length;
  const totalTests = results.length;
  
  results.forEach(result => {
    const status = result.passed ? '✅ PASS' : '❌ FAIL';
    console.log(`${status} - ${result.name}: ${result.allocationsCount || 0}/${result.expectedCount} allocations`);
  });
  
  console.log(`\nTotal: ${passedTests}/${totalTests} tests passed`);
  
  if (passedTests === totalTests) {
    console.log('\n✅ All tests passed! The fix is working correctly.');
  } else {
    console.log('\n❌ Some tests failed. Please review the results above.');
  }
}

// Run the tests
runTests().catch(console.error);
