import { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Navbar from '../../components/layout/Navbar';
import AttendanceView from './AttendanceView';
import PerformanceView from './PerformanceView';
import SubjectsView from './SubjectsView';
import { getMyDashboardStats } from '../../api/endpoints/performanceApi';
import { getMyAttendance, getMyAttendanceSummary } from '../../api/endpoints/attendanceApi';
import './StudentDashboard.css';

const StudentDashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    overallAttendance: 0,
    currentGPA: 0,
    totalSubjects: 0,
    lowAttendanceCount: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const response = await getMyDashboardStats();
      setStats(response.data);
    } catch (error) {
      console.error('Failed to fetch stats', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadReport = async () => {
    try {
      const [attendanceRes, summaryRes] = await Promise.all([
        getMyAttendance(),
        getMyAttendanceSummary()
      ]);

      const records = attendanceRes.data;
      const summary = summaryRes.data;

      let csv = `Student Attendance Report\nOverall Attendance: ${summary.overallPercentage?.toFixed(1)}%\nTotal Classes: ${summary.totalClasses} | Attended: ${summary.attendedClasses}\n\n`;
      csv += 'Date,Subject,Status\n';
      records
        .sort((a, b) => new Date(b.date) - new Date(a.date))
        .forEach(r => {
          csv += `${new Date(r.date).toLocaleDateString()},${r.subjectName || ''},${r.status}\n`;
        });

      const blob = new Blob([csv], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `attendance-report-${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Failed to download report', error);
    }
  };

  const getAttendanceColor = (percentage) => {
    if (percentage >= 75) return '#28a745';
    if (percentage >= 70) return '#ffc107';
    return '#dc3545';
  };

  const getGPAColor = (gpa) => {
    if (gpa >= 9) return '#28a745';
    if (gpa >= 8) return '#20c997';
    if (gpa >= 7) return '#17a2b8';
    if (gpa >= 6) return '#ffc107';
    if (gpa >= 5) return '#fd7e14';
    return '#dc3545';
  };

  const DashboardHome = () => (
    <>
      <div className="dashboard-header">
        <h1>Student Dashboard</h1>
        <p>Welcome, {user?.name || 'Student'}</p>
      </div>

      {stats.lowAttendanceCount > 0 && (
        <div className="alert alert-warning">
          ⚠️ Warning: You have low attendance in {stats.lowAttendanceCount} subject(s). 
          Please improve your attendance to meet the 75% requirement.
        </div>
      )}

      <div className="stats-grid">
        <div className="stat-card" style={{ borderLeft: `4px solid ${getAttendanceColor(stats.overallAttendance)}` }}>
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <h3>Overall Attendance</h3>
            <p className="stat-value" style={{ color: getAttendanceColor(stats.overallAttendance) }}>
              {stats.overallAttendance.toFixed(1)}%
            </p>
          </div>
        </div>

        <div className="stat-card" style={{ borderLeft: `4px solid ${getGPAColor(stats.currentGPA)}` }}>
          <div className="stat-icon">🎓</div>
          <div className="stat-content">
            <h3>Current GPA</h3>
            <p className="stat-value" style={{ color: getGPAColor(stats.currentGPA) }}>
              {stats.currentGPA.toFixed(2)}
            </p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">📚</div>
          <div className="stat-content">
            <h3>Enrolled Subjects</h3>
            <p className="stat-value">{stats.totalSubjects}</p>
          </div>
        </div>
      </div>

      <div className="quick-links">
        <h2>Quick Links</h2>
        <div className="link-grid">
          <Link to="/student/attendance" className="link-btn">
            <span className="link-icon">📅</span>
            <span>View Attendance</span>
          </Link>
          <Link to="/student/performance" className="link-btn">
            <span className="link-icon">📈</span>
            <span>View Performance</span>
          </Link>
          <Link to="/student/subjects" className="link-btn">
            <span className="link-icon">📋</span>
            <span>View Subjects</span>
          </Link>
          <button className="link-btn" onClick={handleDownloadReport}>
            <span className="link-icon">📄</span>
            <span>Download Reports</span>
          </button>
        </div>
      </div>

      <div className="recent-updates">
        <h2>Recent Updates</h2>
        <div className="updates-list">
          <p className="no-updates">No recent updates</p>
        </div>
      </div>
    </>
  );

  return (
    <div className="student-layout">
      <Navbar />
      <div className="student-container">
        <Routes>
          <Route index element={loading ? <div className="loading">Loading...</div> : <DashboardHome />} />
          <Route path="dashboard" element={loading ? <div className="loading">Loading...</div> : <DashboardHome />} />
          <Route path="attendance" element={<AttendanceView />} />
          <Route path="performance" element={<PerformanceView />} />
          <Route path="subjects" element={<SubjectsView />} />
        </Routes>
      </div>
    </div>
  );
};

export default StudentDashboard;
