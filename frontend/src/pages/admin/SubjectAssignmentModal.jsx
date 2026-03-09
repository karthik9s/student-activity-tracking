import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { getAllocationsByFaculty, createAllocation, deleteAllocation } from '../../api/endpoints/allocationApi';
import { getAllSubjects } from '../../api/endpoints/subjectApi';
import { getAllCourses } from '../../api/endpoints/courseApi';
import './SubjectAssignmentModal.css';

const SubjectAssignmentModal = ({ faculty, onClose }) => {
  const [allocations, setAllocations] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [formData, setFormData] = useState({
    subjectId: '',
    courseId: '',
    year: '',
    section: '',
    semester: '',
    academicYear: new Date().getFullYear(),
  });

  useEffect(() => {
    fetchAllocations();
    fetchSubjects();
    fetchCourses();
  }, [faculty.id]);

  const fetchAllocations = async () => {
    setLoading(true);
    try {
      const response = await getAllocationsByFaculty(faculty.id);
      setAllocations(response.data);
    } catch (error) {
      toast.error('Failed to fetch allocations');
    } finally {
      setLoading(false);
    }
  };

  const fetchSubjects = async () => {
    try {
      const response = await getAllSubjects({ page: 0, size: 100 });
      setSubjects(response.data.content || []);
    } catch (error) {
      console.error('Failed to fetch subjects');
    }
  };

  const fetchCourses = async () => {
    try {
      const response = await getAllCourses({ page: 0, size: 100 });
      setCourses(response.data.content || []);
    } catch (error) {
      console.error('Failed to fetch courses');
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleAddAllocation = async (e) => {
    e.preventDefault();
    
    if (!formData.subjectId || !formData.courseId || !formData.year || !formData.section || !formData.semester) {
      toast.error('Please fill all required fields');
      return;
    }

    try {
      const allocationData = {
        facultyId: faculty.id,
        subjectId: formData.subjectId,
        courseId: formData.courseId,
        year: parseInt(formData.year),
        section: formData.section,
        semester: parseInt(formData.semester),
        academicYear: parseInt(formData.academicYear),
      };

      await createAllocation(allocationData);
      toast.success('Subject assigned successfully');
      setShowAddForm(false);
      setFormData({
        subjectId: '',
        courseId: '',
        year: '',
        section: '',
        semester: '',
        academicYear: new Date().getFullYear(),
      });
      fetchAllocations();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to assign subject');
    }
  };

  const handleDeleteAllocation = async (allocationId) => {
    if (!window.confirm('Are you sure you want to remove this assignment?')) {
      return;
    }

    try {
      await deleteAllocation(allocationId);
      toast.success('Assignment removed successfully');
      fetchAllocations();
    } catch (error) {
      toast.error('Failed to remove assignment');
    }
  };

  const getSubjectName = (subjectId) => {
    const subject = subjects.find(s => s.id === subjectId);
    return subject ? `${subject.code} - ${subject.name}` : 'Unknown';
  };

  const getCourseName = (courseId) => {
    const course = courses.find(c => c.id === courseId);
    return course ? course.name : 'Unknown';
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content subject-assignment-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Subject Assignments - {faculty.firstName} {faculty.lastName}</h2>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>

        <div className="modal-body">
          <div className="assignment-actions">
            <button 
              className="btn-primary" 
              onClick={() => setShowAddForm(!showAddForm)}
            >
              {showAddForm ? 'Cancel' : 'Add New Assignment'}
            </button>
          </div>

          {showAddForm && (
            <form className="assignment-form" onSubmit={handleAddAllocation}>
              <div className="form-row">
                <div className="form-group">
                  <label>Subject *</label>
                  <select
                    name="subjectId"
                    value={formData.subjectId}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select Subject</option>
                    {subjects.map(subject => (
                      <option key={subject.id} value={subject.id}>
                        {subject.code} - {subject.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Course *</label>
                  <select
                    name="courseId"
                    value={formData.courseId}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select Course</option>
                    {courses.map(course => (
                      <option key={course.id} value={course.id}>
                        {course.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Year *</label>
                  <select
                    name="year"
                    value={formData.year}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select Year</option>
                    <option value="1">1st Year</option>
                    <option value="2">2nd Year</option>
                    <option value="3">3rd Year</option>
                    <option value="4">4th Year</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Section *</label>
                  <input
                    type="text"
                    name="section"
                    value={formData.section}
                    onChange={handleInputChange}
                    placeholder="e.g., A, B, C"
                    maxLength="2"
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Semester *</label>
                  <select
                    name="semester"
                    value={formData.semester}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select Semester</option>
                    <option value="1">Semester 1</option>
                    <option value="2">Semester 2</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Academic Year *</label>
                  <input
                    type="number"
                    name="academicYear"
                    value={formData.academicYear}
                    onChange={handleInputChange}
                    min="2020"
                    max="2100"
                    required
                  />
                </div>
              </div>

              <button type="submit" className="btn-primary">
                Assign Subject
              </button>
            </form>
          )}

          <div className="allocations-list">
            <h3>Current Assignments</h3>
            {loading ? (
              <div className="loading">Loading...</div>
            ) : allocations.length === 0 ? (
              <p className="no-data">No assignments found</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Subject</th>
                    <th>Course</th>
                    <th>Year</th>
                    <th>Section</th>
                    <th>Semester</th>
                    <th>Academic Year</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {allocations.map(allocation => (
                    <tr key={allocation.id}>
                      <td>{getSubjectName(allocation.subjectId)}</td>
                      <td>{getCourseName(allocation.courseId)}</td>
                      <td>{allocation.year}</td>
                      <td>{allocation.section}</td>
                      <td>{allocation.semester}</td>
                      <td>{allocation.academicYear}</td>
                      <td>
                        <button
                          className="btn-delete"
                          onClick={() => handleDeleteAllocation(allocation.id)}
                        >
                          Remove
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SubjectAssignmentModal;
