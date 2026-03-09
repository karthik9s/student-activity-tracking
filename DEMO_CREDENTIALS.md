# Demo Credentials Quick Reference

## 🔐 Login Credentials

### Admin Access
```
Username: admin
Password: admin123
Email: admin@cvr.ac.in
Role: ADMIN
```

### Faculty Access (All use password: faculty123)

| Username | Name | Email | Department | Specialization |
|----------|------|-------|------------|----------------|
| rajesh.kumar | Dr. Rajesh Kumar | rajesh.kumar@cvr.ac.in | CSE | Data Structures & Algorithms |
| priya.sharma | Prof. Priya Sharma | priya.sharma@cvr.ac.in | CSE | Database Management |
| amit.patel | Dr. Amit Patel | amit.patel@cvr.ac.in | CSE | Web Technologies |
| sneha.reddy | Prof. Sneha Reddy | sneha.reddy@cvr.ac.in | CSE-AIML | Machine Learning |
| vikram.singh | Dr. Vikram Singh | vikram.singh@cvr.ac.in | CSE-AIML | Deep Learning |
| anita.desai | Prof. Anita Desai | anita.desai@cvr.ac.in | CSE-DS | Data Analytics |
| karthik.iyer | Dr. Karthik Iyer | karthik.iyer@cvr.ac.in | CSE-DS | Big Data |
| meera.nair | Prof. Meera Nair | meera.nair@cvr.ac.in | CSE | Operating Systems |

### Student Access (All use password: student123)

#### CSE Students

**Year 1, Semester 1, Section A**
| Username | Name | Roll Number | Email |
|----------|------|-------------|-------|
| cse21a001 | Aarav Sharma | CSE21A001 | cse21a001@cvr.ac.in |
| cse21a002 | Diya Patel | CSE21A002 | cse21a002@cvr.ac.in |
| cse21a003 | Arjun Reddy | CSE21A003 | cse21a003@cvr.ac.in |
| cse21a004 | Ananya Kumar | CSE21A004 | cse21a004@cvr.ac.in |
| cse21a005 | Rohan Gupta | CSE21A005 | cse21a005@cvr.ac.in |

**Year 2, Semester 3, Section A**
| Username | Name | Roll Number | Email |
|----------|------|-------------|-------|
| cse20a001 | Ishaan Verma | CSE20A001 | cse20a001@cvr.ac.in |
| cse20a002 | Kavya Singh | CSE20A002 | cse20a002@cvr.ac.in |
| cse20a003 | Aditya Joshi | CSE20A003 | cse20a003@cvr.ac.in |

#### CSE-AIML Students

**Year 1, Semester 1, Section A**
| Username | Name | Roll Number | Email |
|----------|------|-------------|-------|
| aiml21a001 | Sai Krishna | AIML21A001 | aiml21a001@cvr.ac.in |
| aiml21a002 | Priya Menon | AIML21A002 | aiml21a002@cvr.ac.in |
| aiml21a003 | Rahul Nair | AIML21A003 | aiml21a003@cvr.ac.in |
| aiml21a004 | Sneha Iyer | AIML21A004 | aiml21a004@cvr.ac.in |

**Year 2, Semester 3, Section A**
| Username | Name | Roll Number | Email |
|----------|------|-------------|-------|
| aiml20a001 | Kiran Kumar | AIML20A001 | aiml20a001@cvr.ac.in |
| aiml20a002 | Divya Rao | AIML20A002 | aiml20a002@cvr.ac.in |

#### CSE-DS Students

**Year 1, Semester 1, Section A**
| Username | Name | Roll Number | Email |
|----------|------|-------------|-------|
| ds21a001 | Varun Reddy | DS21A001 | ds21a001@cvr.ac.in |
| ds21a002 | Lakshmi Devi | DS21A002 | ds21a002@cvr.ac.in |
| ds21a003 | Harish Babu | DS21A003 | ds21a003@cvr.ac.in |
| ds21a004 | Pooja Reddy | DS21A004 | ds21a004@cvr.ac.in |

**Year 2, Semester 3, Section A**
| Username | Name | Roll Number | Email |
|----------|------|-------------|-------|
| ds20a001 | Naveen Kumar | DS20A001 | ds20a001@cvr.ac.in |
| ds20a002 | Swathi Sharma | DS20A002 | ds20a002@cvr.ac.in |

## 📊 Data Summary

- **Total Users**: 29 (1 Admin + 8 Faculty + 20 Students)
- **Courses**: 3 (CSE, CSE-AIML, CSE-DS)
- **Subjects**: 106 (across 8 semesters)
- **Class Allocations**: ~18
- **Attendance Records**: ~5,400

## 🚀 Quick Start

### 1. Install Dependencies
```bash
npm install
```

### 2. Seed Database
```bash
# Seed courses and subjects first
npm run seed:subjects

# Then seed demo data (users, allocations, attendance)
npm run seed:demo

# Or run both at once
npm run seed:all
```

### 3. Update MongoDB Connection
Edit both seeding scripts and update the connection string:
```javascript
const MONGODB_URI = 'mongodb+srv://your-username:your-password@your-cluster.mongodb.net/student_tracker?retryWrites=true&w=majority';
```

## 🧪 Testing Scenarios

### Scenario 1: Faculty Marks Attendance
1. Login as: `rajesh.kumar` / `faculty123`
2. Navigate to: Attendance Marking
3. Select assigned subject and date
4. Mark attendance for students
5. Submit

### Scenario 2: Student Views Attendance
1. Login as: `cse21a001` / `student123`
2. Navigate to: My Attendance
3. View subject-wise attendance
4. Check attendance percentage

### Scenario 3: Admin Manages System
1. Login as: `admin` / `admin123`
2. View all students, faculty, courses
3. Create new class allocations
4. Generate reports
5. View audit logs

### Scenario 4: Faculty Enters Performance
1. Login as: `priya.sharma` / `faculty123`
2. Navigate to: Performance Entry
3. Select subject and assessment type
4. Enter marks for students
5. Submit

### Scenario 5: Cross-Course Testing
1. Test CSE student: `cse21a001` / `student123`
2. Test AIML student: `aiml21a001` / `student123`
3. Test DS student: `ds21a001` / `student123`
4. Verify each sees only their course subjects

## 📝 Notes

- All passwords are for demo purposes only
- Change passwords in production
- Attendance data is randomly generated with realistic patterns
- Class allocations cover Semester 1 and Semester 3
- Academic Year: 2023-2024

## 🔒 Security Reminder

⚠️ **Important**: These are demo credentials. In production:
- Use strong, unique passwords
- Implement password reset functionality
- Enable two-factor authentication
- Use environment variables for sensitive data
- Implement proper session management
- Add rate limiting for login attempts

## 📞 Support

If you need to reset the demo data:
```bash
# Re-run the seeding scripts
npm run seed:all
```

This will clear existing data and create fresh demo data.

---

**Ready to test! 🎉**
