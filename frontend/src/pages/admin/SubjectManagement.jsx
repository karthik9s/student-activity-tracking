import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { getAllSubjects, createSubject, updateSubject, deleteSubject } from '../../api/endpoints/subjectApi';
import SubjectForm from './SubjectForm';
import './SubjectManagement.css';

const SubjectManagement = () => {
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingSubject, setEditingSubject] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  });
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchSubjects();
  }, [pagination.page, pagination.size, searchTerm]);

  const fetchSubjects = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        ...(searchTerm && { search: searchTerm }),
      };

      const response = await getAllSubjects(params);
      setSubjects(response.data.content);
      setPagination(prev => ({
        ...prev,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages,
      }));
    } catch (error) {
      toast.error('Failed to fetch subjects');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingSubject(null);
    setShowForm(true);
  };

  const handleEdit = (subject) => {
    setEditingSubject(subject);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this subject?')) {
      return;
    }

    try {
      await deleteSubject(id);
      toast.success('Subject deleted successfully');
      fetchSubjects();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete subject');
    }
  };

  const handleFormSubmit = async (data) => {
    try {
      if (editingSubject) {
        await updateSubject(editingSubject.id, data);
        toast.success('Subject updated successfully');
      } else {
        await createSubject(data);
        toast.success('Subject created successfully');
      }
      setShowForm(false);
      fetchSubjects();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  return (
    <div className="subject-management">
      <div className="page-header">
        <h1>Subject Management</h1>
        <button className="btn-primary" onClick={handleCreate}>
          Add Subject
        </button>
      </div>

      <div className="filters-section">
        <input
          type="text"
          placeholder="Search subjects..."
          value={searchTerm}
          onChange={handleSearch}
          className="search-input"
        />
      </div>

      {loading ? (
        <div className="loading">Loading...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Name</th>
                  <th>Course</th>
                  <th>Semester</th>
                  <th>Credits</th>
                  <th>Type</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {subjects.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="no-data">No subjects found</td>
                  </tr>
                ) : (
                  subjects.map((subject) => (
                    <tr key={subject.id}>
                      <td>{subject.code}</td>
                      <td>{subject.name}</td>
                      <td>{subject.courseName || subject.courseId}</td>
                      <td>{subject.semester}</td>
                      <td>{subject.credits}</td>
                      <td>{subject.type || 'N/A'}</td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn-edit"
                            onClick={() => handleEdit(subject)}
                          >
                            Edit
                          </button>
                          <button
                            className="btn-delete"
                            onClick={() => handleDelete(subject.id)}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {pagination.totalPages > 1 && (
            <div className="pagination">
              <button
                onClick={() => handlePageChange(pagination.page - 1)}
                disabled={pagination.page === 0}
                className="pagination-btn"
              >
                Previous
              </button>
              <span className="pagination-info">
                Page {pagination.page + 1} of {pagination.totalPages}
              </span>
              <button
                onClick={() => handlePageChange(pagination.page + 1)}
                disabled={pagination.page >= pagination.totalPages - 1}
                className="pagination-btn"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}

      {showForm && (
        <SubjectForm
          subject={editingSubject}
          onSubmit={handleFormSubmit}
          onCancel={() => setShowForm(false)}
        />
      )}
    </div>
  );
};

export default SubjectManagement;
