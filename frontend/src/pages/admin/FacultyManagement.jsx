import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { getAllFaculty, createFaculty, updateFaculty, deleteFaculty } from '../../api/endpoints/facultyApi';
import FacultyForm from './FacultyForm';
import SubjectAssignmentModal from './SubjectAssignmentModal';
import './FacultyManagement.css';

const FacultyManagement = () => {
  const [faculty, setFaculty] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingFaculty, setEditingFaculty] = useState(null);
  const [showAssignmentModal, setShowAssignmentModal] = useState(false);
  const [selectedFaculty, setSelectedFaculty] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  });
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchFaculty();
  }, [pagination.page, pagination.size, searchTerm]);

  const fetchFaculty = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        ...(searchTerm && { search: searchTerm }),
      };

      const response = await getAllFaculty(params);
      setFaculty(response.data.content);
      setPagination(prev => ({
        ...prev,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages,
      }));
    } catch (error) {
      toast.error('Failed to fetch faculty');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingFaculty(null);
    setShowForm(true);
  };

  const handleEdit = (facultyMember) => {
    setEditingFaculty(facultyMember);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this faculty member?')) {
      return;
    }

    try {
      await deleteFaculty(id);
      toast.success('Faculty deleted successfully');
      fetchFaculty();
    } catch (error) {
      toast.error('Failed to delete faculty');
    }
  };

  const handleFormSubmit = async (data) => {
    try {
      if (editingFaculty) {
        await updateFaculty(editingFaculty.id, data);
        toast.success('Faculty updated successfully');
      } else {
        await createFaculty(data);
        toast.success('Faculty created successfully');
      }
      setShowForm(false);
      fetchFaculty();
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

  const handleAssignSubjects = (facultyMember) => {
    setSelectedFaculty(facultyMember);
    setShowAssignmentModal(true);
  };

  return (
    <div className="faculty-management">
      <div className="page-header">
        <h1>Faculty Management</h1>
        <button className="btn-primary" onClick={handleCreate}>
          Add Faculty
        </button>
      </div>

      <div className="filters-section">
        <input
          type="text"
          placeholder="Search faculty..."
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
                  <th>Employee ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Department</th>
                  <th>Designation</th>
                  <th>Phone</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {faculty.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="no-data">No faculty found</td>
                  </tr>
                ) : (
                  faculty.map((member) => (
                    <tr key={member.id}>
                      <td>{member.employeeId}</td>
                      <td>{`${member.firstName} ${member.lastName}`}</td>
                      <td>{member.email}</td>
                      <td>{member.department}</td>
                      <td>{member.designation}</td>
                      <td>{member.phone || 'N/A'}</td>
                      <td>
                        <span className={`status-badge ${member.isActive ? 'active' : 'inactive'}`}>
                          {member.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn-edit"
                            onClick={() => handleEdit(member)}
                          >
                            Edit
                          </button>
                          <button
                            className="btn-secondary"
                            onClick={() => handleAssignSubjects(member)}
                          >
                            Assign Subjects
                          </button>
                          <button
                            className="btn-delete"
                            onClick={() => handleDelete(member.id)}
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
        <FacultyForm
          faculty={editingFaculty}
          onSubmit={handleFormSubmit}
          onCancel={() => setShowForm(false)}
        />
      )}

      {showAssignmentModal && selectedFaculty && (
        <SubjectAssignmentModal
          faculty={selectedFaculty}
          onClose={() => {
            setShowAssignmentModal(false);
            setSelectedFaculty(null);
          }}
        />
      )}
    </div>
  );
};

export default FacultyManagement;
