import React, { useState, useEffect } from 'react';
import { getMyPerformance, getMyPerformanceSummary } from '../../api/endpoints/performanceApi';
import { toast } from 'react-toastify';
import './PerformanceView.css';

const PerformanceView = () => {
  const [performance, setPerformance] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedSubject, setSelectedSubject] = useState('all');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [performanceRes, summaryRes] = await Promise.all([
        getMyPerformance(),
        getMyPerformanceSummary()
      ]);
      setPerformance(performanceRes.data || []);
      setSummary(summaryRes.data || null);
    } catch (error) {
      toast.error('Failed to fetch performance data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getFilteredPerformance = () => {
    if (selectedSubject === 'all') return performance;
    return performance.filter(p => p.subjectId === selectedSubject);
  };

  const getGradeColor = (grade) => {
    const colors = {
      'A+': '#28a745',
      'A': '#20c997',
      'B+': '#17a2b8',
      'B': '#007bff',
      'C': '#ffc107',
      'D': '#fd7e14',
      'F': '#dc3545'
    };
    return colors[grade] || '#6c757d';
  };

  const getGPAColor = (gpa) => {
    if (gpa >= 9) return '#28a745';
    if (gpa >= 8) return '#20c997';
    if (gpa >= 7) return '#17a2b8';
    if (gpa >= 6) return '#ffc107';
    if (gpa >= 5) return '#fd7e14';
    return '#dc3545';
  };

  if (loading) {
    return <div className="loading">Loading performance data...</div>;
  }

  return (
    <div className="performance-view">
      <div className="page-header">
        <h1>My Performance</h1>
      </div>

      {summary && (
        <>
          <div className="performance-overview">
            <div className="gpa-card" style={{ borderColor: getGPAColor(summary.gpa || 0) }}>
              <div className="gpa-value" style={{ color: getGPAColor(summary.gpa || 0) }}>
                {(summary.gpa || 0).toFixed(2)}
              </div>
              <div className="gpa-label">Current GPA</div>
              <div className="gpa-scale">Out of 10.0</div>
            </div>

            <div className="performance-stats">
              <div className="stat-card">
                <div className="stat-icon">📚</div>
                <div className="stat-content">
                  <div className="stat-value">{summary.totalSubjects || 0}</div>
                  <div className="stat-label">Total Subjects</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon">📊</div>
                <div className="stat-content">
                  <div className="stat-value">{(summary.averagePercentage || 0).toFixed(1)}%</div>
                  <div className="stat-label">Average Percentage</div>
                </div>
              </div>
            </div>
          </div>

          {summary.gradeDistribution && Object.keys(summary.gradeDistribution).length > 0 && (
            <div className="grade-distribution">
              <h2>Grade Distribution</h2>
              <div className="grades-grid">
                {Object.entries(summary.gradeDistribution)
                  .sort((a, b) => {
                    const order = ['A+', 'A', 'B+', 'B', 'C', 'D', 'F'];
                    return order.indexOf(a[0]) - order.indexOf(b[0]);
                  })
                  .map(([grade, count]) => (
                    <div key={grade} className="grade-item">
                      <div
                        className="grade-badge"
                        style={{ backgroundColor: getGradeColor(grade) }}
                      >
                        {grade}
                      </div>
                      <div className="grade-count">{count} assessment{count !== 1 ? 's' : ''}</div>
                    </div>
                  ))}
              </div>
            </div>
          )}

          <div className="subject-wise-performance">
            <h2>Subject-wise Performance</h2>
            <div className="subjects-performance-grid">
              {summary.subjectWise.map(subject => (
                <div key={subject.subjectId} className="subject-performance-card">
                  <h3>{subject.subjectName}</h3>
                  <div className="subject-grade" style={{ color: getGradeColor(subject.bestGrade) }}>
                    {subject.bestGrade}
                  </div>
                  <div className="subject-percentage">
                    Average: {(subject.averagePercentage || 0).toFixed(1)}%
                  </div>
                  <div className="subject-assessments">
                    {subject.totalAssessments} assessment{subject.totalAssessments !== 1 ? 's' : ''}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      <div className="performance-records">
        <div className="records-header">
          <h2>Performance Records</h2>
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
          <table className="performance-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Subject</th>
                <th>Assessment Type</th>
                <th>Marks</th>
                <th>Percentage</th>
                <th>Grade</th>
              </tr>
            </thead>
            <tbody>
              {getFilteredPerformance().length === 0 ? (
                <tr>
                  <td colSpan="6" className="no-data">No performance records found</td>
                </tr>
              ) : (
                getFilteredPerformance()
                  .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
                  .map(record => (
                    <tr key={record.id}>
                      <td>{new Date(record.createdAt).toLocaleDateString()}</td>
                      <td>{record.subjectName}</td>
                      <td>{record.examType}</td>
                      <td>{record.marksObtained} / {record.totalMarks}</td>
                      <td>{(record.percentage || 0).toFixed(1)}%</td>
                      <td>
                        <span
                          className="grade-badge-small"
                          style={{ backgroundColor: getGradeColor(record.grade) }}
                        >
                          {record.grade}
                        </span>
                      </td>
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

export default PerformanceView;
