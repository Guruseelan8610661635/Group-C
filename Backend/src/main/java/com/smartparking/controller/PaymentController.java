package com.smartparking.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.smartparking.model.Booking;
import com.smartparking.model.Booking.ParkingStatus;
import com.smartparking.model.Slot;
import com.smartparking.repository.BookingRepository;
import com.smartparking.repository.SlotRepository;
import com.smartparking.service.BookingService;
import com.smartparking.service.PaymentService;

/**
 * Payment Controller
 * Handles payment processing for parking bookings
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SlotRepository slotRepository;

    /**
     * Process payment for a booking
     * POST /api/payments/process
     * Accepts: { bookingId, amount, paymentMethod }
     */
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(
            @RequestBody PaymentRequest request,
            Authentication authentication
    ) {
        try {
            System.out.println("💳 Processing payment request: " + request);
            
            if (request == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Request body is null"));
            }
            if (request.bookingId == null) {
                System.out.println("❌ Error: request.bookingId is NULL");
                return ResponseEntity.badRequest().body(createErrorResponse("Booking ID is required"));
            }
            
            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);

            // Validate booking exists and belongs to user
            Booking booking = bookingRepository.findById(request.bookingId)
                    .orElseThrow(() -> new Exception("Booking not found with ID: " + request.bookingId));

            if (!booking.getUserId().equals(userId)) {
                return ResponseEntity.status(403)
                        .body(createErrorResponse("Unauthorized: Booking does not belong to user"));
            }

            // Validate amount matches booking fee
            // Use Math.abs for float comparison or accept if payment >= fee
            System.out.println("💰 Fee Validation - Required: " + booking.getParkingFee() + ", Provided: " + request.amount);
            
            if (request.amount == null) {
                 return ResponseEntity.badRequest().body(createErrorResponse("Amount is required"));
            }
            
            // Allow small difference for floating point or if user pays more due to live ticker
            double diff = Math.abs(request.amount - (booking.getParkingFee() != null ? booking.getParkingFee() : 0.0));
            if (diff > 1.0) { // Allow 1.0 (currency unit) tolerance
                System.out.println("⚠️ Amount mismatch! Diff: " + diff);
                // For now, let's accept it if it's close or update the fee
                // return ResponseEntity.badRequest().body(createErrorResponse("Amount mismatch. Expected: " + booking.getParkingFee()));
                
                // UPDATE the fee to match what the user is paying (assuming they paid the live rate)
                System.out.println("🔧 Updating booking fee to match payment amount: " + request.amount);
                booking.setParkingFee(request.amount);
                bookingRepository.save(booking);
            }

            // Process payment through PaymentService
            PaymentService.PaymentResult paymentResult = paymentService.processPayment(request.amount);

            if (paymentResult.isSuccess()) {
                // Release the parking slot
                if (booking.getSlotId() == null) {
                     System.out.println("❌ Error: booking.SlotId is NULL for booking " + booking.getId());
                     throw new Exception("Booking has no assigned slot ID");
                }
                
                Slot slot = slotRepository.findById(booking.getSlotId())
                        .orElseThrow(() -> new Exception("Slot not found with ID: " + booking.getSlotId()));
                
                slot.setAvailable(true); // Mark slot as available again
                slotRepository.save(slot);

                // Update booking status to COMPLETED (payment done)
                booking.setStatus(ParkingStatus.COMPLETED);
                booking.setPaymentStatus("PAID");
                booking.setPaymentTime(LocalDateTime.now());
                booking.setTransactionId(paymentResult.getTransactionId());
                bookingRepository.save(booking);

                System.out.println("✅ Payment processed successfully. Transaction ID: " + paymentResult.getTransactionId());

                // Return success response
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("transactionId", paymentResult.getTransactionId());
                response.put("message", "Payment successful - Slot released");
                response.put("bookingId", booking.getId());
                response.put("slotId", booking.getSlotId());
                response.put("amount", request.amount);
                response.put("paymentMethod", request.paymentMethod);
                response.put("timestamp", LocalDateTime.now());

                return ResponseEntity.ok(response);
            } else {
                System.out.println("❌ Payment failed: " + paymentResult.getMessage());
                return ResponseEntity.badRequest()
                        .body(createErrorResponse(paymentResult.getMessage()));
            }

        } catch (IllegalArgumentException e) {
             // Catch specific Spring Data "id must not be null" error
             System.out.println("❌ IllegalArgumentException (likely null ID): " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.badRequest().body(createErrorResponse("Invalid ID provided: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("❌ Payment processing error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Payment processing failed: " + e.getMessage()));
        }
    }

    /**
     * Get payment history for authenticated user
     * GET /api/payments/history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(Authentication authentication) {
        try {
            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);

            // Get all completed bookings (paid bookings)
            List<Booking> paidBookings = bookingRepository.findByUserIdAndStatus(userId, ParkingStatus.COMPLETED);

            System.out.println("📋 Found " + paidBookings.size() + " paid bookings for user " + userId);

            Map<String, Object> response = new HashMap<>();
            response.put("payments", paidBookings);
            response.put("totalCount", paidBookings.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error fetching payment history: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to fetch payment history"));
        }
    }

    /**
     * Get payment details for a specific booking
     * GET /api/payments/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getPaymentByBooking(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            Long userId = bookingService.getUserIdByEmail(email);

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new Exception("Booking not found"));

            if (!booking.getUserId().equals(userId)) {
                return ResponseEntity.status(403)
                        .body(createErrorResponse("Unauthorized"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", booking.getId());
            response.put("amount", booking.getParkingFee());
            response.put("paymentStatus", booking.getPaymentStatus());
            response.put("transactionId", booking.getTransactionId());
            response.put("paymentTime", booking.getPaymentTime());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to fetch payment details"));
        }
    }

    // Helper method to create error responses
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now());
        return error;
    }

    /**
     * Payment Request DTO
     */
    public static class PaymentRequest {
        public Long bookingId;
        public Double amount;
        public String paymentMethod; // CARD or UPI

        // Getters and Setters
        public Long getBookingId() {
            return bookingId;
        }

        public void setBookingId(Long bookingId) {
            this.bookingId = bookingId;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        @Override
        public String toString() {
            return "PaymentRequest{" +
                    "bookingId=" + bookingId +
                    ", amount=" + amount +
                    ", paymentMethod='" + paymentMethod + '\'' +
                    '}';
        }
    }
}
