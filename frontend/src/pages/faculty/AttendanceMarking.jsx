import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { getMyAllocations } from '../../api/endpoints/allocationApi';
import { getFacultyStudents } from '../../api/endpoints/facultyApi';
import { markBulkAttendance, getAttendanceBySubjectAndDate } from '../../api/endpoints/attendanceApi';
import './AttendanceMarking.css';

const AttendanceMarking = () => {
  const [allocations, setAllocations] = useState([]);
  const [students, setStudents] = useState([]);
  const [selectedAllocation, setSelectedAllocation] = useState(null);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [attendance, setAttendance] = useState({});
  const [loading, setLoading] = useState(false);
  const [existingAttendance, setExistingAttendance] = useState([]);

  useEffect(() => {
    fetchAllocations();
  }, []);

  useEffect(() => {
    if (selectedAllocation) {
      fetchStudents();
      checkExistingAttendance();
    }
  }, [selectedAllocation, selectedDate]);

  const fetchAllocations = async () => {
    try {
      const response = await getMyAllocations();
      setAllocations(response.data);
    } catch (error) {
      toast.error('Failed to fetch allocations');
      console.error(error);
    }
  };

  const fetchStudents = async () => {
    try {
      setLoading(true);
      const response = await getFacultyStudents(
        selectedAllocation.courseId,
        selectedAllocation.year,
        selectedAllocation.section
      );
      const filteredStudents = response.data.filter(
        s => s.isActive && !s.isDeleted
      );
      setStudents(filteredStudents);
      
      // Initialize attendance state
      const initialAttendance = {};
      filteredStudents.forEach(student => {
        initialAttendance[student.id] = 'PRESENT';
      });
      setAttendance(initialAttendance);
    } catch (error) {
      toast.error('Failed to fetch students');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const checkExistingAttendance = async () => {
    if (!selectedAllocation || !selectedDate) return;
    
    try {
      const response = await getAttendanceBySubjectAndDate(
        selectedAllocation.subjectId,
        selectedDate
      );
      setExistingAttendance(response.data);
      
      // Update attendance state with existing data
      const existingData = {};
      response.data.forEach(record => {
        existingData[record.studentId] = record.status;
      });
      setAttendance(prev => ({ ...prev, ...existingData }));
    } catch (error) {
      setExistingAttendance([]);
    }
  };

  const handleAllocationChange = (e) => {
    const allocation = allocations.find(a => a.id === e.target.value);
    setSelectedAllocation(allocation);
    setStudents([]);
    setAttendance({});
  };

  const handleAttendanceChange = (studentId, status) => {
    setAttendance(prev => ({
      ...prev,
      [studentId]: status
    }));
  };

  const handleMarkAll = (status) => {
    const newAttendance = {};
    students.forEach(student => {
      newAttendance[student.id] = status;
    });
    setAttendance(newAttendance);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedAllocation) {
      toast.error('Please select a class');
      return;
    }

    if (!selectedDate) {
      toast.error('Please select a date');
      return;
    }

    try {
      setLoading(true);
      
      const attendanceList = students.map(student => ({
        studentId: student.id,
        subjectId: selectedAllocation.subjectId,
        facultyId: selectedAllocation.facultyId,
        courseId: selectedAllocation.courseId,
        year: selectedAllocation.year,
        section: selectedAllocation.section,
        date: selectedDate,
        status: attendance[student.id] || 'ABSENT',
        remarks: ''
      }));

      await markBulkAttendance(attendanceList);
      toast.success('Attendance marked successfully');
      setExistingAttendance(attendanceList);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to mark attendance');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getPresentCount = () => {
    return Object.values(attendance).filter(status => status === 'PRESENT').length;
  };

  const getAbsentCount = () => {
    return Object.values(attendance).filter(status => status === 'ABSENT').length;
  };

  return (
    <div className="attendance-marking">
      <div className="page-header">
        <h1>Mark Attendance</h1>
      </div>

      <div className="attendance-form-container">
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="allocation">Select Class *</label>
              <select
                id="allocation"
                className="form-control"
                onChange={handleAllocationChange}
                value={selectedAllocation?.id || ''}
                required
              >
                <option value="">Select Class</option>
                {allocations.map(allocation => (
                  <option key={allocation.id} value={allocation.id}>
                    {allocation.courseName} - {allocation.subjectName} - Year {allocation.year} - Section {allocation.section}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="date">Date *</label>
              <input
                type="date"
                id="date"
                className="form-control"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                max={new Date().toISOString().split('T')[0]}
                required
              />
            </div>
          </div>

          {existingAttendance.length > 0 && (
            <div className="alert alert-info">
              Attendance already marked for this date. You can update it below.
            </div>
          )}

          {selectedAllocation && students.length > 0 && (
            <>
              <div className="attendance-summary">
                <div className="summary-card">
                  <div className="summary-label">Total Students</div>
                  <div className="summary-value">{students.length}</div>
                </div>
                <div className="summary-card present">
                  <div className="summary-label">Present</div>
                  <div className="summary-value">{getPresentCount()}</div>
                </div>
                <div className="summary-card absent">
                  <div className="summary-label">Absent</div>
                  <div className="summary-value">{getAbsentCount()}</div>
                </div>
              </div>

              <div className="bulk-actions">
                <button
                  type="button"
                  className="btn btn-success"
                  onClick={() => handleMarkAll('PRESENT')}
                >
                  Mark All Present
                </button>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={() => handleMarkAll('ABSENT')}
                >
                  Mark All Absent
                </button>
              </div>

              <div className="students-list">
                <table className="attendance-table">
                  <thead>
                    <tr>
                      <th>Roll Number</th>
                      <th>Student Name</th>
                      <th>Email</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {students.map(student => (
                      <tr key={student.id}>
                        <td>{student.rollNumber}</td>
                        <td>{student.name || student.firstName + ' ' + student.lastName || student.rollNumber}</td>
                        <td>{student.email}</td>
                        <td>
                          <div className="attendance-toggle">
                            <label className="radio-label">
                              <input
                                type="radio"
                                name={`attendance-${student.id}`}
                                value="PRESENT"
                                checked={attendance[student.id] === 'PRESENT'}
                                onChange={() => handleAttendanceChange(student.id, 'PRESENT')}
                              />
                              <span className="present-label">Present</span>
                            </label>
                            <label className="radio-label">
                              <input
                                type="radio"
                                name={`attendance-${student.id}`}
                                value="ABSENT"
                                checked={attendance[student.id] === 'ABSENT'}
                                onChange={() => handleAttendanceChange(student.id, 'ABSENT')}
                              />
                              <span className="absent-label">Absent</span>
                            </label>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="form-actions">
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Submitting...' : 'Submit Attendance'}
                </button>
              </div>
            </>
          )}

          {selectedAllocation && students.length === 0 && !loading && (
            <div className="no-data">
              No students found for this class
            </div>
          )}
        </form>
      </div>
    </div>
  );
};

export default AttendanceMarking;
