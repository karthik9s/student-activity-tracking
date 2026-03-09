// MongoDB Data Seeding Script for Courses and Subjects
// Run this script using: node seed-subjects-data.js

const { MongoClient } = require('mongodb');

// MongoDB connection string - UPDATE THIS with your connection string
const MONGODB_URI = 'mongodb+srv://surasani_karthik_reddy:mamayya123@cluster0.wzxks5z.mongodb.net/studentDB?retryWrites=true&w=majority';

const coursesData = [
  {
    code: 'CSE',
    name: 'Computer Science and Engineering',
    description: 'Bachelor of Technology in Computer Science and Engineering',
    duration: 4,
    department: 'CSE'
  },
  {
    code: 'CSE-AIML',
    name: 'Computer Science and Engineering - Artificial Intelligence and Machine Learning',
    description: 'Bachelor of Technology in CSE with specialization in AI & ML',
    duration: 4,
    department: 'CSE-AIML'
  },
  {
    code: 'CSE-DS',
    name: 'Computer Science and Engineering - Data Science',
    description: 'Bachelor of Technology in CSE with specialization in Data Science',
    duration: 4,
    department: 'CSE-DS'
  }
];

const subjectsData = {
  'CSE': [
    // Year 1, Semester 1
    { code: 'CSE101', name: 'Engineering Chemistry', semester: 1, credits: 4, type: 'THEORY' },
    { code: 'CSE102', name: 'Mathematics for Computing', semester: 1, credits: 4, type: 'THEORY' },
    { code: 'CSE103', name: 'Problem Solving through C', semester: 1, credits: 4, type: 'THEORY' },
    { code: 'CSE104', name: 'Computer Aided Engineering Drawing', semester: 1, credits: 3, type: 'LAB' },
    { code: 'CSE105', name: 'Essentials of System and Web Interfacing', semester: 1, credits: 3, type: 'LAB' },
    
    // Year 1, Semester 2
    { code: 'CSE201', name: 'Applied Physics', semester: 2, credits: 4, type: 'THEORY' },
    { code: 'CSE202', name: 'Applied Linear Algebra', semester: 2, credits: 4, type: 'THEORY' },
    { code: 'CSE203', name: 'English for Skill Enhancement', semester: 2, credits: 3, type: 'THEORY' },
    { code: 'CSE204', name: 'Data Structures through C', semester: 2, credits: 4, type: 'THEORY' },
    { code: 'CSE205', name: 'Python for Computing', semester: 2, credits: 4, type: 'LAB' },
    { code: 'CSE206', name: 'Environmental Science', semester: 2, credits: 2, type: 'THEORY' },
    
    // Year 2, Semester 3
    { code: 'CSE301', name: 'Computer Oriented Statistical Methods', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'CSE302', name: 'Digital Electronics', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'CSE303', name: 'Basic Electrical & Electronics Engineering', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'CSE304', name: 'Discrete Mathematics', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'CSE305', name: 'Object Oriented Programming through Java', semester: 3, credits: 4, type: 'THEORY' },
    
    // Year 2, Semester 4
    { code: 'CSE401', name: 'Computer Organization', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'CSE402', name: 'Advanced Data Structures through Java', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'CSE403', name: 'Database Management Systems', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'CSE404', name: 'Software Engineering', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'CSE405', name: 'Operating Systems', semester: 4, credits: 4, type: 'THEORY' },
    
    // Year 3, Semester 5
    { code: 'CSE501', name: 'Computer Networking', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'CSE502', name: 'Algorithms Design and Analysis', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'CSE503', name: 'Web Technologies', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'CSE504', name: 'Data Science', semester: 5, credits: 4, type: 'THEORY' },
    
    // Year 3, Semester 6
    { code: 'CSE601', name: 'Artificial Intelligence and Machine Learning', semester: 6, credits: 4, type: 'THEORY' },
    { code: 'CSE602', name: 'Full Stack Development', semester: 6, credits: 4, type: 'THEORY' },
    { code: 'CSE603', name: 'Cloud Computing and DevOps', semester: 6, credits: 4, type: 'THEORY' },
    { code: 'CSE604', name: 'Automata Theory and Compiler Design', semester: 6, credits: 4, type: 'THEORY' },
    
    // Year 4, Semester 7
    { code: 'CSE701', name: 'Linux Programming', semester: 7, credits: 4, type: 'THEORY' },
    { code: 'CSE702', name: 'Business Economics and Financial Analysis', semester: 7, credits: 3, type: 'THEORY' },
    
    // Year 4, Semester 8
    { code: 'CSE801', name: 'Organizational Behaviour', semester: 8, credits: 3, type: 'THEORY' },
    { code: 'CSE802', name: 'Generative AI', semester: 8, credits: 4, type: 'THEORY' }
  ],
  
  'CSE-AIML': [
    // Year 1, Semester 1
    { code: 'AIML101', name: 'Applied Physics', semester: 1, credits: 4, type: 'THEORY' },
    { code: 'AIML102', name: 'Mathematics for Computing', semester: 1, credits: 4, type: 'THEORY' },
    { code: 'AIML103', name: 'Essentials of System and Web Interfacing', semester: 1, credits: 3, type: 'LAB' },
    { code: 'AIML104', name: 'Problem Solving through C', semester: 1, credits: 4, type: 'THEORY' },
    { code: 'AIML105', name: 'English for Skill Enhancement', semester: 1, credits: 3, type: 'THEORY' },
    { code: 'AIML106', name: 'Environmental Science', semester: 1, credits: 2, type: 'THEORY' },
    
    // Year 1, Semester 2
    { code: 'AIML201', name: 'Engineering Chemistry', semester: 2, credits: 4, type: 'THEORY' },
    { code: 'AIML202', name: 'Data Structures through C', semester: 2, credits: 4, type: 'THEORY' },
    { code: 'AIML203', name: 'Python for Computing', semester: 2, credits: 4, type: 'LAB' },
    { code: 'AIML204', name: 'Applied Linear Algebra', semester: 2, credits: 4, type: 'THEORY' },
    { code: 'AIML205', name: 'Computer Aided Engineering Drawing', semester: 2, credits: 3, type: 'LAB' },
    
    // Year 2, Semester 3
    { code: 'AIML301', name: 'Object Oriented Programming through Java', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'AIML302', name: 'Discrete Mathematics', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'AIML303', name: 'Database Management Systems', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'AIML304', name: 'Digital Electronics and Design', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'AIML305', name: 'Mathematical and Statistical Foundations', semester: 3, credits: 4, type: 'THEORY' },
    
    // Year 2, Semester 4
    { code: 'AIML401', name: 'Advanced Data Structures through Java', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'AIML402', name: 'Computer Organization', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'AIML403', name: 'Operating Systems', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'AIML404', name: 'Algorithms Design and Analysis', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'AIML405', name: 'Software Engineering', semester: 4, credits: 4, type: 'THEORY' },
    
    // Year 3, Semester 5
    { code: 'AIML501', name: 'Computer Networking', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'AIML502', name: 'Web Technologies', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'AIML503', name: 'Automata Theory and Compiler Design', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'AIML504', name: 'Machine Learning', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'AIML505', name: 'Image Processing', semester: 5, credits: 4, type: 'THEORY' },
    
    // Year 3, Semester 6
    { code: 'AIML601', name: 'Data Analytics', semester: 6, credits: 4, type: 'THEORY' },
    { code: 'AIML602', name: 'Artificial Intelligence and Neural Networks', semester: 6, credits: 4, type: 'THEORY' },
    { code: 'AIML603', name: 'Deep Learning', semester: 6, credits: 4, type: 'THEORY' },
    { code: 'AIML604', name: 'Business Economics and Financial Analysis', semester: 6, credits: 3, type: 'THEORY' },
    
    // Year 4, Semester 7
    { code: 'AIML701', name: 'Natural Language Processing', semester: 7, credits: 4, type: 'THEORY' },
    { code: 'AIML702', name: 'Professional Practice Law and Ethics', semester: 7, credits: 3, type: 'THEORY' },
    { code: 'AIML703', name: 'Game Theory', semester: 7, credits: 3, type: 'THEORY' },
    
    // Year 4, Semester 8
    { code: 'AIML801', name: 'Cognitive Computing', semester: 8, credits: 4, type: 'THEORY' },
    { code: 'AIML802', name: 'Federated Machine Learning', semester: 8, credits: 4, type: 'THEORY' },
    { code: 'AIML803', name: 'Nature Inspired Computing', semester: 8, credits: 4, type: 'THEORY' },
    { code: 'AIML804', name: 'Conversational AI', semester: 8, credits: 4, type: 'THEORY' }
  ],
  
  'CSE-DS': [
    // Year 1, Semester 1
    { code: 'DS101', name: 'Applied Physics', semester: 1, credits: 4, type: 'THEORY' },
    { code: 'DS102', name: 'Mathematics for Computing', semester: 1, credits: 4, type: 'THEORY' },
    { code: 'DS103', name: 'Essentials of System and Web Interfacing', semester: 1, credits: 3, type: 'LAB' },
    { code: 'DS104', name: 'Problem Solving through C', semester: 1, credits: 4, type: 'THEORY' },
    { code: 'DS105', name: 'English for Skill Enhancement', semester: 1, credits: 3, type: 'THEORY' },
    { code: 'DS106', name: 'Environmental Science', semester: 1, credits: 2, type: 'THEORY' },
    
    // Year 1, Semester 2
    { code: 'DS201', name: 'Engineering Chemistry', semester: 2, credits: 4, type: 'THEORY' },
    { code: 'DS202', name: 'Data Structures through C', semester: 2, credits: 4, type: 'THEORY' },
    { code: 'DS203', name: 'Python for Computing', semester: 2, credits: 4, type: 'LAB' },
    { code: 'DS204', name: 'Applied Linear Algebra', semester: 2, credits: 4, type: 'THEORY' },
    { code: 'DS205', name: 'Computer Aided Engineering Drawing', semester: 2, credits: 3, type: 'LAB' },
    
    // Year 2, Semester 3
    { code: 'DS301', name: 'Object Oriented Programming through Java', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'DS302', name: 'Discrete Mathematics', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'DS303', name: 'Database Management Systems', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'DS304', name: 'Digital Electronics and Design', semester: 3, credits: 4, type: 'THEORY' },
    { code: 'DS305', name: 'Mathematical and Statistical Foundations', semester: 3, credits: 4, type: 'THEORY' },
    
    // Year 2, Semester 4
    { code: 'DS401', name: 'Advanced Data Structures through Java', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'DS402', name: 'Computer Organization', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'DS403', name: 'Operating Systems', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'DS404', name: 'Algorithms Design and Analysis', semester: 4, credits: 4, type: 'THEORY' },
    { code: 'DS405', name: 'Software Engineering', semester: 4, credits: 4, type: 'THEORY' },
    
    // Year 3, Semester 5
    { code: 'DS501', name: 'Computer Networking', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'DS502', name: 'Web Technologies', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'DS503', name: 'Introduction to Data Analysis', semester: 5, credits: 4, type: 'THEORY' },
    { code: 'DS504', name: 'Automata Theory and Compiler Design', semester: 5, credits: 4, type: 'THEORY' },
    
    // Year 3, Semester 6
    { code: 'DS601', name: 'Predictive Analytics', semester: 6, credits: 4, type: 'THEORY' },
    { code: 'DS602', name: 'Machine Learning', semester: 6, credits: 4, type: 'THEORY' },
    { code: 'DS603', name: 'Big Data Analytics', semester: 6, credits: 4, type: 'THEORY' },
    { code: 'DS604', name: 'Business Economics and Financial Analysis', semester: 6, credits: 3, type: 'THEORY' },
    
    // Year 4, Semester 7
    { code: 'DS701', name: 'Web and Social Media Analytics', semester: 7, credits: 4, type: 'THEORY' },
    { code: 'DS702', name: 'Organizational Behaviour', semester: 7, credits: 3, type: 'THEORY' },
    { code: 'DS703', name: 'Introduction to Deep Learning', semester: 7, credits: 4, type: 'THEORY' },
    
    // Year 4, Semester 8
    { code: 'DS801', name: 'Database Security', semester: 8, credits: 4, type: 'THEORY' },
    { code: 'DS802', name: 'Privacy Preserving in Data Mining', semester: 8, credits: 4, type: 'THEORY' },
    { code: 'DS803', name: 'Social Network Analysis', semester: 8, credits: 4, type: 'THEORY' },
    { code: 'DS804', name: 'Logistics and Supply Chain Management', semester: 8, credits: 3, type: 'THEORY' }
  ]
};

async function seedData() {
  const client = new MongoClient(MONGODB_URI);
  
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    
    const db = client.db('student_tracker');
    const coursesCollection = db.collection('courses');
    const subjectsCollection = db.collection('subjects');
    
    // Clear existing data
    console.log('\nClearing existing courses and subjects...');
    await coursesCollection.deleteMany({});
    await subjectsCollection.deleteMany({});
    
    // Insert courses
    console.log('\nInserting courses...');
    const courseResults = await coursesCollection.insertMany(coursesData);
    console.log(`Inserted ${courseResults.insertedCount} courses`);
    
    // Get course IDs
    const courses = await coursesCollection.find({}).toArray();
    const courseMap = {};
    courses.forEach(course => {
      courseMap[course.code] = course._id.toString();
    });
    
    // Insert subjects for each course
    console.log('\nInserting subjects...');
    let totalSubjects = 0;
    
    for (const [courseCode, subjects] of Object.entries(subjectsData)) {
      const courseId = courseMap[courseCode];
      const subjectsWithCourseId = subjects.map(subject => ({
        ...subject,
        courseId: courseId,
        description: `${subject.name} for ${courseCode}`,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date()
      }));
      
      const result = await subjectsCollection.insertMany(subjectsWithCourseId);
      console.log(`  - Inserted ${result.insertedCount} subjects for ${courseCode}`);
      totalSubjects += result.insertedCount;
    }
    
    console.log(`\nTotal subjects inserted: ${totalSubjects}`);
    
    // Summary
    console.log('\n=== Data Seeding Summary ===');
    console.log(`Courses: ${courseResults.insertedCount}`);
    console.log(`Subjects: ${totalSubjects}`);
    console.log('\nBreakdown by course:');
    for (const [courseCode, subjects] of Object.entries(subjectsData)) {
      console.log(`  ${courseCode}: ${subjects.length} subjects across 8 semesters`);
    }
    
    console.log('\n✓ Data seeding completed successfully!');
    
  } catch (error) {
    console.error('Error seeding data:', error);
  } finally {
    await client.close();
    console.log('\nMongoDB connection closed');
  }
}

// Run the seeding
seedData();
