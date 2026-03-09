# Task 6.1 - Manual Testing Guide
## Faculty Allocation Retrieval Fix - Real Account Testing

### Test Date
January 5, 2025

### Test Environment
- Frontend: http://localhost:5173 (running)
- Backend: http://localhost:8080 (running)
- Database: MongoDB Atlas (student_tracker)

---

## Test Scenario 1: Faculty with 2 Allocations (Rajesh Kumar)

### Test Data
- **Email**: rajesh.kumar@cvr.ac.in
- **Password**: faculty123
- **User ID**: 69ad4582e1dd11555058fa8f
- **Faculty ID**: 69ad4582e1dd11555058fa90
- **Expected Allocations**: 2
  1. Engineering Chemistry
  2. Database Management Systems

### Test Steps
1. Navigate to http://localhost:5173
2. Click "Login" or navigate to login page
3. Enter credentials:
   - Username: `rajesh.kumar`
   - Password: `faculty123`
4. Click "Login" button
5. Verify successful login and redirect to faculty dashboard
6. Check the "Class Allocations" or "My Classes" section

### Expected Results
✅ Login successful
✅ Dashboard displays without errors
✅ Class Allocations section shows 2 allocations:
   - Engineering Chemistry
   - Database Management Systems
✅ Each allocation shows complete details (course, year, section, semester)
✅ No error messages displayed
✅ Attendance and Performance sections are accessible

### Actual Results
_To be filled during manual testing_

---

## Test Scenario 2: Faculty with 2 Allocations (Priya Sharma)

### Test Data
- **Email**: priya.sharma@cvr.ac.in
- **Password**: faculty123
- **User ID**: 69ad4582e1dd11555058fa91
- **Faculty ID**: 69ad4582e1dd11555058fa92
- **Expected Allocations**: 2
  1. Mathematics for Computing
  2. Computer Oriented Statistical Methods

### Test Steps
1. Log out from previous session (if logged in)
2. Navigate to http://localhost:5173
3. Click "Login"
4. Enter credentials:
   - Username: `priya.sharma`
   - Password: `faculty123`
5. Click "Login" button
6. Verify successful login and redirect to faculty dashboard
7. Check the "Class Allocations" or "My Classes" section

### Expected Results
✅ Login successful
✅ Dashboard displays without errors
✅ Class Allocations section shows 2 allocations:
   - Mathematics for Computing
   - Computer Oriented Statistical Methods
✅ Each allocation shows complete details
✅ No error messages displayed

### Actual Results
_To be filled during manual testing_

**Note**: Task requirements mentioned 3 allocations, but database verification shows Priya Sharma has 2 allocations. This is the correct expected behavior based on actual data.

---

## Test Scenario 3: Faculty with 3 Allocations (Anita Desai)

### Test Data
- **Email**: anita.desai@cvr.ac.in
- **Password**: faculty123
- **User ID**: 69ad4584e1dd11555058fa99
- **Faculty ID**: 69ad4584e1dd11555058fa9a
- **Expected Allocations**: 3
  1. Essentials of System and Web Interfacing
  2. Discrete Mathematics
  3. Applied Physics

### Test Steps
1. Log out from previous session
2. Navigate to http://localhost:5173
3. Click "Login"
4. Enter credentials:
   - Username: `anita.desai`
   - Password: `faculty123`
5. Click "Login" button
6. Verify successful login and redirect to faculty dashboard
7. Check the "Class Allocations" or "My Classes" section

### Expected Results
✅ Login successful
✅ Dashboard displays without errors
✅ Class Allocations section shows 3 allocations:
   - Essentials of System and Web Interfacing
   - Discrete Mathematics
   - Applied Physics
✅ All 3 allocations are displayed correctly
✅ No error messages displayed

### Actual Results
_To be filled during manual testing_

---

## Test Scenario 4: Faculty with No Allocations

### Current Status
⚠️ **All faculty in the current database have allocations**

To test the empty state scenario, we need to either:
1. Create a new faculty member without allocations
2. Temporarily remove allocations from an existing faculty member

### Option A: Create New Faculty Without Allocations

#### Steps to Create Test Faculty
1. Log in as admin (username: `admin`, password: `admin123`)
2. Navigate to Faculty Management
3. Create new faculty:
   - First Name: Test
   - Last Name: Faculty
   - Email: test.faculty@cvr.ac.in
   - Employee ID: FAC999
   - Department: CSE
   - Designation: Assistant Professor
4. Create user account for this faculty (if not auto-created)
5. Log out and log in as test.faculty with password: faculty123

#### Expected Results
✅ Login successful
✅ Dashboard displays without errors
✅ Class Allocations section shows empty state message (e.g., "No allocations found" or "You have no class allocations")
✅ **NOT an error message** - should be a friendly empty state
✅ Other sections of dashboard are accessible

### Option B: Use Existing Faculty (Not Recommended)
Temporarily removing allocations from existing faculty could affect other tests and data integrity.

### Actual Results
_To be filled during manual testing_

---

## Verification Checklist

### Before Testing
- [ ] Backend server is running (http://localhost:8080)
- [ ] Frontend server is running (http://localhost:5173)
- [ ] Database connection is active
- [ ] All previous bugfix tasks (1-5) are completed and tests pass

### During Testing
- [ ] Test Scenario 1: Rajesh Kumar (2 allocations) - PASS/FAIL
- [ ] Test Scenario 2: Priya Sharma (2 allocations) - PASS/FAIL
- [ ] Test Scenario 3: Anita Desai (3 allocations) - PASS/FAIL
- [ ] Test Scenario 4: Empty state handling - PASS/FAIL

### After Testing
- [ ] All scenarios passed
- [ ] No console errors in browser
- [ ] No backend errors in logs
- [ ] Faculty can access other features (attendance, performance)
- [ ] Admin panel still works correctly

---

## Requirements Validation

This manual testing validates:
- **Requirement 2.1**: System resolves User ID to Faculty ID before querying allocations
- **Requirement 2.2**: System queries allocations using correct Faculty ID
- **Requirement 2.3**: Faculty dashboard displays all allocated classes

---

## Troubleshooting

### Issue: Login fails
- Verify credentials are correct
- Check backend logs for authentication errors
- Verify user exists in database

### Issue: Dashboard shows no allocations (but should have them)
- This indicates the bug is NOT fixed
- Check backend logs for errors
- Verify FacultyService.getAllocationsByUserId() is being called
- Verify Faculty.userId matches User._id

### Issue: Dashboard shows error message
- Check browser console for errors
- Check backend logs for exceptions
- Verify API endpoint is accessible

### Issue: Allocations show incorrect data
- Verify database data integrity
- Check ClassAllocationDTO mapping
- Verify subject and course data exists

---

## Database Verification Queries

If you need to verify data directly in MongoDB:

```javascript
// Check user
db.users.findOne({ email: 'rajesh.kumar@cvr.ac.in' })

// Check faculty record
db.faculty.findOne({ userId: '<user_id_from_above>' })

// Check allocations
db.classAllocations.find({ facultyId: '<faculty_id_from_above>' })
```

---

## Notes

1. **Task Discrepancy**: The task requirements mentioned specific User IDs (675e8f9a2b1c3d4e5f6a7b8c), but the actual database has different IDs. This is expected as the database was reseeded.

2. **Priya Sharma Allocations**: Task mentioned 3 allocations, but actual data shows 2. Testing should verify against actual data (2 allocations).

3. **Empty State Testing**: Since all faculty have allocations, we need to create a test faculty or use admin to create one without allocations.

4. **Fix Verification**: The key test is that faculty members can now see their allocations. Before the fix, they would see empty dashboards even though allocations existed in the database.

---

## Success Criteria

✅ All faculty members can log in successfully
✅ Faculty dashboards display correct number of allocations
✅ Allocation details are complete and accurate
✅ Empty state is handled gracefully (no error messages)
✅ No console or backend errors
✅ Other faculty features remain functional
