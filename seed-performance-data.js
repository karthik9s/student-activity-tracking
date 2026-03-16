// Seed Sample Performance Data
// This script adds performance records for students based on existing class allocations
// Run: node seed-performance-data.js

const { MongoClient } = require('mongodb');

const MONGODB_URI = 'mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker?retryWrites=true&w=majority';

function calculateGrade(percentage) {
  if (percentage >= 90) return 'A+';
  if (percentage >= 80) return 'A';
  if (percentage >= 70) return 'B+';
  if (percentage >= 60) return 'B';
  if (percentage >= 50) return 'C';
  if (percentage >= 40) return 'D';
  return 'F';
}

function randomMarks(min, max) {
  return parseFloat((Math.random() * (max - min) + min).toFixed(1));
}

// Realistic marks ranges per exam type
const examConfigs = [
  { examType: 'INTERNAL',    totalMarks: 30,  minPct: 40, maxPct: 100 },
  { examType: 'ASSIGNMENT',  totalMarks: 20,  minPct: 50, maxPct: 100 },
  { examType: 'EXAM',        totalMarks: 100, minPct: 35, maxPct: 95  },
  { examType: 'FINAL',       totalMarks: 75,  minPct: 35, maxPct: 95  },
];

async function seedPerformance() {
  const client = new MongoClient(MONGODB_URI);

  try {
    await client.connect();
    console.log('Connected to MongoDB');

    const db = client.db('student_tracker');

    const studentsCol    = db.collection('students');
    const allocationsCol = db.collection('class_allocations');
    const performanceCol = db.collection('performance');
    const facultyCol     = db.collection('faculty');

    // Load all data
    const students    = await studentsCol.find({ isActive: true }).toArray();
    const allocations = await allocationsCol.find({ isActive: true }).toArray();
    const faculty     = await facultyCol.find({}).toArray();

    if (students.length === 0) {
      console.error('No students found. Run seed-complete-demo-data.js first.');
      return;
    }
    if (allocations.length === 0) {
      console.error('No class allocations found. Run seed-complete-demo-data.js first.');
      return;
    }

    console.log(`Found ${students.length} students, ${allocations.length} allocations`);

    // Clear existing performance data
    await performanceCol.deleteMany({});
    console.log('Cleared existing performance records');

    const records = [];
    const now = new Date();

    for (const allocation of allocations) {
      // Find students matching this allocation
      const matchingStudents = students.filter(s =>
        s.courseId === allocation.courseId &&
        s.year     === allocation.year &&
        s.section  === allocation.section
      );

      if (matchingStudents.length === 0) continue;

      // Find faculty name for createdBy
      const fac = faculty.find(f => f._id.toString() === allocation.facultyId);
      const createdBy = fac ? `${fac.firstName} ${fac.lastName}` : 'Faculty';

      for (const student of matchingStudents) {
        // Give each student a "performance profile" (weak / average / strong)
        const profile = Math.random();
        const boost = profile > 0.7 ? 15 : profile < 0.2 ? -15 : 0;

        for (const exam of examConfigs) {
          const minPct = Math.min(100, Math.max(0, exam.minPct + boost));
          const maxPct = Math.min(100, Math.max(minPct + 5, exam.maxPct + boost));

          const percentage  = randomMarks(minPct, maxPct);
          const marksObtained = parseFloat(((percentage / 100) * exam.totalMarks).toFixed(1));
          const grade       = calculateGrade(percentage);

          // Stagger dates: INTERNAL oldest, FINAL most recent
          const daysAgo = exam.examType === 'INTERNAL'   ? 60
                        : exam.examType === 'ASSIGNMENT'  ? 45
                        : exam.examType === 'EXAM'        ? 30
                        : 15; // FINAL

          const createdAt = new Date(now);
          createdAt.setDate(createdAt.getDate() - daysAgo);

          records.push({
            studentId:    student._id.toString(),
            subjectId:    allocation.subjectId,
            facultyId:    allocation.facultyId,
            courseId:     allocation.courseId,
            year:         allocation.year,
            section:      allocation.section,
            semester:     allocation.semester ? String(allocation.semester) : null,
            examType:     exam.examType,
            marksObtained,
            totalMarks:   parseFloat(exam.totalMarks.toFixed(1)),
            percentage:   parseFloat(percentage.toFixed(2)),
            grade,
            remarks:      grade === 'F' ? 'Needs improvement' : grade === 'A+' ? 'Excellent' : null,
            createdBy,
            createdAt,
            updatedAt:    createdAt,
          });
        }
      }
    }

    if (records.length === 0) {
      console.error('No performance records generated. Check student/allocation data.');
      return;
    }

    // Insert in batches to catch individual errors
    let inserted = 0;
    for (const record of records) {
      try {
        await performanceCol.insertOne(record);
        inserted++;
      } catch (e) {
        console.error('Failed record:', JSON.stringify({
          examType: record.examType,
          marksObtained: record.marksObtained,
          totalMarks: record.totalMarks,
          percentage: record.percentage,
          error: e.message
        }));
        break;
      }
    }
    console.log(`\n✓ Inserted ${inserted} performance records`);

    // Summary
    const byExamType = {};
    records.forEach(r => {
      byExamType[r.examType] = (byExamType[r.examType] || 0) + 1;
    });
    console.log('\nBreakdown by exam type:');
    Object.entries(byExamType).forEach(([type, count]) => {
      console.log(`  ${type}: ${count} records`);
    });

    const byGrade = {};
    records.forEach(r => {
      byGrade[r.grade] = (byGrade[r.grade] || 0) + 1;
    });
    console.log('\nGrade distribution:');
    ['A+','A','B+','B','C','D','F'].forEach(g => {
      if (byGrade[g]) console.log(`  ${g}: ${byGrade[g]}`);
    });

    console.log('\n✓ Performance seeding complete!');
    console.log('Students can now log in and view their performance data.');

  } catch (err) {
    console.error('Error:', err.message);
  } finally {
    await client.close();
  }
}

seedPerformance();
