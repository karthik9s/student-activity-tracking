import axiosInstance from '../axios.config';

export const getAllFaculty = async (params = {}) => {
  const response = await axiosInstance.get('/admin/faculty', { params });
  return response;
};

export const getFacultyById = async (id) => {
  const response = await axiosInstance.get(`/admin/faculty/${id}`);
  return response;
};

export const createFaculty = async (facultyData) => {
  const response = await axiosInstance.post('/admin/faculty', facultyData);
  return response;
};

export const updateFaculty = async (id, facultyData) => {
  const response = await axiosInstance.put(`/admin/faculty/${id}`, facultyData);
  return response;
};

export const deleteFaculty = async (id) => {
  const response = await axiosInstance.delete(`/admin/faculty/${id}`);
  return response;
};

export const searchFaculty = async (searchTerm, params = {}) => {
  const response = await axiosInstance.get('/admin/faculty', {
    params: { ...params, search: searchTerm }
  });
  return response;
};

export const getFacultyStudents = async (courseId, year, section) => {
  const params = { courseId };
  if (year !== undefined && year !== null) params.year = year;
  if (section) params.section = section;
  
  const response = await axiosInstance.get('/faculty/students', { params });
  return response;
};
