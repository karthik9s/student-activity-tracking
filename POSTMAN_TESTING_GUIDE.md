# Postman Testing Guide - Student Activity Tracker API

## Prerequisites
1. Install Postman from https://www.postman.com/downloads/
2. Backend running on http://localhost:8080
3. Admin user created in MongoDB

## Step 1: Import Postman Collection (Optional)

You can import the existing collection:
1. Open Postman
2. Click "Import" button
3. Select `backend/POSTMAN_COLLECTION.json`
4. Click "Import"

OR follow the manual steps below:

## Step 2: Create a New Collection

1. Open Postman
2. Click "New" → "Collection"
3. Name it "Student Activity Tracker"
4. Click "Create"

## Step 3: Set Up Environment Variables

1. Click on "Environments" (left sidebar)
2. Click "+" to create new environment
3. Name it "Local Development"
4. Add these variables:

| Variable | Initial Value | Current Value |
|----------|--------------|---------------|
| baseUrl | http://localhost:8080 | http://localhost:8080 |
| accessToken | (leave empty) | (leave empty) |

5. Click "Save"
6. Select "Local Development" from the environment dropdown (top right)

## Step 4: Test Authentication Endpoints

### 4.1 Login (Get Access Token)

**Request:**
- Method: `POST`
- URL: `{{baseUrl}}/api/v1/auth/login`
- Headers:
  ```
  Content-Type: application/json
  ```
- Body (raw JSON):
  ```json
  {
    "email": "admin@college.com",
    "password": "admin123"
  }
  ```

**Expected Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "...",
    "email": "admin@college.com",
    "role": "ROLE_ADMIN",
    "isActive": true
  }
}
```

**Important:** Copy the `accessToken` value!

### 4.2 Set Access Token Automatically

Add this to the "Tests" tab of the Login request:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("accessToken", jsonData.accessToken);
    console.log("Access token saved:", jsonData.accessToken);
}
```

Now when you login, the token will be saved automatically!

### 4.3 Refresh Token

**Request:**
- Method: `POST`
- URL: `{{baseUrl}}/api/v1/auth/refresh`
- Headers:
  ```
  Content-Type: application/json
  ```
- Body (raw JSON):
  ```json
  {
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }
  ```

## Step 5: Test Admin Endpoints (Requires Admin Role)

### 5.1 Create a Course

**Request:**
- Method: `POST`
- URL: `{{baseUrl}}/api/v1/admin/courses`
- Headers:
  ```
  Content-Type: application/json
  Authorization: Bearer {{accessToken}}
  ```
- Body (raw JSON):
  ```json
  {
    "code": "BTECH-CSE",
    "name": "Bachelor of Technology in Computer Science",
    "duration": 4,
    "totalSemesters": 8
  }
  ```

**Expected Response (201 Created):**
```json
{
  "id": "...",
  "code": "BTECH-CSE",
  "name": "Bachelor of Technology in Computer Science",
  "duration": 4,
  "totalSemesters": 8
}
```

### 5.2 Get All Courses

**Request:**
- Method: `GET`
- URL: `{{baseUrl}}/api/v1/admin/courses?page=0&size=10`
- Headers:
  ```
  Authorization: Bearer {{accessToken}}
  ```

### 5.3 Create a Subject

**Request:**
- Method: `POST`
- URL: `{{baseUrl}}/api/v1/admin/subjects`
- Headers:
  ```
  Content-Type: application/json
  Authorization: Bearer {{accessToken}}
  ```
- Body (raw JSON):
  ```json
  {
    "code": "CS101",
    "name": "Introduction to Programming",
    "credits": 4,
    "semester": 1,
    "courseId": "COURSE_ID_FROM_STEP_5.1"
  }
  ```

### 5.4 Create a Student

**Request:**
- Method: `POST`
- URL: `{{baseUrl}}/api/v1/admin/students`
- Headers:
  ```
  Content-Type: application/json
  Authorization: Bearer {{accessToken}}
  ```
- Body (raw JSON):
  ```json
  {
    "rollNumber": "CS2024001",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@student.com",
    "phone": "1234567890",
    "dateOfBirth": "2005-01-15",
    "gender": "Male",
    "address": "123 Main St",
    "courseId": "COURSE_ID_FROM_STEP_5.1",
    "semester": 1,
    "year": 1,
    "section": "A",
    "admissionDate": "2024-08-01"
  }
  ```

### 5.5 Get All Students

**Request:**
- Method: `GET`
- URL: `{{baseUrl}}/api/v1/admin/students?page=0&size=10`
- Headers:
  ```
  Authorization: Bearer {{accessToken}}
  ```

### 5.6 Create Faculty

**Request:**
- Method: `POST`
- URL: `{{baseUrl}}/api/v1/admin/faculty`
- Headers:
  ```
  Content-Type: application/json
  Authorization: Bearer {{accessToken}}
  ```
- Body (raw JSON):
  ```json
  {
    "employeeId": "FAC001",
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@college.com",
    "phone": "9876543210",
    "department": "Computer Science",
    "designation": "Assistant Professor",
    "dateOfJoining": "2020-07-01",
    "qualification": "PhD in Computer Science"
  }
  ```

### 5.7 Get Dashboard Statistics

**Request:**
- Method: `GET`
- URL: `{{baseUrl}}/api/v1/admin/dashboard/stats`
- Headers:
  ```
  Authorization: Bearer {{accessToken}}
  ```

## Step 6: Test Search and Filter

### 6.1 Search Students

**Request:**
- Method: `GET`
- URL: `{{baseUrl}}/api/v1/admin/students/search?query=John&page=0&size=10`
- Headers:
  ```
  Authorization: Bearer {{accessToken}}
  ```

### 6.2 Filter Students by Course

**Request:**
- Method: `GET`
- URL: `{{baseUrl}}/api/v1/admin/students?courseId=COURSE_ID&page=0&size=10`
- Headers:
  ```
  Authorization: Bearer {{accessToken}}
  ```

## Step 7: Test Update Operations

### 7.1 Update Student

**Request:**
- Method: `PUT`
- URL: `{{baseUrl}}/api/v1/admin/students/STUDENT_ID`
- Headers:
  ```
  Content-Type: application/json
  Authorization: Bearer {{accessToken}}
  ```
- Body (raw JSON):
  ```json
  {
    "rollNumber": "CS2024001",
    "firstName": "John",
    "lastName": "Doe Updated",
    "email": "john.doe@student.com",
    "phone": "1234567890",
    "semester": 2,
    "year": 1,
    "section": "A"
  }
  ```

## Step 8: Test Delete Operations

### 8.1 Delete Student

**Request:**
- Method: `DELETE`
- URL: `{{baseUrl}}/api/v1/admin/students/STUDENT_ID`
- Headers:
  ```
  Authorization: Bearer {{accessToken}}
  ```

## Step 9: Test Error Scenarios

### 9.1 Unauthorized Access (No Token)

**Request:**
- Method: `GET`
- URL: `{{baseUrl}}/api/v1/admin/students`
- Headers: (Don't include Authorization header)

**Expected Response (401 Unauthorized)**

### 9.2 Invalid Credentials

**Request:**
- Method: `POST`
- URL: `{{baseUrl}}/api/v1/auth/login`
- Body:
  ```json
  {
    "email": "admin@college.com",
    "password": "wrongpassword"
  }
  ```

**Expected Response (401 Unauthorized)**

### 9.3 Validation Error

**Request:**
- Method: `POST`
- URL: `{{baseUrl}}/api/v1/admin/students`
- Headers:
  ```
  Content-Type: application/json
  Authorization: Bearer {{accessToken}}
  ```
- Body (invalid email):
  ```json
  {
    "rollNumber": "CS2024002",
    "firstName": "Test",
    "lastName": "User",
    "email": "invalid-email",
    "phone": "1234567890"
  }
  ```

**Expected Response (400 Bad Request)**

## Step 10: Test Swagger UI (Alternative)

You can also test the API using Swagger UI:

1. Open browser
2. Go to: http://localhost:8080/swagger-ui.html
3. Click "Authorize" button
4. Enter: `Bearer YOUR_ACCESS_TOKEN`
5. Click "Authorize"
6. Now you can test all endpoints directly from Swagger

## Common Issues and Solutions

### Issue 1: 401 Unauthorized
**Solution:** Make sure you've logged in and the access token is set in the Authorization header.

### Issue 2: 403 Forbidden
**Solution:** Your user doesn't have the required role. Admin endpoints require ROLE_ADMIN.

### Issue 3: Token Expired
**Solution:** Login again to get a new access token, or use the refresh token endpoint.

### Issue 4: CORS Error
**Solution:** Make sure the backend CORS configuration includes your origin.

## Quick Test Sequence

1. **Login** → Get access token
2. **Create Course** → Save course ID
3. **Create Subject** → Use course ID from step 2
4. **Create Student** → Use course ID from step 2
5. **Get All Students** → Verify student was created
6. **Update Student** → Change some fields
7. **Delete Student** → Clean up test data

## Tips for Efficient Testing

1. **Use Collection Variables:** Store IDs in collection variables for reuse
2. **Use Pre-request Scripts:** Set up test data automatically
3. **Use Tests Tab:** Add assertions to verify responses
4. **Create Test Suites:** Group related requests into folders
5. **Use Collection Runner:** Run all tests automatically

## Sample Test Script (Add to Tests tab)

```javascript
// Check status code
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Check response time
pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

// Check response structure
pm.test("Response has required fields", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('email');
});
```

## Conclusion

You now have a complete guide to test your backend API using Postman. Start with the authentication endpoints, then move on to CRUD operations, and finally test error scenarios.

For more details, check:
- API Documentation: `backend/API_DOCUMENTATION.md`
- Postman Collection: `backend/POSTMAN_COLLECTION.json`
- Swagger UI: http://localhost:8080/swagger-ui.html
