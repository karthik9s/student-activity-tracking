import axios from '../axios.config';

export const adminApi = {
  getDashboardStats: async () => {
    const response = await axios.get('/admin/dashboard/stats');
    return response.data;
  }
};
