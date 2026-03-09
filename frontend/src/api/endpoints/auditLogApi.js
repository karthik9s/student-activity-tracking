import axios from '../axios.config';

export const auditLogApi = {
  getAuditLogs: async (params = {}) => {
    const response = await axios.get('/admin/audit-logs', { params });
    return response.data;
  }
};
