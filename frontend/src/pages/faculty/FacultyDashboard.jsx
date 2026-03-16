import { useState, useEffect, useRef } from 'react';
import { Routes, Route, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Navbar from '../../components/layout/Navbar';
import AttendanceMarking from './AttendanceMarking';
import PerformanceEntry from './PerformanceEntry';
import ReportsView from './ReportsView';
import { getMyAllocations } from '../../api/endpoints/allocationApi';
import { getFacultyStudents } from '../../api/endpoints/facultyApi';
import { toast } from 'react-toastify';
import './FacultyDashboard.css';

const FacultyDashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    allocations: 0,
    studentsTeaching: 0,
    attendanceMarked: 0,
  });
  const [allocations, setAllocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAllocations, setShowAllocations] = useState(false);
  const allocationsRef = useRef(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setError(null);
      const response = await getMyAllocations();
      const allocationData = response.data;
      setAllocations(allocationData);
      
      // Calculate stats from allocations
      // Fetch actual student count for each allocation
      let totalStudents = 0;
      for (const alloc of allocationData) {
        try {
          const studentsResponse = await getFacultyStudents(
            alloc.courseId,
            alloc.year,
            alloc.section
          );
          totalStudents += studentsResponse.data.filter(s => s.isActive && !s.isDeleted).length;
        } catch (error) {
          console.error('Failed to fetch students for allocation', error);
        }
      }
      
      setStats({
        allocations: allocationData.length,
        studentsTeaching: totalStudents,
        attendanceMarked: 0, // This would need a separate endpoint
      });
    } catch (error) {
      console.error('Failed to fetch dashboard data', error);
      setError('Failed to load dashboard data. Please try again.');
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const DashboardHome = () => {
    const handleMyClassesClick = () => {
      if (allocations.length === 0) {
        toast.info('You have no class allocations assigned');
      } else {
        setShowAllocations(true);
        setTimeout(() => {
          if (allocationsRef.current) {
            allocationsRef.current.scrollIntoView({ 
              behavior: 'smooth', 
              block: 'start' 
            });
            allocationsRef.current.classList.add('highlight');
            setTimeout(() => {
              if (allocationsRef.current) {
                allocationsRef.current.classList.remove('highlight');
              }
            }, 2000);
          }
        }, 100);
      }
    };

    if (loading) {
      return <div className="loading">Loading dashboard...</div>;
    }

    if (error) {
      return (
        <div className="error-container" style={{ textAlign: 'center', padding: '60px 20px' }}>
          <div style={{ fontSize: '48px', marginBottom: '20px' }}>⚠️</div>
          <h2 style={{ color: '#d32f2f', marginBottom: '10px' }}>Error Loading Dashboard</h2>
          <p style={{ color: '#666', marginBottom: '20px' }}>{error}</p>
          <button 
            onClick={fetchDashboardData}
            style={{
              background: '#4CAF50',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              padding: '12px 24px',
              fontSize: '16px',
              cursor: 'pointer'
            }}
          >
            Retry
          </button>
        </div>
      );
    }

    return (
      <>
        <div className="dashboard-header">
          <h1>Faculty Dashboard</h1>
          <p>Welcome, {user?.name || 'Faculty'}</p>
        </div>

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">📚</div>
            <div className="stat-content">
              <h3>Subjects Teaching</h3>
              <p className="stat-value">{stats.allocations}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">👥</div>
            <div className="stat-content">
              <h3>Total Students</h3>
              <p className="stat-value">{stats.studentsTeaching}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">📝</div>
            <div className="stat-content">
              <h3>Class Allocations</h3>
              <p className="stat-value">{stats.allocations}</p>
            </div>
          </div>
        </div>

        {showAllocations && allocations.length > 0 && (
          <div className="my-allocations" ref={allocationsRef}>
            <h2>My Class Allocations</h2>
            <div className="allocations-grid">
              {allocations.map(allocation => (
                <div key={allocation.id} className="allocation-card">
                  <h3>{allocation.subjectName}</h3>
                  <div className="allocation-details">
                    <p><strong>Course:</strong> {allocation.courseName}</p>
                    <p><strong>Year:</strong> {allocation.year} | <strong>Section:</strong> {allocation.section}</p>
                    <p><strong>Semester:</strong> {allocation.semester}</p>
                    <p><strong>Academic Year:</strong> {allocation.academicYear}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="action-grid">
          <Link to="/faculty/attendance" className="action-btn">
            <span className="action-icon">📝</span>
            <span>Mark Attendance</span>
          </Link>
          <Link to="/faculty/performance" className="action-btn">
            <span className="action-icon">📊</span>
            <span>Add Performance</span>
          </Link>
          <Link to="/faculty/reports" className="action-btn">
            <span className="action-icon">📋</span>
            <span>View Reports</span>
          </Link>
          <button className="action-btn" onClick={handleMyClassesClick}>
            <span className="action-icon">👥</span>
            <span>My Classes</span>
          </button>
        </div>
      </div>

      <div className="recent-activity">
        <h2>Recent Activity</h2>
        <div className="activity-list">
          <p className="no-activity">No recent activity</p>
        </div>
      </div>
    </>
  );
  };

  return (
    <div className="faculty-layout">
      <Navbar />
      <div className="faculty-container">
        <Routes>
          <Route index element={<DashboardHome />} />
          <Route path="dashboard" element={<DashboardHome />} />
          <Route path="attendance" element={<AttendanceMarking />} />
          <Route path="performance" element={<PerformanceEntry />} />
          <Route path="reports" element={<ReportsView />} />
        </Routes>
      </div>
    </div>
  );
};

export default FacultyDashboard;
