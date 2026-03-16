import axios from '../axios.config';

export const generateAttendanceReportPDF = (params = {}) => {
  return axios.get('/faculty/reports/attendance/pdf', {
    params,
    responseType: 'blob',
  });
};

export const generatePerformanceReportPDF = (params = {}) => {
  return axios.get('/faculty/reports/performance/pdf', {
    params,
    responseType: 'blob',
  });
};

export const getLowAttendanceList = (params = {}) => {
  return axios.get('/faculty/reports/low-attendance', { params });
};

export const getAttendanceReport = (params = {}) => {
  return axios.get('/faculty/reports/attendance', { params })
    .then((response) => {
      console.log("Attendance Report:", response.data);
      return response;
    })
    .catch((error) => {
      console.error("Error fetching attendance report:", error);
      throw error;
    });
};

export const exportAttendanceCSV = (params = {}) => {
  return axios.get('/faculty/reports/attendance/csv', {
    params,
    responseType: 'blob',
  });
};

export const exportPerformanceCSV = (params = {}) => {
  return axios.get('/faculty/reports/performance/csv', {
    params,
    responseType: 'blob',
  });
};
