package com.smartparking.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.smartparking.model.UserSubscription;
import com.smartparking.service.SubscriptionService;
import com.smartparking.service.BookingService;

@RestController
@RequestMapping("/api/subscription")
@CrossOrigin
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/my")
    public ResponseEntity<?> getMySubscription(Authentication authentication) {
        try {
            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);
            
            UserSubscription subscription = subscriptionService.getUserSubscription(userId);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(
            Authentication authentication,
            @RequestParam String planType,
            @RequestParam Double monthlyAmount,
            @RequestParam Integer slots) {
        try {
            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);
            
            UserSubscription subscription = subscriptionService.createSubscription(userId, planType, monthlyAmount, slots);
            return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/upgrade")
    public ResponseEntity<?> upgradeSubscription(
            Authentication authentication,
            @RequestParam String planType,
            @RequestParam Double monthlyAmount,
            @RequestParam Integer slots) {
        try {
            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);
            
            UserSubscription subscription = subscriptionService.updateSubscription(userId, planType, monthlyAmount, slots);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelSubscription(Authentication authentication) {
        try {
            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);
            
            subscriptionService.cancelSubscription(userId);
            return ResponseEntity.ok(Map.of("message", "Subscription cancelled"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkSubscriptionStatus(Authentication authentication) {
        try {
            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);
            
            Boolean hasSubscription = subscriptionService.hasActiveSubscription(userId);
            return ResponseEntity.ok(Map.of("hasActiveSubscription", hasSubscription));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
