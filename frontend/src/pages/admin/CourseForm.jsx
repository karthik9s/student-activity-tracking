import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import './CourseForm.css';

const courseSchema = Yup.object().shape({
  code: Yup.string().required('Course code is required'),
  name: Yup.string().required('Course name is required'),
  description: Yup.string(),
  duration: Yup.number().min(1).max(6).required('Duration is required'),
});

const CourseForm = ({ course, onSubmit, onCancel }) => {
  const initialValues = course || {
    code: '',
    name: '',
    description: '',
    duration: 4,
    semesters: [],
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>{course ? 'Edit Course' : 'Add Course'}</h2>
          <button className="close-btn" onClick={onCancel}>×</button>
        </div>

        <Formik
          initialValues={initialValues}
          validationSchema={courseSchema}
          onSubmit={onSubmit}
        >
          {({ isSubmitting }) => (
            <Form className="course-form">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="code">Course Code *</label>
                  <Field
                    type="text"
                    name="code"
                    id="code"
                    className="form-input"
                    placeholder="e.g., BTCSE"
                  />
                  <ErrorMessage name="code" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="name">Course Name *</label>
                  <Field
                    type="text"
                    name="name"
                    id="name"
                    className="form-input"
                    placeholder="e.g., B.Tech Computer Science"
                  />
                  <ErrorMessage name="name" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="description">Description</label>
                <Field
                  as="textarea"
                  name="description"
                  id="description"
                  className="form-input"
                  rows="3"
                  placeholder="Course description"
                />
                <ErrorMessage name="description" component="div" className="error-message" />
              </div>

              <div className="form-group">
                <label htmlFor="duration">Duration (Years) *</label>
                <Field as="select" name="duration" id="duration" className="form-input">
                  <option value={1}>1 Year</option>
                  <option value={2}>2 Years</option>
                  <option value={3}>3 Years</option>
                  <option value={4}>4 Years</option>
                  <option value={5}>5 Years</option>
                  <option value={6}>6 Years</option>
                </Field>
                <ErrorMessage name="duration" component="div" className="error-message" />
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

export default CourseForm;
