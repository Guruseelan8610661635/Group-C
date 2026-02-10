package com.smartparking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.smartparking.dto.BookingDetailDto;
import com.smartparking.service.BookingCacheService;
import com.smartparking.service.BookingService;

@RestController
@RequestMapping("/api/bookings/fast")
@CrossOrigin
public class FastBookingController {

    @Autowired
    private BookingCacheService bookingCacheService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/recent")
    public ResponseEntity<?> getFastRecentBookings(
            Authentication authentication,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            long startTime = System.currentTimeMillis();

            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);

            List<BookingDetailDto> bookings = bookingCacheService.getRecentBookingsFromCache(userId, limit);

            long fetchTime = System.currentTimeMillis() - startTime;

            return ResponseEntity.ok(Map.of(
                    "bookings", bookings,
                    "count", bookings.size(),
                    "fetchTimeMs", fetchTime
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<?> clearUserCache(Authentication authentication) {
        try {
            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);

            bookingCacheService.clearCache(userId);

            return ResponseEntity.ok(Map.of("message", "Cache cleared for user"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cache/clear-all")
    public ResponseEntity<?> clearAllCache() {
        try {
            bookingCacheService.clearAllCache();
            return ResponseEntity.ok(Map.of("message", "All cache cleared"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
