import React, { useState } from 'react';
import './AdvancedFilterModal.css';

const AdvancedFilterModal = ({ isOpen, onClose, onApply, filterType }) => {
  const [filters, setFilters] = useState({
    searchTerm: '',
    courseId: '',
    year: '',
    section: '',
    department: '',
    isActive: ''
  });

  const handleChange = (e) => {
    setFilters({ ...filters, [e.target.name]: e.target.value });
  };

  const handleApply = () => {
    onApply(filters);
    onClose();
  };

  const handleReset = () => {
    setFilters({
      searchTerm: '',
      courseId: '',
      year: '',
      section: '',
      department: '',
      isActive: ''
    });
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Advanced Filters</h3>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>
        
        <div className="modal-body">
          <div className="filter-group">
            <label>Search Term</label>
            <input
              type="text"
              name="searchTerm"
              value={filters.searchTerm}
              onChange={handleChange}
              placeholder="Search by name, email, ID..."
            />
          </div>

          {filterType === 'student' && (
            <>
              <div className="filter-group">
                <label>Year</label>
                <select name="year" value={filters.year} onChange={handleChange}>
                  <option value="">All Years</option>
                  <option value="1">1st Year</option>
                  <option value="2">2nd Year</option>
                  <option value="3">3rd Year</option>
                  <option value="4">4th Year</option>
                </select>
              </div>

              <div className="filter-group">
                <label>Section</label>
                <input
                  type="text"
                  name="section"
                  value={filters.section}
                  onChange={handleChange}
                  placeholder="e.g., A, B, C"
                />
              </div>
            </>
          )}

          {filterType === 'faculty' && (
            <div className="filter-group">
              <label>Department</label>
              <input
                type="text"
                name="department"
                value={filters.department}
                onChange={handleChange}
                placeholder="Department name"
              />
            </div>
          )}

          <div className="filter-group">
            <label>Status</label>
            <select name="isActive" value={filters.isActive} onChange={handleChange}>
              <option value="">All</option>
              <option value="true">Active</option>
              <option value="false">Inactive</option>
            </select>
          </div>
        </div>

        <div className="modal-footer">
          <button className="btn-secondary" onClick={handleReset}>Reset</button>
          <button className="btn-primary" onClick={handleApply}>Apply Filters</button>
        </div>
      </div>
    </div>
  );
};

export default AdvancedFilterModal;
