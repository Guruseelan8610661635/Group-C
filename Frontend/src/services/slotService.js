import api from "../api/axios";

export const slotService = {
  /**
   * Get all slots by location
   * @param {number} locationId
   */
  getSlotsByLocation: async (locationId) => {
    console.log(`🔍 [slotService] Fetching slots for location ID: ${locationId}`);
    const response = await api.get(`/admin/slots/location/${locationId}`);
    console.log(`✅ [slotService] Received response:`, response.data);
    return response.data;
  },

  /**
   * Get all slots (admin only)
   */
  getAllSlots: async () => {
    const response = await api.get("/admin/slots");
    return response.data;
  },

  /**
   * Add new slot (admin only)
   * @param {Object} data - {slotNumber, locationId, slotType}
   */
  addSlot: async (data) => {
    const response = await api.post("/admin/slots", data);
    return response.data;
  },

  /**
   * Update slot (admin only)
   * @param {number} id
   * @param {Object} data
   */
  updateSlot: async (id, data) => {
    const response = await api.put(`/admin/slots/${id}`, data);
    return response.data;
  },

  /**
   * Delete slot (admin only)
   * @param {number} id
   */
  deleteSlot: async (id) => {
    const response = await api.delete(`/admin/slots/${id}`);
    return response.data;
  },

  /**
   * Toggle slot availability (admin only)
   * @param {number} id
   */
  toggleSlotAvailability: async (id) => {
    const response = await api.put(`/admin/slots/${id}/toggle`);
    return response.data;
  },

  /**
   * Disable slot for maintenance (admin only)
   * @param {number} id
   * @param {string} maintenanceNotes - Optional maintenance notes
   */
  disableSlot: async (id, maintenanceNotes) => {
    const response = await api.put(`/admin/slots/${id}/disable`, {
      maintenanceNotes: maintenanceNotes || null,
    });
    return response.data;
  },

  /**
   * Enable slot (remove from maintenance) (admin only)
   * @param {number} id
   */
  enableSlot: async (id) => {
    const response = await api.put(`/admin/slots/${id}/enable`);
    return response.data;
  },

  /**
   * Update maintenance notes for a slot (admin only)
   * @param {number} id
   * @param {string} maintenanceNotes
   */
  updateMaintenanceNotes: async (id, maintenanceNotes) => {
    const response = await api.put(`/admin/slots/${id}/maintenance-notes`, {
      maintenanceNotes,
    });
    return response.data;
  },
};
