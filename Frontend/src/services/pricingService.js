import axios from 'axios';

const API_URL = 'http://localhost:8080/api/pricing';

/**
 * Pricing Service
 * Handles API calls for location-specific vehicle pricing
 */

/**
 * Get pricing configuration for a location
 * @param {number} locationId - Location ID
 * @returns {Promise} Pricing data
 */
export const getPricingForLocation = async (locationId) => {
  try {
    const token = localStorage.getItem('token');
    const response = await axios.get(`${API_URL}/${locationId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching pricing:', error);
    throw error;
  }
};

/**
 * Save or update pricing configuration for a location
 * @param {number} locationId - Location ID
 * @param {Object} pricingData - Pricing data { BIKE: 15.0, CAR: 25.0, SUV: 35.0, TRUCK: 50.0 }
 * @returns {Promise} Response data
 */
export const savePricingForLocation = async (locationId, pricingData) => {
  try {
    const token = localStorage.getItem('token');
    const response = await axios.post(`${API_URL}/${locationId}`, pricingData, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error saving pricing:', error);
    throw error;
  }
};

/**
 * Delete pricing configuration for a location
 * @param {number} locationId - Location ID
 * @returns {Promise} Response data
 */
export const deletePricingForLocation = async (locationId) => {
  try {
    const token = localStorage.getItem('token');
    const response = await axios.delete(`${API_URL}/${locationId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error deleting pricing:', error);
    throw error;
  }
};

/**
 * Get default pricing rates
 * @returns {Promise} Default pricing data
 */
export const getDefaultPricing = async () => {
  try {
    const response = await axios.get(`${API_URL}/default`);
    return response.data;
  } catch (error) {
    console.error('Error fetching default pricing:', error);
    throw error;
  }
};

/**
 * Get price for a specific vehicle type at a location
 * @param {number} locationId - Location ID
 * @param {string} vehicleType - Vehicle type (BIKE, CAR, SUV, TRUCK)
 * @returns {Promise} Price data
 */
export const getPriceForVehicleType = async (locationId, vehicleType) => {
  try {
    const token = localStorage.getItem('token');
    const response = await axios.get(`${API_URL}/${locationId}/${vehicleType}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching price for vehicle type:', error);
    throw error;
  }
};

export default {
  getPricingForLocation,
  savePricingForLocation,
  deletePricingForLocation,
  getDefaultPricing,
  getPriceForVehicleType
};

// Backward compatibility: export as named object for existing imports
export const pricingService = {
  getPricingForLocation,
  savePricingForLocation,
  deletePricingForLocation,
  getDefaultPricing,
  getPriceForVehicleType,
  // Legacy method for backward compatibility
  getPricingByType: async (vehicleType) => {
    try {
      const response = await getDefaultPricing();
      const pricing = response.pricing || [];
      const vehiclePrice = pricing.find(p => p.vehicleType === vehicleType);
      return {
        hourlyRate: vehiclePrice?.pricePerHour || 20.0,
        ratePerHour: vehiclePrice?.pricePerHour || 20.0
      };
    } catch (error) {
      console.error('Error fetching pricing by type:', error);
      return { hourlyRate: 20.0, ratePerHour: 20.0 };
    }
  }
};
