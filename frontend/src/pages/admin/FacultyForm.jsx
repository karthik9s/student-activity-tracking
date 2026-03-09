import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import './FacultyForm.css';

const facultySchema = Yup.object().shape({
  employeeId: Yup.string().required('Employee ID is required'),
  firstName: Yup.string().required('First name is required'),
  lastName: Yup.string().required('Last name is required'),
  email: Yup.string().email('Invalid email').required('Email is required'),
  phone: Yup.string().matches(/^[0-9]{10}$/, 'Phone must be 10 digits'),
  dateOfBirth: Yup.date().max(new Date(), 'Date must be in the past'),
  department: Yup.string().required('Department is required'),
  designation: Yup.string().required('Designation is required'),
  qualification: Yup.string(),
  experience: Yup.number().min(0, 'Experience cannot be negative'),
  joiningDate: Yup.date(),
  isActive: Yup.boolean(),
});

const FacultyForm = ({ faculty, onSubmit, onCancel }) => {
  const initialValues = faculty || {
    employeeId: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    dateOfBirth: '',
    department: '',
    designation: '',
    qualification: '',
    experience: 0,
    joiningDate: '',
    isActive: true,
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>{faculty ? 'Edit Faculty' : 'Add Faculty'}</h2>
          <button className="close-btn" onClick={onCancel}>×</button>
        </div>

        <Formik
          initialValues={initialValues}
          validationSchema={facultySchema}
          onSubmit={onSubmit}
        >
          {({ isSubmitting }) => (
            <Form className="faculty-form">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="employeeId">Employee ID *</label>
                  <Field
                    type="text"
                    name="employeeId"
                    id="employeeId"
                    className="form-input"
                  />
                  <ErrorMessage name="employeeId" component="div" className="error-message" />
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
                  <label htmlFor="department">Department *</label>
                  <Field
                    type="text"
                    name="department"
                    id="department"
                    className="form-input"
                    placeholder="e.g., Computer Science"
                  />
                  <ErrorMessage name="department" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="designation">Designation *</label>
                  <Field
                    type="text"
                    name="designation"
                    id="designation"
                    className="form-input"
                    placeholder="e.g., Professor, Assistant Professor"
                  />
                  <ErrorMessage name="designation" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="qualification">Qualification</label>
                  <Field
                    type="text"
                    name="qualification"
                    id="qualification"
                    className="form-input"
                    placeholder="e.g., Ph.D., M.Tech"
                  />
                  <ErrorMessage name="qualification" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="experience">Experience (years)</label>
                  <Field
                    type="number"
                    name="experience"
                    id="experience"
                    className="form-input"
                    min="0"
                  />
                  <ErrorMessage name="experience" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="joiningDate">Joining Date</label>
                  <Field
                    type="date"
                    name="joiningDate"
                    id="joiningDate"
                    className="form-input"
                  />
                  <ErrorMessage name="joiningDate" component="div" className="error-message" />
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

export default FacultyForm;
