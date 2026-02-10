import axios from '../api/axios';

// Base axios already prefixes /api, so keep this route relative
const API_BASE_URL = '/payments';

/**
 * Payment Service
 * Handles payment processing for parking bookings
 */
export const paymentService = {
  /**
   * Process payment for a booking
   * @param {Number} bookingId - The booking ID to process payment for
   * @param {Number} amount - The amount to pay
   * @param {String} paymentMethod - Payment method: 'CARD' or 'UPI'
   * @returns {Promise} Payment result with transaction ID
   */
  processPayment: async (bookingId, amount, paymentMethod) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/process`, {
        bookingId,
        amount,
        paymentMethod,
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get payment history for user
   * @returns {Promise} List of user's payments
   */
  getPaymentHistory: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/history`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get payment details for a specific booking
   * @param {Number} bookingId - The booking ID
   * @returns {Promise} Payment details
   */
  getPaymentByBooking: async (bookingId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/booking/${bookingId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },
};
