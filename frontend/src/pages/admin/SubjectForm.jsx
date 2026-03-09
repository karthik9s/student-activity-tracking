import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import './SubjectForm.css';

const subjectSchema = Yup.object().shape({
  code: Yup.string().required('Subject code is required'),
  name: Yup.string().required('Subject name is required'),
  courseId: Yup.string().required('Course is required'),
  semester: Yup.number().min(1).max(8).required('Semester is required'),
  credits: Yup.number().min(1).max(10).required('Credits are required'),
  type: Yup.string(),
  description: Yup.string(),
});

const SubjectForm = ({ subject, onSubmit, onCancel }) => {
  const initialValues = subject || {
    code: '',
    name: '',
    description: '',
    courseId: '',
    semester: 1,
    credits: 3,
    type: 'THEORY',
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>{subject ? 'Edit Subject' : 'Add Subject'}</h2>
          <button className="close-btn" onClick={onCancel}>×</button>
        </div>

        <Formik
          initialValues={initialValues}
          validationSchema={subjectSchema}
          onSubmit={onSubmit}
        >
          {({ isSubmitting }) => (
            <Form className="subject-form">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="code">Subject Code *</label>
                  <Field
                    type="text"
                    name="code"
                    id="code"
                    className="form-input"
                  />
                  <ErrorMessage name="code" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="name">Subject Name *</label>
                  <Field
                    type="text"
                    name="name"
                    id="name"
                    className="form-input"
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
                />
                <ErrorMessage name="description" component="div" className="error-message" />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="courseId">Course ID *</label>
                  <Field
                    type="text"
                    name="courseId"
                    id="courseId"
                    className="form-input"
                  />
                  <ErrorMessage name="courseId" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="semester">Semester *</label>
                  <Field
                    type="number"
                    name="semester"
                    id="semester"
                    className="form-input"
                    min="1"
                    max="8"
                  />
                  <ErrorMessage name="semester" component="div" className="error-message" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="credits">Credits *</label>
                  <Field
                    type="number"
                    name="credits"
                    id="credits"
                    className="form-input"
                    min="1"
                    max="10"
                  />
                  <ErrorMessage name="credits" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="type">Type</label>
                  <Field as="select" name="type" id="type" className="form-input">
                    <option value="THEORY">Theory</option>
                    <option value="PRACTICAL">Practical</option>
                    <option value="LAB">Lab</option>
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

export default SubjectForm;
