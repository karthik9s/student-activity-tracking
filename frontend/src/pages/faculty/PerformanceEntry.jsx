import React, { useState, useEffect } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import { getMyAllocations } from '../../api/endpoints/allocationApi';
import { getFacultyStudents } from '../../api/endpoints/facultyApi';
import { addPerformance, getPerformanceBySubject } from '../../api/endpoints/performanceApi';
import './PerformanceEntry.css';

const validationSchema = Yup.object({
  studentId: Yup.string().required('Student is required'),
  examType: Yup.string().required('Assessment type is required'),
  marksObtained: Yup.number()
    .required('Marks obtained is required')
    .min(0, 'Marks cannot be negative')
    .test('max-marks', 'Marks obtained cannot exceed total marks', function(value) {
      return value <= this.parent.totalMarks;
    }),
  totalMarks: Yup.number()
    .required('Total marks is required')
    .min(1, 'Total marks must be at least 1'),
  remarks: Yup.string()
});

const PerformanceEntry = () => {
  const [allocations, setAllocations] = useState([]);
  const [students, setStudents] = useState([]);
  const [selectedAllocation, setSelectedAllocation] = useState(null);
  const [performanceRecords, setPerformanceRecords] = useState([]);
  const [loading, setLoading] = useState(false);

  const examTypes = [
    { value: 'INTERNAL', label: 'Internal Assessment' },
    { value: 'ASSIGNMENT', label: 'Assignment' },
    { value: 'EXAM', label: 'Semester Exam' }
  ];

  useEffect(() => {
    fetchAllocations();
  }, []);

  useEffect(() => {
    if (selectedAllocation) {
      fetchStudents();
      fetchPerformanceRecords();
    }
  }, [selectedAllocation]);

  const fetchAllocations = async () => {
    try {
      const response = await getMyAllocations();
      setAllocations(response.data);
    } catch (error) {
      toast.error('Failed to fetch allocations');
      console.error(error);
    }
  };

  const fetchStudents = async () => {
    try {
      const response = await getFacultyStudents(
        selectedAllocation.courseId,
        selectedAllocation.year,
        selectedAllocation.section
      );
      const filteredStudents = response.data.filter(
        s => s.isActive && !s.isDeleted
      );
      setStudents(filteredStudents);
    } catch (error) {
      toast.error('Failed to fetch students');
      console.error(error);
    }
  };

  const fetchPerformanceRecords = async () => {
    try {
      const response = await getPerformanceBySubject(selectedAllocation.subjectId);
      setPerformanceRecords(response.data);
    } catch (error) {
      console.error('Failed to fetch performance records', error);
    }
  };

  const handleAllocationChange = (e, setFieldValue) => {
    const allocation = allocations.find(a => a.id === e.target.value);
    setSelectedAllocation(allocation);
    setFieldValue('allocationId', e.target.value);
    setFieldValue('studentId', '');
    setStudents([]);
    setPerformanceRecords([]);
  };

  const calculateGrade = (marksObtained, totalMarks) => {
    const percentage = (marksObtained / totalMarks) * 100;
    if (percentage >= 90) return 'A+';
    if (percentage >= 80) return 'A';
    if (percentage >= 70) return 'B+';
    if (percentage >= 60) return 'B';
    if (percentage >= 50) return 'C';
    if (percentage >= 40) return 'D';
    return 'F';
  };

  const initialValues = {
    allocationId: '',
    studentId: '',
    examType: '',
    marksObtained: '',
    totalMarks: '',
    remarks: ''
  };

  const handleSubmit = async (values, { setSubmitting, resetForm }) => {
    if (!selectedAllocation) {
      toast.error('Please select a class');
      return;
    }

    try {
      setLoading(true);
      
      // Get faculty ID from the selected allocation
      const facultyId = selectedAllocation.facultyId;
      
      if (!facultyId) {
        toast.error('Faculty ID not found in allocation. Please contact admin.');
        return;
      }
      
      const performanceData = {
        studentId: values.studentId,
        subjectId: selectedAllocation.subjectId,
        facultyId: facultyId,
        courseId: selectedAllocation.courseId,
        year: selectedAllocation.year,
        section: selectedAllocation.section,
        semester: selectedAllocation.semester,
        examType: values.examType,
        marksObtained: parseFloat(values.marksObtained),
        totalMarks: parseFloat(values.totalMarks),
        remarks: values.remarks
      };

      await addPerformance(performanceData);
      toast.success('Performance added successfully');
      resetForm();
      // Refresh performance records to show the newly added entry
      await fetchPerformanceRecords();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add performance');
      console.error(error);
    } finally {
      setLoading(false);
      setSubmitting(false);
    }
  };

  return (
    <div className="performance-entry">
      <div className="page-header">
        <h1>Enter Performance</h1>
      </div>

      <div className="performance-form-container">
        <Formik
          initialValues={initialValues}
          validationSchema={validationSchema}
          onSubmit={handleSubmit}
          enableReinitialize
        >
          {({ values, setFieldValue, isSubmitting }) => (
            <Form>
              <div className="form-group">
                <label htmlFor="allocationId">Select Class *</label>
                <Field
                  as="select"
                  name="allocationId"
                  className="form-control"
                  onChange={(e) => handleAllocationChange(e, setFieldValue)}
                >
                  <option value="">Select Class</option>
                  {allocations.map(allocation => (
                    <option key={allocation.id} value={allocation.id}>
                      {allocation.courseName} - {allocation.subjectName} - Year {allocation.year} - Section {allocation.section}
                    </option>
                  ))}
                </Field>
              </div>

              {selectedAllocation && (
                <>
                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="studentId">Student *</label>
                      <Field as="select" name="studentId" className="form-control">
                        <option value="">Select Student</option>
                        {students.map(student => (
                          <option key={student.id} value={student.id}>
                            {student.rollNumber} - {student.firstName} {student.lastName}
                          </option>
                        ))}
                      </Field>
                      <ErrorMessage name="studentId" component="div" className="error-message" />
                    </div>

                    <div className="form-group">
                      <label htmlFor="examType">Assessment Type *</label>
                      <Field as="select" name="examType" className="form-control">
                        <option value="">Select Type</option>
                        {examTypes.map(type => (
                          <option key={type.value} value={type.value}>
                            {type.label}
                          </option>
                        ))}
                      </Field>
                      <ErrorMessage name="examType" component="div" className="error-message" />
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="marksObtained">Marks Obtained *</label>
                      <Field
                        type="number"
                        name="marksObtained"
                        className="form-control"
                        step="0.01"
                        min="0"
                      />
                      <ErrorMessage name="marksObtained" component="div" className="error-message" />
                    </div>

                    <div className="form-group">
                      <label htmlFor="totalMarks">Total Marks *</label>
                      <Field
                        type="number"
                        name="totalMarks"
                        className="form-control"
                        step="0.01"
                        min="1"
                      />
                      <ErrorMessage name="totalMarks" component="div" className="error-message" />
                    </div>
                  </div>

                  {values.marksObtained && values.totalMarks && (
                    <div className="grade-preview">
                      <div className="grade-info">
                        <span className="label">Percentage:</span>
                        <span className="value">
                          {((values.marksObtained / values.totalMarks) * 100).toFixed(2)}%
                        </span>
                      </div>
                      <div className="grade-info">
                        <span className="label">Grade:</span>
                        <span className={`value grade-${calculateGrade(values.marksObtained, values.totalMarks)}`}>
                          {calculateGrade(values.marksObtained, values.totalMarks)}
                        </span>
                      </div>
                    </div>
                  )}

                  <div className="form-group">
                    <label htmlFor="remarks">Remarks</label>
                    <Field
                      as="textarea"
                      name="remarks"
                      className="form-control"
                      rows="3"
                      placeholder="Optional remarks..."
                    />
                    <ErrorMessage name="remarks" component="div" className="error-message" />
                  </div>

                  <div className="form-actions">
                    <button
                      type="submit"
                      className="btn btn-primary"
                      disabled={isSubmitting || loading}
                    >
                      {loading ? 'Submitting...' : 'Submit Performance'}
                    </button>
                  </div>
                </>
              )}
            </Form>
          )}
        </Formik>
      </div>

      {selectedAllocation && performanceRecords.length > 0 && (
        <div className="performance-records">
          <h2>Recent Performance Records</h2>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Assessment Type</th>
                  <th>Marks</th>
                  <th>Percentage</th>
                  <th>Grade</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {performanceRecords.slice(0, 10).map(record => (
                  <tr key={record.id}>
                    <td>{record.studentName}</td>
                    <td>{record.examType}</td>
                    <td>{record.marksObtained} / {record.totalMarks}</td>
                    <td>{record.percentage.toFixed(2)}%</td>
                    <td>
                      <span className={`grade-badge grade-${record.grade}`}>
                        {record.grade}
                      </span>
                    </td>
                    <td>{new Date(record.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default PerformanceEntry;
