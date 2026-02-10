package com.smartparking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.smartparking.dto.BookingResponse;
import com.smartparking.dto.CheckoutResponse;
import com.smartparking.model.Booking;
import com.smartparking.service.BookingService;

/**
 * Booking Controller
 * Handles parking slot booking, checkout, and booking history endpoints.
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {

    @Autowired
    private BookingService service;

    /**
     * Book a parking slot
     * POST /api/bookings/book
     */
    @PostMapping("/book")
    public ResponseEntity<?> bookSlot(
            @RequestBody Booking booking,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            Long userId = service.getUserIdByEmail(email);

            booking.setUserId(userId);
            service.bookSlot(booking);

            return ResponseEntity.ok(new BookingResponse(
                    booking.getId(),
                    booking.getSlotId(),
                    "Slot-" + booking.getSlotId(),
                    booking.getVehicleType() != null ? booking.getVehicleType().name() : "CAR",
                    booking.getEntryTime(),
                    booking.getExitTime(),
                    booking.getStatus().toString(),
                    null,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Checkout a booking (exit parking slot)
     * POST /api/bookings/{bookingId}/checkout
     * Records exit time, calculates fee, processes payment
     */
    @PostMapping("/{bookingId}/checkout")
    public ResponseEntity<?> checkoutBooking(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            Long userId = service.getUserIdByEmail(email);

            // Verify booking belongs to authenticated user
            Booking booking = service.getBookingById(bookingId);
            if (!booking.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body("Unauthorized: Booking does not belong to user");
            }

            // Process checkout
            CheckoutResponse response = service.checkoutBooking(bookingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Cancel a booking
     * DELETE /api/bookings/{bookingId}
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            Long userId = service.getUserIdByEmail(email);

            // Verify booking belongs to authenticated user
            Booking booking = service.getBookingById(bookingId);
            if (!booking.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body("Unauthorized: Booking does not belong to user");
            }

            service.cancelBooking(bookingId);
            return ResponseEntity.ok("Booking cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get active bookings for authenticated user
     * GET /api/bookings/my
     */
    @GetMapping("/my")
    public ResponseEntity<?> getUserBookings(Authentication auth) {
        System.out.println("🔵 ========================================");
        System.out.println("🔵 GET /api/bookings/my endpoint called");
        System.out.println("🔵 ========================================");
        
        // ... (existing logging code) ...
        
        try {
            String email = auth.getName();
            Long userId = service.getUserIdByEmail(email);
            
            // ... (rest of implementation) ...
            List<BookingResponse> bookings = service.getUserBookings(userId);
            
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // REMOVED /history/current and /history/past as they are handled by BookingHistoryController


    /**
     * Get booking details by ID
     * GET /api/bookings/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingDetails(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            Long userId = service.getUserIdByEmail(email);

            Booking booking = service.getBookingById(bookingId);
            if (!booking.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body("Unauthorized: Booking does not belong to user");
            }

            BookingResponse response = new BookingResponse(
                    booking.getId(),
                    booking.getSlotId(),
                    "Slot-" + booking.getSlotId(),
                    booking.getVehicleType() != null ? booking.getVehicleType().name() : "CAR",
                    booking.getEntryTime(),
                    booking.getExitTime(),
                    booking.getStatus().toString(),
                    booking.getParkingFee(),
                    booking.getTransactionId()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DEBUG: Get user info and ALL bookings (for troubleshooting)
     * GET /api/bookings/debug
     */
    @GetMapping("/debug")
    public ResponseEntity<?> debugUserBookings(Authentication auth) {
        System.out.println("🔧 ========================================");
        System.out.println("🔧 GET /api/bookings/debug endpoint called");
        System.out.println("🔧 ========================================");
        
        try {
            String email = auth.getName();
            Long userId = service.getUserIdByEmail(email);
            
            // Get all bookings for this user
            List<BookingResponse> userBookings = service.getUserBookings(userId);
            
            // Get ALL bookings from database (requires admin access to see, but let's see what we get)
            List<Booking> allBookings = service.getBookingsByUser(userId);
            
            java.util.Map<String, Object> debugInfo = new java.util.HashMap<>();
            debugInfo.put("authenticatedEmail", email);
            debugInfo.put("userId", userId);
            debugInfo.put("bookingsCount", userBookings.size());
            debugInfo.put("rawBookingsCount", allBookings.size());
            debugInfo.put("bookings", userBookings);
            debugInfo.put("rawBookingIds", allBookings.stream().map(Booking::getId).collect(java.util.stream.Collectors.toList()));
            debugInfo.put("rawBookingStatuses", allBookings.stream().map(b -> b.getStatus().toString()).collect(java.util.stream.Collectors.toList()));
            
            System.out.println("🔧 Debug Info: " + debugInfo);
            System.out.println("🔧 ========================================");
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            System.out.println("❌ DEBUG ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * FIX: Repair corrupted ACTIVE bookings that have exitTime set
     * POST /api/bookings/fix-corrupted
     * This endpoint fixes bookings where status='ACTIVE' but exitTime is not NULL
     */
    @PostMapping("/fix-corrupted")
    public ResponseEntity<?> fixCorruptedBookings(Authentication auth) {
        System.out.println("🔧 ========================================");
        System.out.println("🔧 POST /api/bookings/fix-corrupted endpoint called");
        System.out.println("🔧 ========================================");
        
        try {
            String email = auth.getName();
            Long userId = service.getUserIdByEmail(email);
            
            // Get all bookings for this user
            List<Booking> allBookings = service.getBookingsByUser(userId);
            
            // Find corrupted ACTIVE bookings (status=ACTIVE but exitTime is set)
            List<Booking> corruptedBookings = allBookings.stream()
                .filter(b -> b.getStatus().toString().equals("ACTIVE") && b.getExitTime() != null)
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println("🔧 Found " + corruptedBookings.size() + " corrupted booking(s)");
            
            if (corruptedBookings.isEmpty()) {
                return ResponseEntity.ok(java.util.Map.of(
                    "message", "No corrupted bookings found",
                    "fixedCount", 0
                ));
            }
            
            // Fix each corrupted booking by using the service's fix method
            List<Long> fixedIds = new java.util.ArrayList<>();
            for (Booking booking : corruptedBookings) {
                System.out.println("🔧 Fixing booking ID: " + booking.getId());
                System.out.println("   - Before: exitTime=" + booking.getExitTime());
                
                try {
                    Booking fixed = service.fixCorruptedBooking(booking.getId());
                    System.out.println("   - After: exitTime=" + fixed.getExitTime());
                    fixedIds.add(booking.getId());
                } catch (Exception e) {
                    System.out.println("   - Failed to fix: " + e.getMessage());
                }
            }
            
            System.out.println("✅ Fixed " + fixedIds.size() + " corrupted booking(s)");
            System.out.println("🔧 ========================================");
            
            return ResponseEntity.ok(java.util.Map.of(
                "message", "Corrupted bookings fixed successfully",
                "fixedCount", fixedIds.size(),
                "bookingIds", fixedIds
            ));
        } catch (Exception e) {
            System.out.println("❌ FIX ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
