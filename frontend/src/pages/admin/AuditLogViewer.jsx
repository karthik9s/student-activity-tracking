import { useState, useEffect } from 'react';
import { auditLogApi } from '../../api/endpoints/auditLogApi';
import { toast } from 'react-toastify';
import './AuditLogViewer.css';

const AuditLogViewer = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalPages: 0,
    totalElements: 0
  });
  const [filters, setFilters] = useState({
    userId: '',
    entityType: '',
    action: '',
    startDate: '',
    endDate: ''
  });

  useEffect(() => {
    fetchAuditLogs();
  }, [pagination.page, pagination.size]);

  const fetchAuditLogs = async () => {
    try {
      setLoading(true);
      const params = {
        page: pagination.page,
        size: pagination.size,
        ...Object.fromEntries(
          Object.entries(filters).filter(([_, v]) => v !== '')
        )
      };
      
      const data = await auditLogApi.getAuditLogs(params);
      setLogs(data.content || []);
      setPagination(prev => ({
        ...prev,
        totalPages: data.totalPages || 0,
        totalElements: data.totalElements || 0
      }));
    } catch (error) {
      console.error('Error fetching audit logs:', error);
      toast.error('Failed to load audit logs');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (field, value) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  const handleApplyFilters = () => {
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchAuditLogs();
  };

  const handleClearFilters = () => {
    setFilters({
      userId: '',
      entityType: '',
      action: '',
      startDate: '',
      endDate: ''
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    setTimeout(fetchAuditLogs, 100);
  };

  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString();
  };

  const getActionBadgeClass = (action) => {
    switch (action) {
      case 'CREATE':
        return 'badge-success';
      case 'UPDATE':
        return 'badge-warning';
      case 'DELETE':
        return 'badge-danger';
      default:
        return 'badge-info';
    }
  };

  return (
    <div className="audit-log-viewer">
      <div className="audit-header">
        <h1>Audit Logs</h1>
        <p>View and filter system audit logs</p>
      </div>

      <div className="audit-filters">
        <div className="filter-row">
          <div className="filter-group">
            <label>User ID</label>
            <input
              type="text"
              value={filters.userId}
              onChange={(e) => handleFilterChange('userId', e.target.value)}
              placeholder="Filter by user ID"
            />
          </div>

          <div className="filter-group">
            <label>Entity Type</label>
            <select
              value={filters.entityType}
              onChange={(e) => handleFilterChange('entityType', e.target.value)}
            >
              <option value="">All Types</option>
              <option value="STUDENT">Student</option>
              <option value="FACULTY">Faculty</option>
              <option value="COURSE">Course</option>
              <option value="SUBJECT">Subject</option>
              <option value="ATTENDANCE">Attendance</option>
              <option value="PERFORMANCE">Performance</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Action</label>
            <select
              value={filters.action}
              onChange={(e) => handleFilterChange('action', e.target.value)}
            >
              <option value="">All Actions</option>
              <option value="CREATE">Create</option>
              <option value="UPDATE">Update</option>
              <option value="DELETE">Delete</option>
            </select>
          </div>
        </div>

        <div className="filter-row">
          <div className="filter-group">
            <label>Start Date</label>
            <input
              type="datetime-local"
              value={filters.startDate}
              onChange={(e) => handleFilterChange('startDate', e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label>End Date</label>
            <input
              type="datetime-local"
              value={filters.endDate}
              onChange={(e) => handleFilterChange('endDate', e.target.value)}
            />
          </div>

          <div className="filter-actions">
            <button onClick={handleApplyFilters} className="btn-primary">
              Apply Filters
            </button>
            <button onClick={handleClearFilters} className="btn-secondary">
              Clear
            </button>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="audit-loading">Loading audit logs...</div>
      ) : (
        <>
          <div className="audit-table-container">
            <table className="audit-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>User ID</th>
                  <th>Action</th>
                  <th>Entity Type</th>
                  <th>Entity ID</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                {logs.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="no-data">
                      No audit logs found
                    </td>
                  </tr>
                ) : (
                  logs.map((log) => (
                    <tr key={log.id}>
                      <td>{formatDate(log.timestamp)}</td>
                      <td className="user-id">{log.userId}</td>
                      <td>
                        <span className={`badge ${getActionBadgeClass(log.action)}`}>
                          {log.action}
                        </span>
                      </td>
                      <td>{log.entityType}</td>
                      <td className="entity-id">{log.entityId}</td>
                      <td>
                        {log.oldValue || log.newValue ? (
                          <button
                            className="details-btn"
                            onClick={() => {
                              const details = {
                                oldValue: log.oldValue,
                                newValue: log.newValue
                              };
                              alert(JSON.stringify(details, null, 2));
                            }}
                          >
                            View
                          </button>
                        ) : (
                          '-'
                        )}
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
                ({pagination.totalElements} total records)
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
    </div>
  );
};

export default AuditLogViewer;
