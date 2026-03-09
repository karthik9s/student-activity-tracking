import axios from '../axios.config';

export const notificationApi = {
  getNotifications: async () => {
    const response = await axios.get('/student/notifications');
    return response.data;
  },

  getUnreadNotifications: async () => {
    const response = await axios.get('/student/notifications/unread');
    return response.data;
  },

  getUnreadCount: async () => {
    const response = await axios.get('/student/notifications/unread/count');
    return response.data;
  },

  markAsRead: async (id) => {
    const response = await axios.put(`/student/notifications/${id}/read`);
    return response.data;
  },

  markAllAsRead: async () => {
    const response = await axios.put('/student/notifications/read-all');
    return response.data;
  }
};
