import api from "../api/axios";

export const notificationService = {
  /**
   * Get user notifications
   */
  getNotifications: async () => {
    const response = await api.get("/notifications/my");
    return response.data;
  },

  /**
   * Get unread notifications
   */
  getUnreadNotifications: async () => {
    const response = await api.get("/notifications/unread");
    return response.data;
  },

  /**
   * Get unread notification count
   */
  getUnreadCount: async () => {
    const response = await api.get("/notifications/unread/count");
    return response.data;
  },

  /**
   * Mark notification as read
   * @param {number} notificationId
   */
  markAsRead: async (notificationId) => {
    const response = await api.put(`/notifications/${notificationId}/read`);
    return response.data;
  },

  /**
   * Mark all notifications as read
   */
  markAllAsRead: async () => {
    const response = await api.put("/notifications/read-all");
    return response.data;
  },

  /**
   * Delete notification
   * @param {number} notificationId
   */
  deleteNotification: async (notificationId) => {
    const response = await api.delete(`/notifications/${notificationId}`);
    return response.data;
  },
};
