import axios from '../axios.config';

// Faculty endpoints
export const addPerformance = (performanceData) => {
  return axios.post('/faculty/performance', performanceData);
};

export const getPerformanceBySubject = (subjectId) => {
  return axios.get(`/faculty/performance/subject/${subjectId}`);
};

export const updatePerformance = (id, performanceData) => {
  return axios.put(`/faculty/performance/${id}`, performanceData);
};

// Student endpoints
export const getMyPerformance = () => {
  return axios.get('/student/performance');
};

export const getMyPerformanceBySubject = (subjectId) => {
  return axios.get(`/student/performance/subject/${subjectId}`);
};

export const getMyPerformanceSummary = () => {
  return axios.get('/student/performance/summary');
};

export const getMyDashboardStats = () => {
  return axios.get('/student/dashboard/stats');
};
