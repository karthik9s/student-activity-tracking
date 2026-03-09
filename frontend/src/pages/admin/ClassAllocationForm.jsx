import React, { useState, useEffect } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import { createAllocation, updateAllocation } from '../../api/endpoints/allocationApi';
import { getAllFaculty } from '../../api/endpoints/facultyApi';
import { getAllCourses } from '../../api/endpoints/courseApi';
import { getSubjectsByCourse } from '../../api/endpoints/subjectApi';
import './ClassAllocationForm.css';

const validationSchema = Yup.object({
  facultyId: Yup.string().required('Faculty is required'),
  courseId: Yup.string().required('Course is required'),
  subjectId: Yup.string().required('Subject is required'),
  year: Yup.number().required('Year is required').min(1).max(5),
  section: Yup.string().required('Section is required'),
  semester: Yup.number().required('Semester is required').min(1).max(8),
  academicYear: Yup.string().required('Academic year is required')
});

const ClassAllocationForm = ({ allocation, onClose, onSuccess }) => {
  const [faculty, setFaculty] = useState([]);
  const [courses, setCourses] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchFaculty();
    fetchCourses();
  }, []);

  const fetchFaculty = async () => {
    try {
      const response = await getAllFaculty({ size: 1000 });
      setFaculty(response.data.content);
    } catch (error) {
      console.error('Failed to fetch faculty', error);
    }
  };

  const fetchCourses = async () => {
    try {
      const response = await getAllCourses({ size: 100 });
      setCourses(response.data.content);
    } catch (error) {
      console.error('Failed to fetch courses', error);
    }
  };

  const fetchSubjects = async (courseId) => {
    try {
      const response = await getSubjectsByCourse(courseId);
      setSubjects(response.data);
    } catch (error) {
      console.error('Failed to fetch subjects', error);
    }
  };

  const initialValues = {
    facultyId: allocation?.facultyId || '',
    courseId: allocation?.courseId || '',
    subjectId: allocation?.subjectId || '',
    year: allocation?.year || '',
    section: allocation?.section || '',
    semester: allocation?.semester || '',
    academicYear: allocation?.academicYear || '',
    isActive: allocation?.isActive !== undefined ? allocation.isActive : true
  };

  const handleSubmit = async (values, { setSubmitting }) => {
    try {
      setLoading(true);
      if (allocation) {
        await updateAllocation(allocation.id, values);
        toast.success('Allocation updated successfully');
      } else {
        await createAllocation(values);
        toast.success('Allocation created successfully');
      }
      onSuccess();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save allocation');
      console.error(error);
    } finally {
      setLoading(false);
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>{allocation ? 'Edit Allocation' : 'Create Allocation'}</h2>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>

        <Formik
          initialValues={initialValues}
          validationSchema={validationSchema}
          onSubmit={handleSubmit}
          enableReinitialize
        >
          {({ values, setFieldValue, isSubmitting }) => (
            <Form className="allocation-form">
              <div className="form-group">
                <label htmlFor="facultyId">Faculty *</label>
                <Field as="select" name="facultyId" className="form-control">
                  <option value="">Select Faculty</option>
                  {faculty.map(f => (
                    <option key={f.id} value={f.id}>
                      {f.firstName} {f.lastName} ({f.employeeId})
                    </option>
                  ))}
                </Field>
                <ErrorMessage name="facultyId" component="div" className="error-message" />
              </div>

              <div className="form-group">
                <label htmlFor="courseId">Course *</label>
                <Field
                  as="select"
                  name="courseId"
                  className="form-control"
                  onChange={(e) => {
                    const courseId = e.target.value;
                    setFieldValue('courseId', courseId);
                    setFieldValue('subjectId', '');
                    if (courseId) {
                      fetchSubjects(courseId);
                    } else {
                      setSubjects([]);
                    }
                  }}
                >
                  <option value="">Select Course</option>
                  {courses.map(c => (
                    <option key={c.id} value={c.id}>
                      {c.name} ({c.code})
                    </option>
                  ))}
                </Field>
                <ErrorMessage name="courseId" component="div" className="error-message" />
              </div>

              <div className="form-group">
                <label htmlFor="subjectId">Subject *</label>
                <Field as="select" name="subjectId" className="form-control" disabled={!values.courseId}>
                  <option value="">Select Subject</option>
                  {subjects.map(s => (
                    <option key={s.id} value={s.id}>
                      {s.name} ({s.code})
                    </option>
                  ))}
                </Field>
                <ErrorMessage name="subjectId" component="div" className="error-message" />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="year">Year *</label>
                  <Field type="number" name="year" className="form-control" min="1" max="5" />
                  <ErrorMessage name="year" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="section">Section *</label>
                  <Field type="text" name="section" className="form-control" placeholder="A, B, C..." />
                  <ErrorMessage name="section" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="semester">Semester *</label>
                  <Field type="number" name="semester" className="form-control" min="1" max="8" />
                  <ErrorMessage name="semester" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="academicYear">Academic Year *</label>
                  <Field type="text" name="academicYear" className="form-control" placeholder="2024-2025" />
                  <ErrorMessage name="academicYear" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-group">
                <label className="checkbox-label">
                  <Field type="checkbox" name="isActive" />
                  <span>Active</span>
                </label>
              </div>

              <div className="form-actions">
                <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={isSubmitting || loading}>
                  {loading ? 'Saving...' : 'Save'}
                </button>
              </div>
            </Form>
          )}
        </Formik>
      </div>
    </div>
  );
};

export default ClassAllocationForm;
