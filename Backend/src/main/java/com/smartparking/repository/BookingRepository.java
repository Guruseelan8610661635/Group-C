package com.smartparking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartparking.model.Booking;
import com.smartparking.model.Booking.ParkingStatus;

/**
 * Booking Repository
 * Handles database queries for parking bookings with timer tracking.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Check if a time slot has conflicts with existing bookings
     */
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.slotId = :slotId
        AND b.status = 'ACTIVE'
        AND (b.entryTime < :exitTime AND (b.exitTime IS NULL OR b.exitTime > :entryTime))
    """)
    boolean hasTimeConflict(
        @Param("slotId") Long slotId,
        @Param("entryTime") LocalDateTime entryTime,
        @Param("exitTime") LocalDateTime exitTime
    );

    /**
     * Find all bookings for a specific user
     */
    List<Booking> findByUserId(Long userId);

    /**
     * Find active bookings for a user
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status = 'ACTIVE'")
    List<Booking> findActiveBookingsByUserId(@Param("userId") Long userId);

    /**
     * Find completed bookings for a user (for history)
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status = 'COMPLETED'")
    List<Booking> findCompletedBookingsByUserId(@Param("userId") Long userId);

    /**
     * Find all bookings by status
     */
    List<Booking> findByStatus(ParkingStatus status);

    /**
     * Find all bookings for a user by status
     */
    List<Booking> findByUserIdAndStatus(Long userId, ParkingStatus status);

    /**
     * Find all bookings for a specific slot
     */
    List<Booking> findBySlotId(Long slotId);

    /**
     * Find all active bookings for a location (by querying slots)
     */
    @Query("""
        SELECT b FROM Booking b
        JOIN Slot s ON b.slotId = s.id
        WHERE s.location.id = :locationId AND b.status = 'ACTIVE'
    """)
    List<Booking> findActiveBookingsByLocation(@Param("locationId") Long locationId);

    /**
     * Find bookings within a date range
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.entryTime >= :startDate AND b.entryTime <= :endDate
    """)
    List<Booking> findBookingsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find total revenue from completed bookings
     */
    @Query("""
        SELECT COALESCE(SUM(b.parkingFee), 0) FROM Booking b
        WHERE b.status = 'COMPLETED' AND b.parkingFee IS NOT NULL
    """)
    Double getTotalRevenue();

    /**
     * Count active bookings
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'ACTIVE'")
    long countActiveBookings();

    /**
     * Count completed bookings
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'COMPLETED'")
    long countCompletedBookings();

    /**
     * Count cancelled bookings
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED'")
    long countCancelledBookings();

    /**
     * Find active bookings (status is ACTIVE, ignore exitTime to handle data corruption)
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status = 'ACTIVE' ORDER BY b.entryTime DESC")
    List<Booking> findCurrentBookingsByUserId(@Param("userId") Long userId);

    /**
     * Find past bookings (exit time set AND not active)
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.exitTime IS NOT NULL AND b.status != 'ACTIVE' ORDER BY b.exitTime DESC")
    List<Booking> findPastBookingsByUserId(@Param("userId") Long userId);

    /**
     * Find bookings with pagination
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId ORDER BY b.entryTime DESC")
    List<Booking> findBookingsByUserIdPaginated(
        @Param("userId") Long userId,
        org.springframework.data.domain.Pageable pageable
    );

    /**
     * Quick count for performance
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId AND b.exitTime IS NULL AND b.status = 'ACTIVE'")
    long countCurrentBookingsByUserId(@Param("userId") Long userId);

    /**
     * Quick count for performance
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId AND b.exitTime IS NOT NULL")
    long countPastBookingsByUserId(@Param("userId") Long userId);
}
