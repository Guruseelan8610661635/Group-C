/**
 * LocationFormInline Component
 * Inline form for adding/editing parking locations within mobile layout
 */
import { useState, useEffect } from "react";
import PricingConfigForm from "./PricingConfigForm";

export default function LocationFormInline({ location, onSubmit, onClose }) {
  const [formData, setFormData] = useState({
    name: "",
    address: "",
    latitude: "",
    longitude: "",
    description: "",
  });

  const [pricing, setPricing] = useState({
    BIKE: 20.0,
    CAR: 20.0,
    SUV: 20.0,
    TRUCK: 20.0
  });

  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  // Pre-fill form when editing
  useEffect(() => {
    if (location) {
      setFormData({
        name: location.name || "",
        address: location.address || "",
        latitude: location.latitude || "",
        longitude: location.longitude || "",
        description: location.description || "",
      });

      // Load pricing if available
      if (location.pricing) {
        setPricing({
          BIKE: location.pricing.BIKE || 20.0,
          CAR: location.pricing.CAR || 20.0,
          SUV: location.pricing.SUV || 20.0,
          TRUCK: location.pricing.TRUCK || 20.0
        });
      }
    } else {
      setFormData({
        name: "",
        address: "",
        latitude: "",
        longitude: "",
        description: "",
      });
      setPricing({
        BIKE: 20.0,
        CAR: 20.0,
        SUV: 20.0,
        TRUCK: 20.0
      });
    }
    setError("");
  }, [location]);

  const handleSubmit = async (e) => {
    e?.preventDefault();

    // Validation
    if (!formData.name.trim()) {
      setError("Location name is required");
      return;
    }
    if (!formData.address.trim()) {
      setError("Address is required");
      return;
    }

    try {
      setIsLoading(true);
      setError("");

      // Combine form data with pricing
      const submitData = {
        ...formData,
        pricing: pricing
      };

      // Call the onSubmit handler with location ID and combined data
      await onSubmit(location?.id, submitData);

      setIsLoading(false);
      onClose();
    } catch (err) {
      console.error("Submit error:", err);
      setError(err.message || "Failed to save location");
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-white border-b border-gray-200 p-4 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-lg font-bold text-gray-800">
          {location ? "✏️ Edit Location" : "➕ Add New Location"}
        </h3>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600 text-2xl"
        >
          ✕
        </button>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-3">
          <p className="text-red-700 text-sm font-semibold">⚠️ {error}</p>
        </div>
      )}

      {/* Form Fields */}
      <div className="space-y-3">
        {/* Location Name */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">
            Location Name <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            placeholder="e.g., Downtown Parking, Mall Center"
            value={formData.name}
            onChange={(e) =>
              setFormData({ ...formData, name: e.target.value })
            }
            disabled={isLoading}
            className="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-500 disabled:bg-gray-100 text-base"
          />
        </div>

        {/* Address */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">
            Address <span className="text-red-500">*</span>
          </label>
          <textarea
            placeholder="e.g., 123 Main St, City, State 12345"
            value={formData.address}
            onChange={(e) =>
              setFormData({ ...formData, address: e.target.value })
            }
            disabled={isLoading}
            className="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-500 disabled:bg-gray-100 text-base resize-none h-20"
          />
        </div>

        {/* Latitude (Optional) */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">
            Latitude <span className="text-gray-400 text-xs">(Optional)</span>
          </label>
          <input
            type="number"
            placeholder="e.g., 40.7128"
            step="0.0001"
            value={formData.latitude}
            onChange={(e) =>
              setFormData({ ...formData, latitude: e.target.value ? parseFloat(e.target.value) : "" })
            }
            disabled={isLoading}
            className="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-500 disabled:bg-gray-100 text-base"
          />
        </div>

        {/* Longitude (Optional) */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">
            Longitude <span className="text-gray-400 text-xs">(Optional)</span>
          </label>
          <input
            type="number"
            placeholder="e.g., -74.0060"
            step="0.0001"
            value={formData.longitude}
            onChange={(e) =>
              setFormData({ ...formData, longitude: e.target.value ? parseFloat(e.target.value) : "" })
            }
            disabled={isLoading}
            className="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-500 disabled:bg-gray-100 text-base"
          />
        </div>

        {/* Description (Optional) */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">
            Description <span className="text-gray-400 text-xs">(Optional)</span>
          </label>
          <textarea
            placeholder="e.g., Premium downtown parking facility with 24/7 security"
            value={formData.description}
            onChange={(e) =>
              setFormData({ ...formData, description: e.target.value })
            }
            disabled={isLoading}
            className="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-500 disabled:bg-gray-100 text-base resize-none h-16"
          />
        </div>

        {/* Pricing Configuration */}
        <div>
          <PricingConfigForm
            initialPricing={pricing}
            onChange={setPricing}
          />
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3 pt-2">
        <button
          onClick={handleSubmit}
          disabled={isLoading}
          className="flex-1 bg-slate-700 hover:bg-slate-800 disabled:bg-gray-400 text-white font-semibold py-2.5 rounded-lg transition text-base"
        >
          {isLoading ? "⏳ Saving..." : "💾 Save Location"}
        </button>
        <button
          onClick={onClose}
          disabled={isLoading}
          className="flex-1 bg-gray-200 hover:bg-gray-300 disabled:bg-gray-100 text-gray-800 font-semibold py-2.5 rounded-lg transition text-base"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
