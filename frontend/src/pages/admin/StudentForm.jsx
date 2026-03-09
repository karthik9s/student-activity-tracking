import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import './StudentForm.css';

const studentSchema = Yup.object().shape({
  rollNumber: Yup.string().required('Roll number is required'),
  firstName: Yup.string().required('First name is required'),
  lastName: Yup.string().required('Last name is required'),
  email: Yup.string().email('Invalid email').required('Email is required'),
  phone: Yup.string().matches(/^[0-9]{10}$/, 'Phone must be 10 digits'),
  dateOfBirth: Yup.date().max(new Date(), 'Date must be in the past'),
  courseId: Yup.string().required('Course is required'),
  year: Yup.number().min(1).max(4).required('Year is required'),
  section: Yup.string().required('Section is required'),
  isActive: Yup.boolean(),
});

const StudentForm = ({ student, onSubmit, onCancel }) => {
  const initialValues = student || {
    rollNumber: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    dateOfBirth: '',
    courseId: '',
    year: 1,
    section: '',
    isActive: true,
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>{student ? 'Edit Student' : 'Add Student'}</h2>
          <button className="close-btn" onClick={onCancel}>×</button>
        </div>

        <Formik
          initialValues={initialValues}
          validationSchema={studentSchema}
          onSubmit={onSubmit}
        >
          {({ isSubmitting }) => (
            <Form className="student-form">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="rollNumber">Roll Number *</label>
                  <Field
                    type="text"
                    name="rollNumber"
                    id="rollNumber"
                    className="form-input"
                  />
                  <ErrorMessage name="rollNumber" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="email">Email *</label>
                  <Field
                    type="email"
                    name="email"
                    id="email"
                    className="form-input"
                  />
                  <ErrorMessage name="email" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="firstName">First Name *</label>
                  <Field
                    type="text"
                    name="firstName"
                    id="firstName"
                    className="form-input"
                  />
                  <ErrorMessage name="firstName" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="lastName">Last Name *</label>
                  <Field
                    type="text"
                    name="lastName"
                    id="lastName"
                    className="form-input"
                  />
                  <ErrorMessage name="lastName" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="phone">Phone</label>
                  <Field
                    type="text"
                    name="phone"
                    id="phone"
                    className="form-input"
                    placeholder="10 digits"
                  />
                  <ErrorMessage name="phone" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="dateOfBirth">Date of Birth</label>
                  <Field
                    type="date"
                    name="dateOfBirth"
                    id="dateOfBirth"
                    className="form-input"
                  />
                  <ErrorMessage name="dateOfBirth" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="courseId">Course ID *</label>
                  <Field
                    type="text"
                    name="courseId"
                    id="courseId"
                    className="form-input"
                    placeholder="Enter course ID"
                  />
                  <ErrorMessage name="courseId" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="year">Year *</label>
                  <Field as="select" name="year" id="year" className="form-input">
                    <option value={1}>1st Year</option>
                    <option value={2}>2nd Year</option>
                    <option value={3}>3rd Year</option>
                    <option value={4}>4th Year</option>
                  </Field>
                  <ErrorMessage name="year" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="section">Section *</label>
                  <Field
                    type="text"
                    name="section"
                    id="section"
                    className="form-input"
                    placeholder="e.g., A, B, C"
                  />
                  <ErrorMessage name="section" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="isActive">Status</label>
                  <Field as="select" name="isActive" id="isActive" className="form-input">
                    <option value={true}>Active</option>
                    <option value={false}>Inactive</option>
                  </Field>
                </div>
              </div>

              <div className="form-actions">
                <button type="button" className="btn-cancel" onClick={onCancel}>
                  Cancel
                </button>
                <button type="submit" className="btn-submit" disabled={isSubmitting}>
                  {isSubmitting ? 'Saving...' : 'Save'}
                </button>
              </div>
            </Form>
          )}
        </Formik>
      </div>
    </div>
  );
};

export default StudentForm;
