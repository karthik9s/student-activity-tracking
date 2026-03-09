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
