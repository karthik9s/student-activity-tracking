const { MongoClient } = require('mongodb');
const bcrypt = require('bcryptjs');

const uri = 'mongodb+srv://admin:admin@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority&appName=Cluster0';

async function seedData() {
  const client = new MongoClient(uri);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    
    // Clear existing data (except users)
    console.log('\nClearing existing data...');
    await db.collection('courses').deleteMany({});
    await db.collection('subjects').deleteMany({});
    await db.collection('faculty').deleteMany({});
    await db.collection('students').deleteMany({});
    await db.collection('class_allocations').deleteMany({});
    
    // Create Courses
    console.log('\nCreating courses...');
    const courses = [
      {
        code: 'BTECH-CSE',
        name: 'Bachelor of Technology in Computer Science',
        duration: 4,
        totalSemesters: 8,
        isActive: true
      },
      {
        code: 'BTECH-ECE',
        name: 'Bachelor of Technology in Electronics',
        duration: 4,
        totalSemesters: 8,
        isActive: true
      },
      {
        code: 'BCA',
        name: 'Bachelor of Computer Applications',
        duration: 3,
        totalSemesters: 6,
        isActive: true
      }
    ];
    
    const courseResult = await db.collection('courses').insertMany(courses);
    const courseIds = Object.values(courseResult.insertedIds);
    console.log(`Created ${courseIds.length} courses`);
    
    // Create Subjects for BTECH-CSE
    console.log('\nCreating subjects...');
    const subjects = [
      {
        code: 'CS101',
        name: 'Introduction to Programming',
        credits: 4,
        semester: 1,
        courseId: courseIds[0].toString(),
        isActive: true
      },
      {
        code: 'CS102',
        name: 'Data Structures',
        credits: 4,
        semester: 2,
        courseId: courseIds[0].toString(),
        isActive: true
      },
      {
        code: 'CS201',
        name: 'Database Management Systems',
        credits: 4,
        semester: 3,
        courseId: courseIds[0].toString(),
        isActive: true
      },
      {
        code: 'CS202',
        name: 'Operating Systems',
        credits: 4,
        semester: 4,
        courseId: courseIds[0].toString(),
        isActive: true
      },
      {
        code: 'CS301',
        name: 'Computer Networks',
        credits: 4,
        semester: 5,
        courseId: courseIds[0].toString(),
        isActive: true
      },
      {
        code: 'MATH101',
        name: 'Engineering Mathematics I',
        credits: 4,
        semester: 1,
        courseId: courseIds[0].toString(),
        isActive: true
      }
    ];
    
    const subjectResult = await db.collection('subjects').insertMany(subjects);
    const subjectIds = Object.values(subjectResult.insertedIds);
    console.log(`Created ${subjectIds.length} subjects`);
    
    // Create Faculty with Users
    console.log('\nCreating faculty...');
    const facultyData = [
      {
        employeeId: 'FAC001',
        firstName: 'John',
        lastName: 'Smith',
        email: 'john.smith@college.com',
        phone: '9876543210',
        department: 'Computer Science',
        designation: 'Professor',
        dateOfJoining: new Date('2015-07-01'),
        qualification: 'PhD in Computer Science',
        isActive: true
      },
      {
        employeeId: 'FAC002',
        firstName: 'Sarah',
        lastName: 'Johnson',
        email: 'sarah.johnson@college.com',
        phone: '9876543211',
        department: 'Computer Science',
        designation: 'Associate Professor',
        dateOfJoining: new Date('2017-08-15'),
        qualification: 'PhD in Software Engineering',
        isActive: true
      },
      {
        employeeId: 'FAC003',
        firstName: 'Michael',
        lastName: 'Brown',
        email: 'michael.brown@college.com',
        phone: '9876543212',
        department: 'Computer Science',
        designation: 'Assistant Professor',
        dateOfJoining: new Date('2019-06-01'),
        qualification: 'MTech in Computer Science',
        isActive: true
      }
    ];
    
    const facultyResult = await db.collection('faculty').insertMany(facultyData);
    const facultyIds = Object.values(facultyResult.insertedIds);
    console.log(`Created ${facultyIds.length} faculty members`);
    
    // Create Faculty Users
    console.log('\nCreating faculty user accounts...');
    const hashedPassword = await bcrypt.hash('faculty123', 12);
    const facultyUsers = facultyData.map((fac, index) => ({
      email: fac.email,
      password: hashedPassword,
      role: 'ROLE_FACULTY',
      isActive: true,
      facultyId: facultyIds[index].toString(),
      createdAt: new Date(),
      updatedAt: new Date()
    }));
    
    await db.collection('users').insertMany(facultyUsers);
    console.log(`Created ${facultyUsers.length} faculty user accounts`);
    
    // Create Students with Users
    console.log('\nCreating students...');
    const studentsData = [
      {
        rollNumber: 'CSE2024001',
        firstName: 'Alice',
        lastName: 'Williams',
        email: 'alice.williams@student.com',
        phone: '9123456780',
        dateOfBirth: new Date('2005-03-15'),
        gender: 'Female',
        address: '123 Main Street, City',
        courseId: courseIds[0].toString(),
        semester: 3,
        year: 2,
        section: 'A',
        admissionDate: new Date('2024-08-01'),
        isActive: true
      },
      {
        rollNumber: 'CSE2024002',
        firstName: 'Bob',
        lastName: 'Davis',
        email: 'bob.davis@student.com',
        phone: '9123456781',
        dateOfBirth: new Date('2005-05-20'),
        gender: 'Male',
        address: '456 Oak Avenue, City',
        courseId: courseIds[0].toString(),
        semester: 3,
        year: 2,
        section: 'A',
        admissionDate: new Date('2024-08-01'),
        isActive: true
      },
      {
        rollNumber: 'CSE2024003',
        firstName: 'Carol',
        lastName: 'Martinez',
        email: 'carol.martinez@student.com',
        phone: '9123456782',
        dateOfBirth: new Date('2005-07-10'),
        gender: 'Female',
        address: '789 Pine Road, City',
        courseId: courseIds[0].toString(),
        semester: 3,
        year: 2,
        section: 'B',
        admissionDate: new Date('2024-08-01'),
        isActive: true
      },
      {
        rollNumber: 'CSE2024004',
        firstName: 'David',
        lastName: 'Garcia',
        email: 'david.garcia@student.com',
        phone: '9123456783',
        dateOfBirth: new Date('2005-09-25'),
        gender: 'Male',
        address: '321 Elm Street, City',
        courseId: courseIds[0].toString(),
        semester: 3,
        year: 2,
        section: 'B',
        admissionDate: new Date('2024-08-01'),
        isActive: true
      },
      {
        rollNumber: 'CSE2024005',
        firstName: 'Emma',
        lastName: 'Rodriguez',
        email: 'emma.rodriguez@student.com',
        phone: '9123456784',
        dateOfBirth: new Date('2005-11-30'),
        gender: 'Female',
        address: '654 Maple Drive, City',
        courseId: courseIds[0].toString(),
        semester: 3,
        year: 2,
        section: 'A',
        admissionDate: new Date('2024-08-01'),
        isActive: true
      }
    ];
    
    const studentResult = await db.collection('students').insertMany(studentsData);
    const studentIds = Object.values(studentResult.insertedIds);
    console.log(`Created ${studentIds.length} students`);
    
    // Create Student Users
    console.log('\nCreating student user accounts...');
    const studentHashedPassword = await bcrypt.hash('student123', 12);
    const studentUsers = studentsData.map((student, index) => ({
      email: student.email,
      password: studentHashedPassword,
      role: 'ROLE_STUDENT',
      isActive: true,
      studentId: studentIds[index].toString(),
      createdAt: new Date(),
      updatedAt: new Date()
    }));
    
    await db.collection('users').insertMany(studentUsers);
    console.log(`Created ${studentUsers.length} student user accounts`);
    
    // Create Class Allocations
    console.log('\nCreating class allocations...');
    const classAllocations = [
      {
        facultyId: facultyIds[0].toString(),
        subjectId: subjectIds[0].toString(), // CS101
        courseId: courseIds[0].toString(),
        semester: 1,
        section: 'A',
        academicYear: '2024-2025',
        isActive: true
      },
      {
        facultyId: facultyIds[1].toString(),
        subjectId: subjectIds[1].toString(), // CS102
        courseId: courseIds[0].toString(),
        semester: 2,
        section: 'A',
        academicYear: '2024-2025',
        isActive: true
      },
      {
        facultyId: facultyIds[2].toString(),
        subjectId: subjectIds[2].toString(), // CS201
        courseId: courseIds[0].toString(),
        semester: 3,
        section: 'A',
        academicYear: '2024-2025',
        isActive: true
      }
    ];
    
    await db.collection('class_allocations').insertMany(classAllocations);
    console.log(`Created ${classAllocations.length} class allocations`);
    
    console.log('\n✅ Sample data seeded successfully!');
    console.log('\n📋 Summary:');
    console.log(`   - ${courses.length} Courses`);
    console.log(`   - ${subjects.length} Subjects`);
    console.log(`   - ${facultyData.length} Faculty members`);
    console.log(`   - ${studentsData.length} Students`);
    console.log(`   - ${classAllocations.length} Class allocations`);
    
    console.log('\n🔑 Login Credentials:');
    console.log('   Admin:');
    console.log('     Email: admin@college.com');
    console.log('     Password: admin123');
    console.log('\n   Faculty (any of these):');
    console.log('     Email: john.smith@college.com');
    console.log('     Email: sarah.johnson@college.com');
    console.log('     Email: michael.brown@college.com');
    console.log('     Password: faculty123');
    console.log('\n   Students (any of these):');
    console.log('     Email: alice.williams@student.com');
    console.log('     Email: bob.davis@student.com');
    console.log('     Email: carol.martinez@student.com');
    console.log('     Password: student123');
    
  } catch (error) {
    console.error('Error seeding data:', error);
  } finally {
    await client.close();
    console.log('\nDisconnected from MongoDB');
  }
}

seedData();
