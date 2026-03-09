import axios from '../axios.config';

export const getAllAllocations = (params = {}) => {
  return axios.get('/admin/allocations', { params });
};

export const getAllocationById = (id) => {
  return axios.get(`/admin/allocations/${id}`);
};

export const getAllocationsByFaculty = (facultyId) => {
  return axios.get(`/admin/faculty/${facultyId}/allocations`);
};

export const createAllocation = (allocationData) => {
  return axios.post('/admin/allocations', allocationData);
};

export const updateAllocation = (id, allocationData) => {
  return axios.put(`/admin/allocations/${id}`, allocationData);
};

export const deleteAllocation = (id) => {
  return axios.delete(`/admin/allocations/${id}`);
};

// Faculty endpoints
export const getMyAllocations = () => {
  return axios.get('/faculty/allocations');
};
