package com.smartparking.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long slotId;

    // Vehicle Information
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;    // Type of vehicle (BIKE, CAR, SUV, TRUCK)

    // Parking Timer: Entry & Exit Times
    private LocalDateTime entryTime;    // When vehicle enters (starts timer)
    private LocalDateTime exitTime;    // When vehicle exits (ends timer)

    // Parking Status
    @Enumerated(EnumType.STRING)
    private ParkingStatus status = ParkingStatus.ACTIVE;  // ACTIVE, COMPLETED, CANCELLED

    // Payment Details
    private Double parkingFee;         // Calculated fee for the parking duration
    private String transactionId;      // Generated on successful payment
    private String paymentStatus;      // PENDING, PAID, FAILED
    private LocalDateTime paymentTime; // When payment was processed


    // Enum for parking status
    public enum ParkingStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    // Constructor
    public Booking() {}

    public Booking(Long userId, Long slotId, LocalDateTime entryTime) {
        this.userId = userId;
        this.slotId = slotId;
        this.entryTime = entryTime;
        this.status = ParkingStatus.ACTIVE;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public ParkingStatus getStatus() {
        return status;
    }

    public void setStatus(ParkingStatus status) {
        this.status = status;
    }

    public Double getParkingFee() {
        return parkingFee;
    }

    public void setParkingFee(Double parkingFee) {
        this.parkingFee = parkingFee;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    // Backward compatibility: map old startTime/endTime to entryTime/exitTime
    public LocalDateTime getStartTime() {
        return entryTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.entryTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return exitTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.exitTime = endTime;
    }
}
