import React, { useState, useEffect } from 'react';
import { getMyAttendance, getMyAttendanceSummary } from '../../api/endpoints/attendanceApi';
import { toast } from 'react-toastify';
import './AttendanceView.css';

const AttendanceView = () => {
  const [attendance, setAttendance] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedSubject, setSelectedSubject] = useState('all');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [attendanceRes, summaryRes] = await Promise.all([
        getMyAttendance(),
        getMyAttendanceSummary()
      ]);
      setAttendance(attendanceRes.data);
      setSummary(summaryRes.data);
    } catch (error) {
      toast.error('Failed to fetch attendance data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getFilteredAttendance = () => {
    if (selectedSubject === 'all') return attendance;
    return attendance.filter(a => a.subjectId === selectedSubject);
  };

  const getStatusColor = (percentage) => {
    if (percentage >= 75) return 'good';
    if (percentage >= 70) return 'warning';
    return 'critical';
  };

  if (loading) {
    return <div className="loading">Loading attendance data...</div>;
  }

  return (
    <div className="attendance-view">
      <div className="page-header">
        <h1>My Attendance</h1>
      </div>

      {summary && (
        <>
          <div className="overall-attendance">
            <div className={`attendance-circle ${getStatusColor(summary.overallPercentage)}`}>
              <div className="percentage">{summary.overallPercentage.toFixed(1)}%</div>
              <div className="label">Overall Attendance</div>
            </div>
            <div className="attendance-stats">
              <div className="stat-item">
                <span className="stat-label">Total Classes:</span>
                <span className="stat-value">{summary.totalClasses}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Attended:</span>
                <span className="stat-value">{summary.attendedClasses}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Missed:</span>
                <span className="stat-value">{summary.totalClasses - summary.attendedClasses}</span>
              </div>
            </div>
          </div>

          {summary.lowAttendanceCount > 0 && (
            <div className="alert alert-warning">
              ⚠️ Warning: You have low attendance in {summary.lowAttendanceCount} subject(s). 
              Minimum 75% attendance is required.
            </div>
          )}

          <div className="subject-wise-attendance">
            <h2>Subject-wise Attendance</h2>
            <div className="subjects-grid">
              {summary.subjectWise.map(subject => (
                <div key={subject.subjectId} className="subject-card">
                  <h3>{subject.subjectName}</h3>
                  <div className={`progress-circle ${getStatusColor(subject.percentage)}`}>
                    <svg viewBox="0 0 36 36" className="circular-chart">
                      <path
                        className="circle-bg"
                        d="M18 2.0845
                          a 15.9155 15.9155 0 0 1 0 31.831
                          a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                      <path
                        className="circle"
                        strokeDasharray={`${subject.percentage}, 100`}
                        d="M18 2.0845
                          a 15.9155 15.9155 0 0 1 0 31.831
                          a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                      <text x="18" y="20.35" className="percentage-text">
                        {subject.percentage.toFixed(0)}%
                      </text>
                    </svg>
                  </div>
                  <div className="subject-stats">
                    <span>{subject.attendedClasses} / {subject.totalClasses} classes</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      <div className="attendance-records">
        <div className="records-header">
          <h2>Attendance Records</h2>
          <select
            value={selectedSubject}
            onChange={(e) => setSelectedSubject(e.target.value)}
            className="subject-filter"
          >
            <option value="all">All Subjects</option>
            {summary?.subjectWise.map(subject => (
              <option key={subject.subjectId} value={subject.subjectId}>
                {subject.subjectName}
              </option>
            ))}
          </select>
        </div>

        <div className="table-container">
          <table className="attendance-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Subject</th>
                <th>Status</th>
                <th>Remarks</th>
              </tr>
            </thead>
            <tbody>
              {getFilteredAttendance().length === 0 ? (
                <tr>
                  <td colSpan="4" className="no-data">No attendance records found</td>
                </tr>
              ) : (
                getFilteredAttendance()
                  .sort((a, b) => new Date(b.date) - new Date(a.date))
                  .map(record => (
                    <tr key={record.id}>
                      <td>{new Date(record.date).toLocaleDateString()}</td>
                      <td>{record.subjectName}</td>
                      <td>
                        <span className={`status-badge ${record.status.toLowerCase()}`}>
                          {record.status === 'PRESENT' ? '✓ Present' : '✗ Absent'}
                        </span>
                      </td>
                      <td>{record.remarks || '-'}</td>
                    </tr>
                  ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AttendanceView;
