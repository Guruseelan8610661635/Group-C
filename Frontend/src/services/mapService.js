import apiClient from '../api/axios';

const mapService = {
  // Get all active locations
  getAllLocations: async () => {
    try {
      const response = await apiClient.get('/map/locations');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Get nearby locations
  getNearbyLocations: async (lat, lon, radius = 10) => {
    try {
      const response = await apiClient.get('/map/locations/nearby', {
        params: { lat, lon, radius }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Get location details
  getLocationDetails: async (locationId) => {
    try {
      const response = await apiClient.get(`/map/location/${locationId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Get slot layout
  getSlotLayout: async (locationId) => {
    try {
      const response = await apiClient.get(`/map/location/${locationId}/slots`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Get slot layout by zone
  getSlotLayoutByZone: async (locationId, zone) => {
    try {
      const response = await apiClient.get(`/map/location/${locationId}/slots/zone/${zone}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Refresh location data
  refreshLocationData: async (locationId) => {
    try {
      const response = await apiClient.post(`/map/location/${locationId}/refresh`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Get map bounds
  getMapBounds: async () => {
    try {
      const response = await apiClient.get('/map/bounds');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Search locations
  searchLocations: async (query) => {
    try {
      const response = await apiClient.get('/map/search', {
        params: { query }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  }
};

export default mapService;
