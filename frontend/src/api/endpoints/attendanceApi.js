import axios from '../axios.config';

// Faculty endpoints
export const markAttendance = (attendanceData) => {
  return axios.post('/faculty/attendance', attendanceData);
};

export const markBulkAttendance = (attendanceList) => {
  return axios.post('/faculty/attendance/bulk', attendanceList);
};

export const getAttendanceBySubjectAndDate = (subjectId, date) => {
  return axios.get(`/faculty/attendance/subject/${subjectId}/date/${date}`);
};

export const updateAttendance = (id, attendanceData) => {
  return axios.put(`/faculty/attendance/${id}`, attendanceData);
};

// Student endpoints
export const getMyAttendance = () => {
  return axios.get('/student/attendance');
};

export const getMyAttendanceBySubject = (subjectId) => {
  return axios.get(`/student/attendance/subject/${subjectId}`);
};

export const getMyAttendanceSummary = () => {
  return axios.get('/student/attendance/summary');
};
