# Login Issue Troubleshooting Guide

## 🚨 Current Issue
You're seeing "Invalid credentials" and "Server error" messages because the database hasn't been seeded with demo users yet.

## ✅ Quick Fix

### Step 1: Install Seeding Dependencies
```bash
npm install
```

### Step 2: Configure MongoDB Connection

Edit both seeding scripts and add your MongoDB connection string:

**File: `seed-subjects-data.js` (Line 6)**
```javascript
const MONGODB_URI = 'your-mongodb-connection-string-here';
```

**File: `seed-complete-demo-data.js` (Line 8)**
```javascript
const MONGODB_URI = 'your-mongodb-connection-string-here';
```

### Step 3: Run Seeding Scripts
```bash
# Seed courses and subjects
npm run seed:subjects

# Seed users, allocations, and attendance
npm run seed:demo
```

### Step 4: Verify Seeding
After seeding completes, you should see:
```
✓ Admin user created
✓ Created 8 faculty members
✓ Created 20 students
✓ Created ~18 class allocations
✓ Created ~5,400 attendance records
```

### Step 5: Try Login Again
Now you can login with:
- **Email**: `admin@cvr.ac.in`
- **Password**: `admin123`

---

## 🔍 Alternative: Check if Data Already Exists

If you think data might already be seeded, check MongoDB:

```javascript
// Connect to MongoDB and run:
use student_tracker

// Check if users exist
db.users.countDocuments()  // Should return 29

// Check if admin exists
db.users.findOne({username: "admin"})

// If admin exists, the issue might be password mismatch
```

---

## 🐛 Common Issues

### Issue 1: "Cannot connect to MongoDB"
**Solution**: 
- Verify MongoDB URI is correct
- Check MongoDB Atlas network access
- Whitelist your IP address

### Issue 2: "Cannot find module 'mongodb'"
**Solution**:
```bash
npm install mongodb bcryptjs
```

### Issue 3: Backend not connecting to MongoDB
**Check**: `backend/src/main/resources/application.properties`

Make sure MongoDB URI is configured:
```properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/student_tracker
```

### Issue 4: CORS Error
**Check**: Backend CORS configuration allows frontend origin

---

## 📋 Complete Setup Checklist

- [ ] MongoDB Atlas cluster created
- [ ] Network access configured (IP whitelisted)
- [ ] Database user created with read/write permissions
- [ ] MongoDB URI updated in seeding scripts
- [ ] MongoDB URI updated in backend application.properties
- [ ] Dependencies installed (`npm install`)
- [ ] Subjects seeded (`npm run seed:subjects`)
- [ ] Demo data seeded (`npm run seed:demo`)
- [ ] Backend running (`mvn spring-boot:run`)
- [ ] Frontend running (`npm run dev`)
- [ ] Can access login page
- [ ] Can login with admin@cvr.ac.in / admin123

---

## 🎯 Quick Test

After seeding, test these credentials:

### Admin
```
Email: admin@cvr.ac.in
Password: admin123
```

### Faculty
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

## 📞 Still Having Issues?

1. Check backend console for errors
2. Check browser console (F12) for network errors
3. Verify MongoDB connection in backend logs
4. Check if collections exist in MongoDB
5. Verify user documents have correct structure

---

## 🔧 Manual User Creation (Emergency)

If seeding fails, you can manually create an admin user in MongoDB:

```javascript
use student_tracker

// Create admin user (password is bcrypt hash of "admin123")
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

Then try logging in with:
- Email: `admin@cvr.ac.in`
- Password: `admin123`

---

**Need more help? Check CVR_CREDENTIALS.md for all demo credentials!**
