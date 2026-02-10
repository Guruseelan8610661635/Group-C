package com.smartparking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.smartparking.dto.PricingTierRequest;
import com.smartparking.model.VehiclePricing;
import com.smartparking.service.AdminVehiclePricingService;

@RestController
@RequestMapping("/api/admin/pricing")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminPricingController {

    @Autowired
    private AdminVehiclePricingService vehiclePricingService;

    @GetMapping
    public ResponseEntity<?> getAllPricings() {
        try {
            List<VehiclePricing> pricings = vehiclePricingService.getAllPricings();
            return ResponseEntity.ok(pricings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{vehicleType}")
    public ResponseEntity<?> getPricingByVehicleType(@PathVariable String vehicleType) {
        try {
            VehiclePricing pricing = vehiclePricingService.getPricingByVehicleType(vehicleType);
            return ResponseEntity.ok(pricing);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createPricing(@RequestBody PricingTierRequest request) {
        try {
            VehiclePricing pricing = vehiclePricingService.createPricing(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(pricing);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{vehicleType}")
    public ResponseEntity<?> updatePricing(
            @PathVariable String vehicleType,
            @RequestBody PricingTierRequest request) {
        try {
            VehiclePricing pricing = vehiclePricingService.updatePricing(vehicleType, request);
            return ResponseEntity.ok(pricing);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{vehicleType}")
    public ResponseEntity<?> deletePricing(@PathVariable String vehicleType) {
        try {
            vehiclePricingService.deletePricing(vehicleType);
            return ResponseEntity.ok(Map.of("message", "Pricing deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
