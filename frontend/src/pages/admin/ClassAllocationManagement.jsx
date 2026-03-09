import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { getAllAllocations, deleteAllocation } from '../../api/endpoints/allocationApi';
import ClassAllocationForm from './ClassAllocationForm';
import './ClassAllocationManagement.css';

const ClassAllocationManagement = () => {
  const [allocations, setAllocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedAllocation, setSelectedAllocation] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });

  useEffect(() => {
    fetchAllocations();
  }, [pagination.page, pagination.size]);

  const fetchAllocations = async () => {
    try {
      setLoading(true);
      const response = await getAllAllocations({
        page: pagination.page,
        size: pagination.size
      });
      setAllocations(response.data.content);
      setPagination(prev => ({
        ...prev,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages
      }));
    } catch (error) {
      toast.error('Failed to fetch allocations');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setSelectedAllocation(null);
    setShowModal(true);
  };

  const handleEdit = (allocation) => {
    setSelectedAllocation(allocation);
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this allocation?')) {
      try {
        await deleteAllocation(id);
        toast.success('Allocation deleted successfully');
        fetchAllocations();
      } catch (error) {
        toast.error('Failed to delete allocation');
        console.error(error);
      }
    }
  };

  const handleModalClose = () => {
    setShowModal(false);
    setSelectedAllocation(null);
  };

  const handleSuccess = () => {
    fetchAllocations();
    handleModalClose();
  };

  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="allocation-management">
      <div className="page-header">
        <h1>Class Allocation Management</h1>
        <button className="btn btn-primary" onClick={handleAdd}>
          Add Allocation
        </button>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Faculty</th>
              <th>Subject</th>
              <th>Course</th>
              <th>Year</th>
              <th>Section</th>
              <th>Semester</th>
              <th>Academic Year</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {allocations.length === 0 ? (
              <tr>
                <td colSpan="9" className="no-data">No allocations found</td>
              </tr>
            ) : (
              allocations.map(allocation => (
                <tr key={allocation.id}>
                  <td>{allocation.facultyName}</td>
                  <td>{allocation.subjectName}</td>
                  <td>{allocation.courseName}</td>
                  <td>{allocation.year}</td>
                  <td>{allocation.section}</td>
                  <td>{allocation.semester}</td>
                  <td>{allocation.academicYear}</td>
                  <td>
                    <span className={`status-badge ${allocation.isActive ? 'active' : 'inactive'}`}>
                      {allocation.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="actions">
                    <button
                      className="btn btn-sm btn-edit"
                      onClick={() => handleEdit(allocation)}
                      title="Edit"
                    >
                      ✏️
                    </button>
                    <button
                      className="btn btn-sm btn-delete"
                      onClick={() => handleDelete(allocation.id)}
                      title="Delete"
                    >
                      🗑️
                    </button>
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
            className="btn btn-sm"
          >
            Previous
          </button>
          <span className="page-info">
            Page {pagination.page + 1} of {pagination.totalPages}
          </span>
          <button
            onClick={() => handlePageChange(pagination.page + 1)}
            disabled={pagination.page >= pagination.totalPages - 1}
            className="btn btn-sm"
          >
            Next
          </button>
        </div>
      )}

      {showModal && (
        <ClassAllocationForm
          allocation={selectedAllocation}
          onClose={handleModalClose}
          onSuccess={handleSuccess}
        />
      )}
    </div>
  );
};

export default ClassAllocationManagement;
