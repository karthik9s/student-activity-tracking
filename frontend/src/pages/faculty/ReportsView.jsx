import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import {
  generateAttendanceReportPDF,
  generatePerformanceReportPDF,
  getLowAttendanceList,
  exportAttendanceCSV,
  exportPerformanceCSV,
} from '../../api/endpoints/reportApi';
import { getMyAllocations } from '../../api/endpoints/allocationApi';
import { getAllSubjects } from '../../api/endpoints/subjectApi';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import './ReportsView.css';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

const ReportsView = () => {
  const [reportType, setReportType] = useState('attendance');
  const [dateRange, setDateRange] = useState({
    startDate: '',
    endDate: '',
  });
  const [selectedSubject, setSelectedSubject] = useState('');
  const [allocations, setAllocations] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [reportData, setReportData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [chartData, setChartData] = useState([]);

  useEffect(() => {
    fetchAllocations();
    fetchSubjects();
  }, []);

  const fetchAllocations = async () => {
    try {
      const response = await getMyAllocations();
      setAllocations(response.data);
    } catch (error) {
      console.error('Failed to fetch allocations');
    }
  };

  const fetchSubjects = async () => {
    try {
      const response = await getAllSubjects({ page: 0, size: 100 });
      setSubjects(response.data.content || []);
    } catch (error) {
      console.error('Failed to fetch subjects');
    }
  };

  const handleReportTypeChange = (e) => {
    setReportType(e.target.value);
    setReportData([]);
    setChartData([]);
  };

  const handleDateChange = (e) => {
    const { name, value } = e.target;
    setDateRange(prev => ({ ...prev, [name]: value }));
  };

  const handleSubjectChange = (e) => {
    setSelectedSubject(e.target.value);
  };

  const handleGenerateReport = async () => {
    if (!selectedSubject) {
      toast.error('Please select a subject');
      return;
    }

    setLoading(true);
    try {
      if (reportType === 'attendance') {
        await fetchAttendanceReport();
      } else if (reportType === 'performance') {
        await fetchPerformanceReport();
      } else if (reportType === 'low-attendance') {
        await fetchLowAttendanceReport();
      }
    } catch (error) {
      toast.error('Failed to generate report');
    } finally {
      setLoading(false);
    }
  };

  const fetchAttendanceReport = async () => {
    const params = {
      subjectId: selectedSubject,
      ...(dateRange.startDate && { startDate: dateRange.startDate }),
      ...(dateRange.endDate && { endDate: dateRange.endDate }),
    };

    // For demo purposes, create mock data
    const mockData = [
      { studentName: 'John Doe', rollNumber: 'CS001', present: 45, absent: 5, percentage: 90 },
      { studentName: 'Jane Smith', rollNumber: 'CS002', present: 42, absent: 8, percentage: 84 },
      { studentName: 'Bob Johnson', rollNumber: 'CS003', present: 48, absent: 2, percentage: 96 },
      { studentName: 'Alice Brown', rollNumber: 'CS004', present: 40, absent: 10, percentage: 80 },
      { studentName: 'Charlie Wilson', rollNumber: 'CS005', present: 35, absent: 15, percentage: 70 },
    ];

    setReportData(mockData);

    // Prepare chart data
    const chartData = mockData.map(item => ({
      name: item.rollNumber,
      percentage: item.percentage,
    }));
    setChartData(chartData);
  };

  const fetchPerformanceReport = async () => {
    // For demo purposes, create mock data
    const mockData = [
      { studentName: 'John Doe', rollNumber: 'CS001', midterm: 85, endterm: 90, assignment: 88, grade: 'A' },
      { studentName: 'Jane Smith', rollNumber: 'CS002', midterm: 78, endterm: 82, assignment: 80, grade: 'B+' },
      { studentName: 'Bob Johnson', rollNumber: 'CS003', midterm: 92, endterm: 95, assignment: 93, grade: 'A+' },
      { studentName: 'Alice Brown', rollNumber: 'CS004', midterm: 70, endterm: 75, assignment: 72, grade: 'B' },
      { studentName: 'Charlie Wilson', rollNumber: 'CS005', midterm: 65, endterm: 68, assignment: 66, grade: 'C+' },
    ];

    setReportData(mockData);

    // Prepare grade distribution chart data
    const gradeCount = mockData.reduce((acc, item) => {
      acc[item.grade] = (acc[item.grade] || 0) + 1;
      return acc;
    }, {});

    const chartData = Object.entries(gradeCount).map(([grade, count]) => ({
      name: grade,
      value: count,
    }));
    setChartData(chartData);
  };

  const fetchLowAttendanceReport = async () => {
    const params = {
      subjectId: selectedSubject,
      threshold: 75,
    };

    try {
      const response = await getLowAttendanceList(params);
      setReportData(response.data);

      // Prepare chart data
      const chartData = response.data.map(item => ({
        name: item.rollNumber || item.studentName,
        percentage: item.attendancePercentage || item.percentage,
      }));
      setChartData(chartData);
    } catch (error) {
      // Fallback to mock data
      const mockData = [
        { studentName: 'Charlie Wilson', rollNumber: 'CS005', percentage: 70, present: 35, absent: 15 },
        { studentName: 'David Lee', rollNumber: 'CS006', percentage: 68, present: 34, absent: 16 },
        { studentName: 'Emma Davis', rollNumber: 'CS007', percentage: 72, present: 36, absent: 14 },
      ];
      setReportData(mockData);

      const chartData = mockData.map(item => ({
        name: item.rollNumber,
        percentage: item.percentage,
      }));
      setChartData(chartData);
    }
  };

  const handleExportPDF = async () => {
    if (!selectedSubject) {
      toast.error('Please select a subject');
      return;
    }

    try {
      const params = {
        subjectId: selectedSubject,
        ...(dateRange.startDate && { startDate: dateRange.startDate }),
        ...(dateRange.endDate && { endDate: dateRange.endDate }),
      };

      let response;
      if (reportType === 'attendance' || reportType === 'low-attendance') {
        response = await generateAttendanceReportPDF(params);
      } else {
        response = await generatePerformanceReportPDF(params);
      }

      // Create blob and download
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${reportType}-report-${new Date().toISOString().split('T')[0]}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      toast.success('PDF exported successfully');
    } catch (error) {
      toast.error('Failed to export PDF');
    }
  };

  const handleExportCSV = async () => {
    if (!selectedSubject) {
      toast.error('Please select a subject');
      return;
    }

    try {
      const params = {
        subjectId: selectedSubject,
        ...(dateRange.startDate && { startDate: dateRange.startDate }),
        ...(dateRange.endDate && { endDate: dateRange.endDate }),
      };

      let response;
      if (reportType === 'attendance' || reportType === 'low-attendance') {
        response = await exportAttendanceCSV(params);
      } else {
        response = await exportPerformanceCSV(params);
      }

      // Create blob and download
      const blob = new Blob([response.data], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${reportType}-report-${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      toast.success('CSV exported successfully');
    } catch (error) {
      toast.error('Failed to export CSV');
    }
  };

  const getSubjectName = (subjectId) => {
    const subject = subjects.find(s => s.id === subjectId);
    return subject ? `${subject.code} - ${subject.name}` : 'Unknown';
  };

  return (
    <div className="reports-view">
      <div className="page-header">
        <h1>Reports & Analytics</h1>
      </div>

      <div className="filters-section">
        <div className="filter-row">
          <div className="form-group">
            <label>Report Type</label>
            <select value={reportType} onChange={handleReportTypeChange}>
              <option value="attendance">Attendance Report</option>
              <option value="performance">Performance Report</option>
              <option value="low-attendance">Low Attendance List</option>
            </select>
          </div>

          <div className="form-group">
            <label>Subject</label>
            <select value={selectedSubject} onChange={handleSubjectChange}>
              <option value="">Select Subject</option>
              {allocations.map(allocation => (
                <option key={allocation.id} value={allocation.subjectId}>
                  {getSubjectName(allocation.subjectId)}
                </option>
              ))}
            </select>
          </div>

          {reportType !== 'low-attendance' && (
            <>
              <div className="form-group">
                <label>Start Date</label>
                <input
                  type="date"
                  name="startDate"
                  value={dateRange.startDate}
                  onChange={handleDateChange}
                />
              </div>

              <div className="form-group">
                <label>End Date</label>
                <input
                  type="date"
                  name="endDate"
                  value={dateRange.endDate}
                  onChange={handleDateChange}
                />
              </div>
            </>
          )}
        </div>

        <div className="action-buttons">
          <button className="btn-primary" onClick={handleGenerateReport} disabled={loading}>
            {loading ? 'Generating...' : 'Generate Report'}
          </button>
          {reportData.length > 0 && (
            <>
              <button className="btn-secondary" onClick={handleExportPDF}>
                Export PDF
              </button>
              <button className="btn-secondary" onClick={handleExportCSV}>
                Export CSV
              </button>
            </>
          )}
        </div>
      </div>

      {reportData.length > 0 && (
        <>
          <div className="report-results">
            <h2>Report Results</h2>
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    {reportType === 'attendance' || reportType === 'low-attendance' ? (
                      <>
                        <th>Roll Number</th>
                        <th>Student Name</th>
                        <th>Present</th>
                        <th>Absent</th>
                        <th>Percentage</th>
                      </>
                    ) : (
                      <>
                        <th>Roll Number</th>
                        <th>Student Name</th>
                        <th>Midterm</th>
                        <th>Endterm</th>
                        <th>Assignment</th>
                        <th>Grade</th>
                      </>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {reportData.map((row, index) => (
                    <tr key={index}>
                      {reportType === 'attendance' || reportType === 'low-attendance' ? (
                        <>
                          <td>{row.rollNumber}</td>
                          <td>{row.studentName}</td>
                          <td>{row.present}</td>
                          <td>{row.absent}</td>
                          <td>
                            <span className={`percentage-badge ${row.percentage < 75 ? 'low' : 'good'}`}>
                              {row.percentage}%
                            </span>
                          </td>
                        </>
                      ) : (
                        <>
                          <td>{row.rollNumber}</td>
                          <td>{row.studentName}</td>
                          <td>{row.midterm}</td>
                          <td>{row.endterm}</td>
                          <td>{row.assignment}</td>
                          <td>
                            <span className="grade-badge">{row.grade}</span>
                          </td>
                        </>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="analytics-section">
            <h2>Analytics</h2>
            <div className="chart-container">
              {reportType === 'performance' ? (
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={chartData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, value }) => `${name}: ${value}`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {chartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="percentage" fill="#4CAF50" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default ReportsView;
