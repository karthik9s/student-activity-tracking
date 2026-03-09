# 🔧 Login Issue Fix Guide

## Problem
Getting "Invalid credentials" and "Server error" when trying to login.

## Root Cause
The user might not exist in the database, or the backend can't find the user.

---

## ✅ Solution Steps

### Step 1: Verify Data Was Seeded

Run the verification script:

```bash
# Update MongoDB URI in verify-seeded-data.js first
node verify-seeded-data.js
```

This will show you:
- How many users exist
- If admin exists
- If Rajesh Kumar exists
- All faculty emails

### Step 2: If Data is Missing, Re-seed

```bash
# Clear and re-seed
npm run seed:subjects
npm run seed:demo
```

### Step 3: Check Backend Logs

Look at your backend console for errors. Common issues:
- MongoDB connection failed
- User not found
- Password mismatch

### Step 4: Verify Backend is Running

```bash
# Check if backend is accessible
curl http://localhost:8080/api/v1/auth/login
```

Should return a 400 or 401, not 404 or connection error.

---

## 🔍 Common Issues & Fixes

### Issue 1: User Not Found in Database

**Symptoms**: "Invalid credentials" immediately

**Fix**:
```bash
# Re-run seeding
npm run seed:demo
```

### Issue 2: Wrong Password

**Symptoms**: "Invalid credentials. X attempts remaining"

**Fix**: Use correct passwords:
- Admin: `admin123`
- Faculty: `faculty123`
- Students: `student123`

### Issue 3: Backend Not Connected to MongoDB

**Symptoms**: "Server error. Please try again later"

**Fix**: Check `backend/src/main/resources/application.properties`

```properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/student_tracker
```

### Issue 4: Account Locked

**Symptoms**: "Account is temporarily locked"

**Fix**: Wait 15 minutes or clear the brute force cache

---

## 🎯 Correct Credentials

### Admin
```
Email: admin@cvr.ac.in
Password: admin123
```

### Faculty (Rajesh Kumar)
```
Email: rajesh.kumar@cvr.ac.in
Password: faculty123
```

### Student
```
Email: cse21a001@cvr.ac.in
Password: student123
```

---

## 🔬 Manual Database Check

If you have MongoDB Compass or access to MongoDB shell:

```javascript
// Connect to your database
use student_tracker

// Check if Rajesh Kumar exists
db.users.findOne({ email: "rajesh.kumar@cvr.ac.in" })

// Should return:
{
  _id: ObjectId("..."),
  username: "rajesh.kumar",
  email: "rajesh.kumar@cvr.ac.in",
  password: "$2a$10$...", // Hashed password
  role: "FACULTY",
  isActive: true,
  createdAt: ISODate("..."),
  updatedAt: ISODate("...")
}

// If it returns null, the user doesn't exist - re-run seeding
```

---

## 🚨 Emergency: Manual User Creation

If seeding keeps failing, create admin manually:

```javascript
use student_tracker

db.users.insertOne({
  username: "admin",
  email: "admin@cvr.ac.in",
  password: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
  role: "ADMIN",
  isActive: true,
  createdAt: new Date(),
  updatedAt: new Date()
})
```

Then login with: `admin@cvr.ac.in` / `admin123`

---

## 📋 Checklist

Before trying to login again:

- [ ] MongoDB is running and accessible
- [ ] Backend is running (check console for errors)
- [ ] Frontend is running
- [ ] Seeding scripts completed successfully
- [ ] Users collection has 29 documents
- [ ] Using correct email format (@cvr.ac.in)
- [ ] Using correct password (faculty123 for faculty)
- [ ] No typos in email or password
- [ ] Account is not locked (wait 15 min if locked)

---

## 🔄 Complete Reset

If nothing works, do a complete reset:

```bash
# 1. Stop backend and frontend

# 2. Clear MongoDB collections (in MongoDB shell or Compass)
use student_tracker
db.users.deleteMany({})
db.students.deleteMany({})
db.faculty.deleteMany({})
db.courses.deleteMany({})
db.subjects.deleteMany({})
db.classAllocations.deleteMany({})
db.attendance.deleteMany({})

# 3. Re-seed everything
npm run seed:subjects
npm run seed:demo

# 4. Restart backend
cd backend
mvn spring-boot:run

# 5. Restart frontend
cd frontend
npm run dev

# 6. Try login again
```

---

## 📞 Still Not Working?

Check these files for configuration:
1. `backend/src/main/resources/application.properties` - MongoDB URI
2. `seed-complete-demo-data.js` - MongoDB URI (line 8)
3. Backend console - Look for connection errors
4. Browser console (F12) - Look for network errors

---

**Most Common Fix**: Re-run `npm run seed:demo` to ensure users are created!
