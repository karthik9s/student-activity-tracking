import { useState, useEffect } from 'react';
import { adminApi } from '../../api/endpoints/adminApi';
import { BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { toast } from 'react-toastify';
import './DashboardHome.css';

const DashboardHome = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  const fetchDashboardStats = async () => {
    try {
      setLoading(true);
      const data = await adminApi.getDashboardStats();
      setStats(data);
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
      toast.error('Failed to load dashboard statistics');
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    fetchDashboardStats();
    toast.success('Dashboard refreshed');
  };

  if (loading) {
    return <div className="dashboard-loading">Loading dashboard...</div>;
  }

  if (!stats) {
    return <div className="dashboard-error">Failed to load dashboard data</div>;
  }

  // Prepare data for enrollment trends chart
  const enrollmentData = [
    { name: 'Students', count: stats.totalStudents || 0 },
    { name: 'Faculty', count: stats.totalFaculty || 0 },
    { name: 'Courses', count: stats.totalCourses || 0 }
  ];

  // Prepare data for attendance overview pie chart
  const systemStats = stats.systemStats || {};
  const attendanceData = [
    { name: 'Present', value: Math.round((systemStats.averageAttendancePercentage || 0)) },
    { name: 'Absent', value: Math.round(100 - (systemStats.averageAttendancePercentage || 0)) }
  ];

  const COLORS = ['#4CAF50', '#f44336'];

  return (
    <div className="dashboard-home">
      <div className="dashboard-header">
        <h1>Admin Dashboard</h1>
        <button onClick={handleRefresh} className="refresh-btn">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clipRule="evenodd" />
          </svg>
          Refresh
        </button>
      </div>

      <div className="stats-cards">
        <div className="stat-card">
          <div className="stat-icon students">
            <svg width="24" height="24" viewBox="0 0 20 20" fill="currentColor">
              <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z" />
            </svg>
          </div>
          <div className="stat-content">
            <h3>Total Students</h3>
            <p className="stat-value">{stats.totalStudents || 0}</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon faculty">
            <svg width="24" height="24" viewBox="0 0 20 20" fill="currentColor">
              <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z" />
            </svg>
          </div>
          <div className="stat-content">
            <h3>Total Faculty</h3>
            <p className="stat-value">{stats.totalFaculty || 0}</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon courses">
            <svg width="24" height="24" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10.394 2.08a1 1 0 00-.788 0l-7 3a1 1 0 000 1.84L5.25 8.051a.999.999 0 01.356-.257l4-1.714a1 1 0 11.788 1.838L7.667 9.088l1.94.831a1 1 0 00.787 0l7-3a1 1 0 000-1.838l-7-3z" />
            </svg>
          </div>
          <div className="stat-content">
            <h3>Total Courses</h3>
            <p className="stat-value">{stats.totalCourses || 0}</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon attendance">
            <svg width="24" height="24" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="stat-content">
            <h3>Avg Attendance</h3>
            <p className="stat-value">{(systemStats.averageAttendancePercentage || 0).toFixed(1)}%</p>
          </div>
        </div>
      </div>

      <div className="charts-container">
        <div className="chart-card">
          <h2>Enrollment Overview</h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={enrollmentData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="count" fill="#2196F3" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h2>Attendance Overview</h2>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={attendanceData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, value }) => `${name}: ${value}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {attendanceData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="system-stats">
        <h2>System Statistics</h2>
        <div className="stats-grid">
          <div className="stat-item">
            <span className="stat-label">Total Attendance Records:</span>
            <span className="stat-number">{systemStats.totalAttendanceRecords || 0}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Total Performance Records:</span>
            <span className="stat-number">{systemStats.totalPerformanceRecords || 0}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardHome;
