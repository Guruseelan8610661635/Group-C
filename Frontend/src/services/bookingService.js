import api from "../api/axios";

export const bookingService = {
  /**
   * Book a parking slot
   * @param {Object} data - {slotId, vehicleType, entryTime}
   */
  bookSlot: async (data) => {
    const response = await api.post("/bookings/book", data);
    return response.data;
  },

  /**
   * Checkout booking
   * @param {number} bookingId
   */
  checkout: async (bookingId) => {
    const response = await api.post(`/bookings/${bookingId}/checkout`);
    return response.data;
  },

  /**
   * Cancel booking
   * @param {number} bookingId
   */
  cancelBooking: async (bookingId) => {
    const response = await api.delete(`/bookings/${bookingId}`);
    return response.data;
  },

  /**
   * Get user bookings
   */
  getUserBookings: async () => {
    const response = await api.get("/bookings/my");
    return response.data;
  },

  /**
   * Get booking details
   * @param {number} bookingId
   */
  getBookingDetails: async (bookingId) => {
    const response = await api.get(`/bookings/${bookingId}`);
    return response.data;
  },

  /**
   * Get current bookings (paginated)
   * @param {number} pageNumber
   * @param {number} pageSize
   */
  getCurrentBookings: async (pageNumber = 0, pageSize = 20) => {
    const response = await api.get(
      `/bookings/history/current?pageNumber=${pageNumber}&pageSize=${pageSize}`
    );
    return response.data;
  },

  /**
   * Get past bookings (paginated)
   * @param {number} pageNumber
   * @param {number} pageSize
   */
  getPastBookings: async (pageNumber = 0, pageSize = 20) => {
    const response = await api.get(
      `/bookings/history/past?pageNumber=${pageNumber}&pageSize=${pageSize}`
    );
    return response.data;
  },

  /**
   * Get booking status
   * @param {number} bookingId
   */
  getBookingStatus: async (bookingId) => {
    const response = await api.get(`/bookings/status/${bookingId}`);
    return response.data;
  },

  /**
   * Get live booking status
   * @param {number} bookingId
   */
  getLiveBookingStatus: async (bookingId) => {
    const response = await api.get(`/bookings/status/${bookingId}/live`);
    return response.data;
  },

  /**
   * Get recent bookings (quick)
   * @param {number} limit
   */
  getRecentBookings: async (limit = 10) => {
    const response = await api.get(
      `/bookings/history/quick/recent?limit=${limit}`
    );
    return response.data;
  },

  /**
   * Get fast recent bookings (cached)
   * @param {number} limit
   */
  getFastRecentBookings: async (limit = 10) => {
    const response = await api.get(
      `/bookings/fast/recent?limit=${limit}`
    );
    return response.data;
  },
};
