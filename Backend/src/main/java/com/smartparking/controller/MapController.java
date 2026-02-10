package com.smartparking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartparking.dto.LocationMapDto;
import com.smartparking.dto.SlotMapLayoutDto;
import com.smartparking.service.MapService;

@RestController
@RequestMapping("/api/map")
@CrossOrigin
public class MapController {

    @Autowired
    private MapService mapService;

    /**
     * Get all locations for map display
     * GET /api/map/locations
     */
    @GetMapping("/locations")
    public ResponseEntity<?> getAllLocationsForMap() {
        try {
            List<LocationMapDto> locations = mapService.getActiveLocationsForMap();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all locations including inactive ones (Admin only)
     * GET /api/map/locations/all
     */
    @GetMapping("/locations/all")
    public ResponseEntity<?> getAllLocations() {
        try {
            List<LocationMapDto> locations = mapService.getAllLocationsForMap();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get locations near user's current position
     * GET /api/map/locations/nearby?lat=40.7128&lon=-74.0060&radius=5
     * 
     * @param lat User's latitude
     * @param lon User's longitude
     * @param radius Search radius in kilometers (default: 10km)
     */
    @GetMapping("/locations/nearby")
    public ResponseEntity<?> getLocationsNearby(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "10") Double radius) {
        try {
            List<LocationMapDto> locations = mapService.getLocationsNearby(lat, lon, radius);
            
            return ResponseEntity.ok(Map.of(
                    "userLocation", Map.of("latitude", lat, "longitude", lon),
                    "searchRadius", radius,
                    "count", locations.size(),
                    "locations", locations
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get location details with real-time slot data
     * GET /api/map/location/{id}
     */
    @GetMapping("/location/{id}")
    public ResponseEntity<?> getLocationDetails(@PathVariable Long id) {
        try {
            LocationMapDto location = mapService.getLocationMapDetails(id);
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get slot layout for a specific location
     * GET /api/map/location/{id}/slots
     */
    @GetMapping("/location/{id}/slots")
    public ResponseEntity<?> getSlotLayout(@PathVariable Long id) {
        try {
            List<SlotMapLayoutDto> slots = mapService.getSlotLayoutByLocation(id);
            
            return ResponseEntity.ok(Map.of(
                    "locationId", id,
                    "totalSlots", slots.size(),
                    "availableSlots", slots.stream().filter(SlotMapLayoutDto::getAvailable).count(),
                    "slots", slots
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get slot layout by zone for a location
     * GET /api/map/location/{id}/slots/zone/{zone}
     */
    @GetMapping("/location/{id}/slots/zone/{zone}")
    public ResponseEntity<?> getSlotLayoutByZone(
            @PathVariable Long id,
            @PathVariable String zone) {
        try {
            List<SlotMapLayoutDto> allSlots = mapService.getSlotLayoutByLocation(id);
            
            List<SlotMapLayoutDto> zoneSlots = allSlots.stream()
                    .filter(slot -> zone.equalsIgnoreCase(slot.getZone()))
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                    "locationId", id,
                    "zone", zone,
                    "totalSlots", zoneSlots.size(),
                    "availableSlots", zoneSlots.stream().filter(SlotMapLayoutDto::getAvailable).count(),
                    "slots", zoneSlots
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Refresh location slot counts (for real-time updates)
     * POST /api/map/location/{id}/refresh
     */
    @PostMapping("/location/{id}/refresh")
    public ResponseEntity<?> refreshLocationData(@PathVariable Long id) {
        try {
            mapService.updateLocationSlotCounts(id);
            LocationMapDto location = mapService.getLocationMapDetails(id);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Location data refreshed",
                    "location", location
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get map bounds for all locations (for auto-zoom on map)
     * GET /api/map/bounds
     */
    @GetMapping("/bounds")
    public ResponseEntity<?> getMapBounds() {
        try {
            List<LocationMapDto> locations = mapService.getActiveLocationsForMap();
            
            if (locations.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "message", "No locations available",
                        "bounds", Map.of()
                ));
            }
            
            double minLat = locations.stream()
                    .filter(l -> l.getLatitude() != null)
                    .mapToDouble(LocationMapDto::getLatitude)
                    .min().orElse(0);
            
            double maxLat = locations.stream()
                    .filter(l -> l.getLatitude() != null)
                    .mapToDouble(LocationMapDto::getLatitude)
                    .max().orElse(0);
            
            double minLon = locations.stream()
                    .filter(l -> l.getLongitude() != null)
                    .mapToDouble(LocationMapDto::getLongitude)
                    .min().orElse(0);
            
            double maxLon = locations.stream()
                    .filter(l -> l.getLongitude() != null)
                    .mapToDouble(LocationMapDto::getLongitude)
                    .max().orElse(0);
            
            return ResponseEntity.ok(Map.of(
                    "southWest", Map.of("latitude", minLat, "longitude", minLon),
                    "northEast", Map.of("latitude", maxLat, "longitude", maxLon),
                    "center", Map.of(
                            "latitude", (minLat + maxLat) / 2,
                            "longitude", (minLon + maxLon) / 2
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search locations by name or address
     * GET /api/map/search?query=downtown
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchLocations(@RequestParam String query) {
        try {
            List<LocationMapDto> allLocations = mapService.getActiveLocationsForMap();
            
            String queryLower = query.toLowerCase();
            List<LocationMapDto> results = allLocations.stream()
                    .filter(loc -> 
                            (loc.getName() != null && loc.getName().toLowerCase().contains(queryLower)) ||
                            (loc.getAddress() != null && loc.getAddress().toLowerCase().contains(queryLower)) ||
                            (loc.getDescription() != null && loc.getDescription().toLowerCase().contains(queryLower))
                    )
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                    "query", query,
                    "count", results.size(),
                    "results", results
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
