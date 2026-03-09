# 🚀 Quick Start Guide

Get your Student Activity Tracking System up and running with demo data in 5 minutes!

## ⚡ Super Quick Setup

### 1. Install Dependencies (30 seconds)
```bash
npm install
```

### 2. Configure MongoDB (1 minute)
Open `seed-complete-demo-data.js` and update line 8:
```javascript
const MONGODB_URI = 'your-mongodb-connection-string-here';
```

**Get your MongoDB URI:**
- MongoDB Atlas: Database → Connect → Drivers → Copy connection string
- Local: `mongodb://localhost:27017/student_tracker`

### 3. Seed Database (2 minutes)
```bash
# Seed everything at once
npm run seed:subjects
npm run seed:demo
```

### 4. Login and Test (1 minute)
```bash
# Start backend (in backend folder)
cd backend
mvn spring-boot:run

# Start frontend (in frontend folder)
cd frontend
npm run dev
```

**Login with:**
- Admin: `admin` / `admin123` (admin@cvr.ac.in)
- Faculty: `rajesh.kumar` / `faculty123` (rajesh.kumar@cvr.ac.in)
- Student: `cse21a001` / `student123` (cse21a001@cvr.ac.in)

## ✅ That's It!

You now have:
- ✅ 3 courses
- ✅ 106 subjects
- ✅ 29 users (1 admin, 8 faculty, 20 students)
- ✅ 18 class allocations
- ✅ 5,400+ attendance records

## 🎯 What to Test First

### As Admin (`admin` / `admin123`)
1. Go to Dashboard → View statistics
2. Navigate to Student Management → See all students
3. Navigate to Faculty Management → See all faculty
4. Navigate to Class Allocations → See assignments

### As Faculty (`rajesh.kumar` / `faculty123`)
1. Go to Dashboard → View assigned subjects
2. Navigate to Attendance Marking → Mark attendance
3. Navigate to Reports → Generate attendance report
4. View student list for your subjects

### As Student (`cse21a001` / `student123`)
1. Go to Dashboard → View your subjects
2. Navigate to My Attendance → See attendance records
3. Check attendance percentage
4. View subject-wise breakdown

## 📚 Need More Details?

- **Complete credentials**: See `DEMO_CREDENTIALS.md`
- **Detailed setup**: See `SEEDING_README.md`
- **Data structure**: See `DATA_STRUCTURE_SUMMARY.md`
- **Troubleshooting**: See `COMPLETE_DEMO_DATA_GUIDE.md`

## 🔧 Common Issues

### "Cannot find module 'mongodb'"
```bash
npm install
```

### "Connection timeout"
- Check MongoDB URI
- Verify network access in MongoDB Atlas
- Whitelist your IP address

### "No courses found"
```bash
# Run subjects seeding first
npm run seed:subjects
```

## 🎨 Customize Demo Data

Want to add more students or change data?

Edit `seed-complete-demo-data.js`:
- Line 50: Add more faculty
- Line 100: Add more students
- Line 200: Modify attendance patterns

Then re-run:
```bash
npm run seed:demo
```

## 📊 Verify Seeding

Check in MongoDB:
```javascript
use student_tracker

db.users.countDocuments()      // Should be 29
db.students.countDocuments()   // Should be 20
db.faculty.countDocuments()    // Should be 8
db.attendance.countDocuments() // Should be ~5,400
```

## 🎉 Next Steps

1. ✅ Explore the admin dashboard
2. ✅ Mark attendance as faculty
3. ✅ View attendance as student
4. ✅ Generate reports
5. ✅ Test all features
6. ✅ Customize for your needs

## 💡 Pro Tips

- Use `admin` account to see everything
- Use different faculty accounts to see role-based access
- Use different student accounts to see course-specific data
- Check the audit logs to see all activities
- Generate reports to see data visualization

## 🆘 Need Help?

1. Check console output for errors
2. Verify MongoDB connection
3. Ensure all dependencies installed
4. Review documentation files
5. Check MongoDB Atlas network settings

---

**You're all set! Start exploring! 🎊**

## 📞 Quick Reference

| What | Command |
|------|---------|
| Install | `npm install` |
| Seed Subjects | `npm run seed:subjects` |
| Seed Demo Data | `npm run seed:demo` |
| Seed Everything | `npm run seed:all` |
| Start Backend | `cd backend && mvn spring-boot:run` |
| Start Frontend | `cd frontend && npm run dev` |

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Faculty | rajesh.kumar | faculty123 |
| Student | cse21a001 | student123 |
