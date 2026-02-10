import api from "../api/axios";

export const promotionService = {
  /**
   * Get active promotions
   */
  getActivePromotions: async () => {
    const response = await api.get("/promotions/active");
    return response.data;
  },

  /**
   * Validate promotion code
   * @param {string} code
   */
  validatePromoCode: async (code) => {
    const response = await api.get(`/promotions/code/${code}`);
    return response.data;
  },

  /**
   * Create promotion (admin only)
   * @param {Object} data - {code, description, discountPercentage, usageLimit}
   */
  createPromotion: async (data) => {
    const response = await api.post("/promotions/admin", data);
    return response.data;
  },
};
