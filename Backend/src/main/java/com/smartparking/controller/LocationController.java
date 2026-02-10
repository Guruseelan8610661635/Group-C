package com.smartparking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.smartparking.dto.LocationWithPricingDTO;
import com.smartparking.model.Location;
import com.smartparking.repository.LocationRepository;
import com.smartparking.service.MapService;
import com.smartparking.service.PricingConfigService;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin
public class LocationController {

    private final LocationRepository locationRepo;
    private final MapService mapService;
    private final PricingConfigService pricingConfigService;

    public LocationController(LocationRepository locationRepo, MapService mapService, PricingConfigService pricingConfigService) {
        this.locationRepo = locationRepo;
        this.mapService = mapService;
        this.pricingConfigService = pricingConfigService;
    }

    /**
     * GET /api/locations
     * Get all locations
     */
    @GetMapping
    public List<Location> getLocations() {
        return locationRepo.findAll();
    }

    /**
     * GET /api/locations/{id}
     * Get location by ID with pricing information
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLocationById(@PathVariable Long id) {
        try {
            Location location = locationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            
            // Get pricing information
            Map<String, Double> pricing = pricingConfigService.getPricingForLocation(id);
            
            // Return location with pricing
            LocationWithPricingDTO response = new LocationWithPricingDTO(location, pricing);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/locations
     * Create new location with map data and optional pricing (Admin only)
     * Request body can include "pricing" field with vehicle pricing configuration
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createLocation(@RequestBody Map<String, Object> requestBody) {
        try {
            // Extract location data
            Location location = new Location();
            if (requestBody.containsKey("name")) {
                location.setName((String) requestBody.get("name"));
            }
            if (requestBody.containsKey("address")) {
                location.setAddress((String) requestBody.get("address"));
            }
            if (requestBody.containsKey("latitude")) {
                location.setLatitude(((Number) requestBody.get("latitude")).doubleValue());
            }
            if (requestBody.containsKey("longitude")) {
                location.setLongitude(((Number) requestBody.get("longitude")).doubleValue());
            }
            if (requestBody.containsKey("description")) {
                location.setDescription((String) requestBody.get("description"));
            }
            if (requestBody.containsKey("amenities")) {
                location.setAmenities((String) requestBody.get("amenities"));
            }
            if (requestBody.containsKey("operatingHours")) {
                location.setOperatingHours((String) requestBody.get("operatingHours"));
            }
            if (requestBody.containsKey("isActive")) {
                location.setIsActive((Boolean) requestBody.get("isActive"));
            }
            if (requestBody.containsKey("markerColor")) {
                location.setMarkerColor((String) requestBody.get("markerColor"));
            }
            
            // Save location first
            Location saved = locationRepo.save(location);
            
            // Save pricing configuration if provided
            if (requestBody.containsKey("pricing")) {
                @SuppressWarnings("unchecked")
                Map<String, Double> pricing = (Map<String, Double>) requestBody.get("pricing");
                pricingConfigService.savePricingForLocation(saved.getId(), pricing);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/locations/{id}
     * Update location (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateLocation(@PathVariable Long id, @RequestBody Location locationDetails) {
        try {
            Location location = locationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            
            // Update fields
            location.setName(locationDetails.getName());
            location.setLatitude(locationDetails.getLatitude());
            location.setLongitude(locationDetails.getLongitude());
            location.setAddress(locationDetails.getAddress());
            location.setDescription(locationDetails.getDescription());
            location.setAmenities(locationDetails.getAmenities());
            location.setOperatingHours(locationDetails.getOperatingHours());
            location.setIsActive(locationDetails.getIsActive());
            location.setMarkerColor(locationDetails.getMarkerColor());
            
            // Refresh slot counts
            mapService.updateLocationSlotCounts(id);
            
            Location updated = locationRepo.save(location);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/locations/{id}
     * Delete location and associated pricing (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        try {
            // Delete pricing configurations first
            pricingConfigService.deletePricingForLocation(id);
            
            // Delete location
            locationRepo.deleteById(id);
            
            return ResponseEntity.ok(Map.of("message", "Location deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
