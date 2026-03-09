import axiosInstance from '../axios.config';

// Student endpoints (for students)
export const getMyProfile = () => {
  return axiosInstance.get('/student/profile');
};

export const getMySubjects = () => {
  return axiosInstance.get('/student/subjects');
};

export const getMyDashboardStats = () => {
  return axiosInstance.get('/student/dashboard/stats');
};

// Admin endpoints (for managing students)
export const getAllStudents = () => {
  return axiosInstance.get('/admin/students');
};

export const getStudentById = (id) => {
  return axiosInstance.get(`/admin/students/${id}`);
};

export const createStudent = (studentData) => {
  return axiosInstance.post('/admin/students', studentData);
};

export const updateStudent = (id, studentData) => {
  return axiosInstance.put(`/admin/students/${id}`, studentData);
};

export const deleteStudent = (id) => {
  return axiosInstance.delete(`/admin/students/${id}`);
};
