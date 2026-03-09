import api from '../axios.config';

export const searchApi = {
  globalSearch: (searchTerm) => api.get(`/api/search/global?q=${encodeURIComponent(searchTerm)}`),
  searchStudents: (searchTerm) => api.get(`/api/search/students?q=${encodeURIComponent(searchTerm)}`),
  searchFaculty: (searchTerm) => api.get(`/api/search/faculty?q=${encodeURIComponent(searchTerm)}`),
};

export default searchApi;
