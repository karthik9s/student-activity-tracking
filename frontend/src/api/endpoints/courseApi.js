import axios from '../axios.config';

export const getAllCourses = (params = {}) => {
  return axios.get('/admin/courses', { params });
};

export const getCourseById = (id) => {
  return axios.get(`/admin/courses/${id}`);
};

export const createCourse = (courseData) => {
  return axios.post('/admin/courses', courseData);
};

export const updateCourse = (id, courseData) => {
  return axios.put(`/admin/courses/${id}`, courseData);
};

export const deleteCourse = (id) => {
  return axios.delete(`/admin/courses/${id}`);
};

export const searchCourses = (search, params = {}) => {
  return axios.get('/admin/courses', { params: { ...params, search } });
};
