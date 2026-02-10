package com.smartparking.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartparking.dto.BookingResponse;
import com.smartparking.dto.CheckoutResponse;
import com.smartparking.model.Booking;
import com.smartparking.model.Booking.ParkingStatus;
import com.smartparking.model.Slot;
import com.smartparking.model.User;
import com.smartparking.model.Vehicle;
import com.smartparking.repository.BookingRepository;
import com.smartparking.repository.SlotRepository;
import com.smartparking.repository.UserRepository;
import com.smartparking.repository.VehicleRepository;

/**
 * Booking Service
 * Handles parking slot booking, checkout, and slot release with integrated payment processing.
 * Uses pessimistic locking to prevent race conditions during concurrent booking attempts.
 */
@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private SlotRepository slotRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private FeeCalculationService feeCalculationService;

    @Autowired
    private PaymentService paymentService;

    // ============================================
    // USER AUTHENTICATION
    // ============================================

    /**
     * Get user ID from email (JWT authentication)
     */
    public Long getUserIdByEmail(String email) {
        System.out.println("🔍 BookingService.getUserIdByEmail called");
        System.out.println("📧 Looking up user with email: " + email);
        
        try {
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> {
                        System.out.println("❌ User not found for email: " + email);
                        return new RuntimeException("User not found with email: " + email);
                    });
            
            System.out.println("✅ User found - ID: " + user.getId() + ", Name: " + user.getName());
            return user.getId();
            
        } catch (Exception e) {
            System.out.println("❌ ERROR in getUserIdByEmail: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ============================================
    // BOOKING MANAGEMENT
    // ============================================

    /**
     * Book a parking slot
     * Validates time range, checks for conflicts, creates booking with ACTIVE status
     * Only allows booking with registered vehicles
     * Uses pessimistic locking to prevent race conditions during concurrent booking attempts
     *
     * @param booking Booking details (userId, slotId, entryTime, vehicleType)
     */
    public void bookSlot(Booking booking) {
        // ============================================
        // STEP 1: ACQUIRE PESSIMISTIC LOCK ON SLOT
        // ============================================
        // This lock ensures only ONE transaction can proceed with booking this slot
        // Other concurrent requests will wait until this transaction completes
        Slot slot = slotRepo.findByIdWithLock(booking.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        
        // ============================================
        // STEP 2: VALIDATE INPUT
        // ============================================
        if (booking.getEntryTime() == null) {
            throw new RuntimeException("Entry time is required");
        }

        // ============================================
        // STEP 3: ENFORCE VEHICLE TYPE VALIDATION
        // ============================================
        // Enforce vehicle type to be a registered vehicle for this user
        if (booking.getUserId() != null) {
            User user = userRepo.findById(booking.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            java.util.List<Vehicle> registeredVehicles = vehicleRepo.findByUserId(user.getId());

            if (!registeredVehicles.isEmpty()) {
                // Verify that requested vehicle type matches one of user's registered vehicles
                com.smartparking.model.VehicleType requestedType = booking.getVehicleType();
                
                // Check if the vehicle type matches any registered vehicle
                boolean hasVehicleOfType = registeredVehicles.stream()
                        .anyMatch(v -> v.getVehicleType() == requestedType);

                if (!hasVehicleOfType) {
                    // If no exact match, use the default vehicle
                    Vehicle defaultVehicle = registeredVehicles.stream()
                            .filter(v -> Boolean.TRUE.equals(v.getIsDefault()))
                            .findFirst()
                            .orElse(registeredVehicles.get(0));
                    
                    booking.setVehicleType(defaultVehicle.getVehicleType());
                }
                // If exact match found, keep the requested type
            } else {
                // No registered vehicles: use user profile vehicleType if present, else default to CAR
                if (user.getVehicleType() != null && !user.getVehicleType().isEmpty()) {
                    booking.setVehicleType(com.smartparking.model.VehicleType.fromString(user.getVehicleType()));
                } else if (booking.getVehicleType() != null) {
                    booking.setVehicleType(booking.getVehicleType());
                } else {
                    booking.setVehicleType(com.smartparking.model.VehicleType.CAR);
                }
            }
        } else if (booking.getVehicleType() == null) {
            // No user context provided; default to CAR
            booking.setVehicleType(com.smartparking.model.VehicleType.CAR);
        }

        // ============================================
        // STEP 4: VALIDATE TIME RANGE
        // ============================================
        // For backward compatibility, if exitTime is provided at booking time, validate it
        if (booking.getExitTime() != null && booking.getEntryTime().isAfter(booking.getExitTime())) {
            throw new RuntimeException("Invalid time range: entry time must be before exit time");
        }

        // ============================================
        // STEP 5: CHECK FOR TIME CONFLICTS
        // ============================================
        // Check for time conflicts with existing bookings
        boolean conflict = bookingRepo.hasTimeConflict(
                booking.getSlotId(),
                booking.getEntryTime(),
                booking.getExitTime() != null ? booking.getExitTime() : booking.getEntryTime().plusHours(2)
        );

        if (conflict) {
            throw new RuntimeException("Slot already booked for this time period");
        }

        // ============================================
        // STEP 6: VALIDATE SLOT AVAILABILITY
        // ============================================
        // Check if slot is currently available
        if (!slot.isAvailable()) {
            throw new RuntimeException("Slot is currently unavailable");
        }
        
        // Check if slot is disabled for maintenance
        if (slot.isDisabled()) {
            throw new RuntimeException("Slot is under maintenance and cannot be booked");
        }

        // ============================================
        // STEP 7: VALIDATE VEHICLE TYPE MATCHES SLOT TYPE
        // ============================================
        String slotType = slot.getSlotType();  // e.g., "CAR", "BIKE", "TRUCK", "EV"
        String bookingVehicleType = booking.getVehicleType() != null ? booking.getVehicleType().name() : "CAR";
        
        // If slot has no type assigned (null/empty), default to CAR for backward compatibility
        if (slotType == null || slotType.trim().isEmpty()) {
            slotType = "CAR";
            slot.setSlotType("CAR");  // Update slot with default type
        }
        
        // Check if vehicle type matches slot type
        if (!slotType.equalsIgnoreCase(bookingVehicleType)) {
            throw new RuntimeException("❌ Vehicle type mismatch: This slot is designated for " + slotType + " vehicles only, but you are trying to book it with a " + bookingVehicleType + " vehicle.");
        }

        // ============================================
        // STEP 8: CREATE BOOKING AND MARK SLOT UNAVAILABLE
        // ============================================
        // Set booking status to ACTIVE and persist
        booking.setStatus(ParkingStatus.ACTIVE);
        bookingRepo.save(booking);

        // Mark slot as unavailable (within the same transaction)
        slot.setAvailable(false);
        slotRepo.save(slot);
        
        // Transaction commits here - lock is released
        // If any exception occurs above, transaction rolls back automatically
    }

    /**
 * Release or checkout a parking slot
 * Records exit time, calculates fee, processes payment, and updates booking status
 *
 * @param bookingId Booking ID to checkout
 * @return CheckoutResponse with fee, duration, and payment details
 */
public CheckoutResponse checkoutBooking(Long bookingId) {
    Booking booking = bookingRepo.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

    // Verify booking is still active
    if (booking.getStatus() != ParkingStatus.ACTIVE) {
        throw new RuntimeException("Booking is not active. Current status: " + booking.getStatus());
    }

    // Record exit time (current time)
    LocalDateTime exitTime = LocalDateTime.now();
    booking.setExitTime(exitTime);

    // Calculate parking duration and fee
    long durationMinutes = feeCalculationService.calculateDurationMinutes(
            booking.getEntryTime(),
            exitTime
    );

    // Get location ID from slot for location-based pricing
    Long locationId = null;
    try {
        Slot slot = slotRepo.findById(booking.getSlotId()).orElse(null);
        if (slot != null && slot.getLocation() != null) {
            locationId = slot.getLocation().getId();
        }
    } catch (Exception e) {
        // If we can't get location, will fall back to default pricing
        System.err.println("Could not retrieve location for slot: " + e.getMessage());
    }

    // Calculate fee based on vehicle type and location
    double parkingFee;
    if (booking.getVehicleType() != null) {
        parkingFee = feeCalculationService.calculateFee(
                booking.getEntryTime(),
                exitTime,
                booking.getVehicleType(),
                locationId  // Use location-based pricing
        );
    } else {
        // Fallback to default rate if vehicle type not set
        parkingFee = feeCalculationService.calculateFee(
                booking.getEntryTime(),
                exitTime
        );
    }

    // Do NOT auto-process payment here. Build a pending payment response instead.
    // Get vehicle type and rate for response
    String vehicleTypeName = booking.getVehicleType() != null ? 
                             booking.getVehicleType().name() : "CAR";
    double appliedRate = booking.getVehicleType() != null ? 
                         feeCalculationService.getRatePerHour(locationId, booking.getVehicleType()) :
                         feeCalculationService.getRatePerHour();

    // Mark booking as pending payment and save fee
    booking.setParkingFee(parkingFee);
    booking.setPaymentStatus("PENDING_PAYMENT");
    bookingRepo.save(booking);

    // Do NOT release the slot here; wait for payment confirmation via /api/payments/process

    // Return pending payment response so frontend can show payment modal
    return new CheckoutResponse(
            bookingId,
            booking.getSlotId(),
            vehicleTypeName,
            booking.getEntryTime(),
            booking.getExitTime(),
            durationMinutes,
            appliedRate,
            parkingFee,
            false,
            null,
            "Pending payment"
    );
}
    /**
     * Cancel a booking and release the slot
     *
     * @param bookingId Booking ID to cancel
     */
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == ParkingStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed booking");
        }

        // Mark as cancelled and close the booking
        booking.setStatus(ParkingStatus.CANCELLED);
        if (booking.getExitTime() == null) {
            booking.setExitTime(java.time.LocalDateTime.now());
        }
        bookingRepo.save(booking);

        // Release the slot
        Slot slot = slotRepo.findById(booking.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        slot.setAvailable(true);
        slotRepo.save(slot);
    }

    // ============================================
    // BOOKING RETRIEVAL
    // ============================================

    /**
     * Get all bookings for a user
     */
    public List<BookingResponse> getUserBookings(Long userId) {
        System.out.println("📋 BookingService.getUserBookings called");
        System.out.println("👤 Fetching bookings for userId: " + userId);
        
        try {
            List<Booking> bookings = bookingRepo.findByUserId(userId);
            System.out.println("✅ Found " + bookings.size() + " booking(s) in database");
            
            if (bookings.isEmpty()) {
                System.out.println("ℹ️  No bookings found for user " + userId);
                return new java.util.ArrayList<>();
            }
            
            List<BookingResponse> responses = bookings.stream()
                .map(booking -> {
                    try {
                        System.out.println("  → Converting booking ID: " + booking.getId());
                        Slot slot = slotRepo.findById(booking.getSlotId()).orElse(null);
                        
                        if (slot == null) {
                            System.out.println("  ⚠️  Slot not found for slotId: " + booking.getSlotId());
                        } else {
                            System.out.println("  ✅ Slot found: " + slot.getSlotNumber());
                        }
                        
                        String vehicleTypeName = booking.getVehicleType() != null ? 
                                                 booking.getVehicleType().name() : "CAR";
                        System.out.println("  → Vehicle type: " + vehicleTypeName);
                        
                        // Handle null status - default to ACTIVE
                        String statusString = booking.getStatus() != null ? 
                                              booking.getStatus().toString() : "ACTIVE";
                        System.out.println("  → Status: " + statusString);

                        // Calculate duration in minutes
                        long durationMinutes = 0;
                        if (booking.getEntryTime() != null && booking.getExitTime() != null) {
                            durationMinutes = ChronoUnit.MINUTES.between(
                                booking.getEntryTime(), 
                                booking.getExitTime()
                            );
                        }
                        System.out.println("  → Duration: " + durationMinutes + " minutes");

                        // Get location name from slot
                        String locationName = "Unknown";
                        if (slot != null && slot.getLocation() != null) {
                            locationName = slot.getLocation().getName();
                        }
                        System.out.println("  → Location: " + locationName);

                        BookingResponse response = new BookingResponse(
                                booking.getId(),
                                booking.getSlotId(),
                                slot != null ? slot.getSlotNumber() : "N/A",
                                vehicleTypeName,
                                booking.getEntryTime(),
                                booking.getExitTime(),
                                durationMinutes,
                                locationName,
                                statusString,
                                booking.getParkingFee(),
                                booking.getTransactionId()
                        );
                        
                        System.out.println("  ✅ BookingResponse created successfully");
                        return response;
                        
                    } catch (Exception e) {
                        System.out.println("  ❌ ERROR converting booking " + booking.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("Failed to convert booking " + booking.getId(), e);
                    }
                })
                .collect(Collectors.toList());
                
            System.out.println("✅ Successfully converted " + responses.size() + " booking(s) to responses");
            return responses;
            
        } catch (Exception e) {
            System.out.println("❌ ERROR in getUserBookings: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get a specific booking by ID
     */
    public Booking getBookingById(Long bookingId) {
        return bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    // ============================================
    // ADMIN OVERVIEW & STATISTICS
    // ============================================

    /**
     * Get all bookings in the system (Admin view)
     */
    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    /**
     * Get bookings filtered by status
     */
    public List<Booking> getBookingsByStatus(ParkingStatus status) {
        return bookingRepo.findByStatus(status);
    }

    /**
     * Get bookings for a specific user
     */
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepo.findByUserId(userId);
    }

    /**
     * Get bookings for a specific slot
     */
    public List<Booking> getBookingsBySlot(Long slotId) {
        return bookingRepo.findBySlotId(slotId);
    }

    /**
     * Get active bookings for a specific location
     */
    public List<Booking> getActiveBookingsByLocation(Long locationId) {
        return bookingRepo.findActiveBookingsByLocation(locationId);
    }

    /**
     * Search bookings by date range
     */
    public List<Booking> searchBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return bookingRepo.findAll();
        }
        return bookingRepo.findBookingsByDateRange(startDate, endDate);
    }

    /**
     * Get comprehensive booking statistics
     * Returns dashboard statistics including totals, active, completed, cancelled counts and revenue
     */
    public Map<String, Object> getBookingStatistics() {
        long totalBookings = bookingRepo.count();
        long activeBookings = bookingRepo.countActiveBookings();
        long completedBookings = bookingRepo.countCompletedBookings();
        long cancelledBookings = bookingRepo.countCancelledBookings();
        double totalRevenue = bookingRepo.getTotalRevenue();

        return Map.of(
                "totalBookings", totalBookings,
                "activeBookings", activeBookings,
                "completedBookings", completedBookings,
                "cancelledBookings", cancelledBookings,
                "totalRevenue", totalRevenue,
                "averageFee", completedBookings > 0 ? totalRevenue / completedBookings : 0,
                "occupancyRate", totalBookings > 0 ? Math.round((double) activeBookings / totalBookings * 100) : 0
        );
    }

    /**
     * Get revenue summary
     */
    public Map<String, Object> getRevenueSummary() {
        double totalRevenue = bookingRepo.getTotalRevenue();
        long completedBookings = bookingRepo.countCompletedBookings();
        double averageFee = completedBookings > 0 ? totalRevenue / completedBookings : 0;

        return Map.of(
                "totalRevenue", totalRevenue,
                "completedBookings", completedBookings,
                "averageFee", averageFee,
                "currency", "USD"
        );
    }

    /**
     * Convert Booking entity to BookingResponse DTO
     */
    public BookingResponse convertToResponse(Booking booking) {
        Slot slot = slotRepo.findById(booking.getSlotId()).orElse(null);
        String slotNumber = slot != null ? slot.getSlotNumber() : "N/A";
        String vehicleType = booking.getVehicleType() != null ? booking.getVehicleType().name() : "CAR";
        String status = booking.getStatus() != null ? booking.getStatus().toString() : "ACTIVE";

        BookingResponse response = new BookingResponse(
                booking.getId(),
                booking.getSlotId(),
                slotNumber,
                vehicleType,
                booking.getEntryTime(),
                booking.getExitTime(),
                status,
                booking.getParkingFee(),
                booking.getTransactionId()
        );
        
        // Include user information
        if (booking.getUserId() != null) {
            User user = userRepo.findById(booking.getUserId()).orElse(null);
            if (user != null) {
                response.setUser(new BookingResponse.UserInfo(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getEmail()
                ));
            }
        }
        
        return response;
    }

    /**
     * Convert list of Booking entities to BookingResponse DTOs
     */
    public List<BookingResponse> convertToResponseList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // DATA FIX METHODS
    // ============================================

    /**
     * Fix corrupted booking where status=ACTIVE but exitTime is set
     * This should only happen due to data corruption or bugs
     */
    public Booking fixCorruptedBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Only fix if booking is ACTIVE but has exitTime set
        if (booking.getStatus() == ParkingStatus.ACTIVE && booking.getExitTime() != null) {
            System.out.println("🔧 Fixing corrupted booking ID: " + bookingId);
            System.out.println("   Status: " + booking.getStatus() + ", ExitTime: " + booking.getExitTime());
            
            booking.setExitTime(null);
            Booking savedBooking = bookingRepo.save(booking);
            
            System.out.println("   ✅ Fixed: ExitTime now = " + savedBooking.getExitTime());
            return savedBooking;
        }
        
        return booking;
    }

    /**
     * Get all active bookings from the database
     */
    public List<Booking> getActiveBookings() {
        return bookingRepo.findByStatus(ParkingStatus.ACTIVE);
    }
}

