import axios from '../axios.config';

export const getAllSubjects = (params = {}) => {
  return axios.get('/admin/subjects', { params });
};

export const getSubjectById = (id) => {
  return axios.get(`/admin/subjects/${id}`);
};

export const getSubjectsByCourse = (courseId) => {
  return axios.get(`/admin/courses/${courseId}/subjects`);
};

export const createSubject = (subjectData) => {
  return axios.post('/admin/subjects', subjectData);
};

export const updateSubject = (id, subjectData) => {
  return axios.put(`/admin/subjects/${id}`, subjectData);
};

export const deleteSubject = (id) => {
  return axios.delete(`/admin/subjects/${id}`);
};

export const searchSubjects = (search, params = {}) => {
  return axios.get('/admin/subjects', { params: { ...params, search } });
};
