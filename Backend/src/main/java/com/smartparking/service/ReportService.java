package com.smartparking.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartparking.dto.*;
import com.smartparking.dto.UsageReportResponse.PeakHourInfo;
import com.smartparking.model.Booking;
import com.smartparking.model.Location;
import com.smartparking.model.Slot;
import com.smartparking.model.User;
import com.smartparking.repository.BookingRepository;
import com.smartparking.repository.LocationRepository;
import com.smartparking.repository.SlotRepository;
import com.smartparking.repository.UserRepository;

@Service
public class ReportService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SlotRepository slotRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private UserRepository userRepository;

    public ReportResponse generateReport(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        List<Booking> bookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        
        long totalBookings = bookings.size();
        long completedBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.ParkingStatus.COMPLETED).count();
        long activeBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.ParkingStatus.ACTIVE).count();
        long cancelledBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.ParkingStatus.CANCELLED).count();
        
        double totalRevenue = bookings.stream()
                .filter(b -> b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
                .mapToDouble(Booking::getParkingFee)
                .sum();
        
        double averageFee = completedBookings > 0 ? totalRevenue / completedBookings : 0;
        
        long totalSlots = slotRepository.count();
        long availableSlots = slotRepository.findAll().stream()
                .filter(slot -> slot.isAvailable() && !slot.isDisabled())
                .count();
        long disabledSlots = slotRepository.findAll().stream()
                .filter(slot -> slot.isDisabled())
                .count();
        long occupiedSlots = totalSlots - availableSlots - disabledSlots;
        
        int occupancyRate = totalSlots > 0 ? Math.toIntExact(occupiedSlots * 100 / totalSlots) : 0;

        ReportResponse report = new ReportResponse();
        report.setReportType(reportType);
        report.setGeneratedDate(LocalDateTime.now());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalBookings(totalBookings);
        report.setCompletedBookings(completedBookings);
        report.setActiveBookings(activeBookings);
        report.setCancelledBookings(cancelledBookings);
        report.setTotalRevenue(totalRevenue);
        report.setAverageFee(averageFee);
        report.setOccupancyRate(occupancyRate);
        report.setTotalSlots(totalSlots);
        report.setAvailableSlots(availableSlots);
        
        return report;
    }

    public ReportResponse generateDailyReport() {
        LocalDateTime startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endDate = LocalDateTime.now();
        return generateReport(startDate, endDate, "DAILY");
    }

    public ReportResponse generateWeeklyReport() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        return generateReport(startDate, endDate, "WEEKLY");
    }

    public ReportResponse generateMonthlyReport() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);
        return generateReport(startDate, endDate, "MONTHLY");
    }

    /**
     * Generate comprehensive usage report with peak hours, average duration, and segmentation
     */
    public UsageReportResponse generateUsageReport(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        List<Booking> bookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        
        UsageReportResponse report = new UsageReportResponse();
        
        // Basic report info
        report.setReportType(reportType);
        report.setGeneratedDate(LocalDateTime.now());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        
        // Calculate basic metrics
        calculateBasicMetrics(bookings, report);
        
        // Calculate duration metrics
        calculateDurationMetrics(bookings, report);
        
        // Calculate slot utilization
        calculateSlotUtilization(report);
        
        // Calculate peak hours
        calculatePeakHours(bookings, report);
        
        // Calculate daily/weekly breakdown
        calculateTemporalBreakdown(bookings, report, reportType);
        
        // Calculate vehicle type breakdown
        calculateVehicleTypeBreakdown(bookings, report);
        
        // Calculate status distribution
        calculateStatusDistribution(bookings, report);
        
        return report;
    }
    
    /**
     * Calculate basic booking and revenue metrics
     */
    private void calculateBasicMetrics(List<Booking> bookings, UsageReportResponse report) {
        long totalBookings = bookings.size();
        long completedBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.ParkingStatus.COMPLETED)
                .count();
        long activeBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.ParkingStatus.ACTIVE)
                .count();
        long cancelledBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.ParkingStatus.CANCELLED)
                .count();
        
        List<Double> fees = bookings.stream()
                .filter(b -> b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
                .map(Booking::getParkingFee)
                .collect(Collectors.toList());
        
        double totalRevenue = fees.stream().mapToDouble(Double::doubleValue).sum();
        double averageFee = fees.isEmpty() ? 0 : totalRevenue / fees.size();
        double maxFee = fees.isEmpty() ? 0 : fees.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double minFee = fees.isEmpty() ? 0 : fees.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        
        report.setTotalBookings(totalBookings);
        report.setCompletedBookings(completedBookings);
        report.setActiveBookings(activeBookings);
        report.setCancelledBookings(cancelledBookings);
        report.setTotalRevenue(totalRevenue);
        report.setAverageFee(averageFee);
        report.setMaxFee(maxFee);
        report.setMinFee(minFee);
    }
    
    /**
     * Calculate parking duration metrics
     */
    private void calculateDurationMetrics(List<Booking> bookings, UsageReportResponse report) {
        List<Double> durations = bookings.stream()
                .filter(b -> b.getEntryTime() != null && b.getExitTime() != null)
                .map(b -> {
                    Duration duration = Duration.between(b.getEntryTime(), b.getExitTime());
                    return (double) duration.toMinutes();
                })
                .filter(d -> d >= 0)
                .collect(Collectors.toList());
        
        if (!durations.isEmpty()) {
            double avgDuration = durations.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double maxDuration = durations.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double minDuration = durations.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            
            // Calculate median
            Collections.sort(durations);
            double medianDuration = durations.size() % 2 == 0 
                ? (durations.get(durations.size() / 2 - 1) + durations.get(durations.size() / 2)) / 2
                : durations.get(durations.size() / 2);
            
            report.setAverageDurationMinutes(avgDuration);
            report.setMaxDurationMinutes(maxDuration);
            report.setMinDurationMinutes(minDuration);
            report.setMedianDurationMinutes(medianDuration);
        } else {
            report.setAverageDurationMinutes(0.0);
            report.setMaxDurationMinutes(0.0);
            report.setMinDurationMinutes(0.0);
            report.setMedianDurationMinutes(0.0);
        }
    }
    
    /**
     * Calculate slot utilization and occupancy
     */
    private void calculateSlotUtilization(UsageReportResponse report) {
        long totalSlots = slotRepository.count();
        long availableSlots = slotRepository.findAll().stream()
                .filter(slot -> slot.isAvailable() && !slot.isDisabled())
                .count();
        long disabledSlots = slotRepository.findAll().stream()
                .filter(slot -> slot.isDisabled())
                .count();
        long occupiedSlots = totalSlots - availableSlots - disabledSlots;
        
        int occupancyRate = totalSlots > 0 ? Math.toIntExact(occupiedSlots * 100 / totalSlots) : 0;
        
        report.setTotalSlots(totalSlots);
        report.setAvailableSlots(availableSlots);
        report.setOccupancyRate(occupancyRate);
    }
    
    /**
     * Calculate peak hours based on entry times
     */
    private void calculatePeakHours(List<Booking> bookings, UsageReportResponse report) {
        Map<Integer, Long> hourlyBookings = bookings.stream()
                .filter(b -> b.getEntryTime() != null)
                .collect(Collectors.groupingBy(
                    b -> b.getEntryTime().getHour(),
                    Collectors.counting()
                ));
        
        report.setPeakHoursData(hourlyBookings);
        
        // Get top 5 peak hours
        List<PeakHourInfo> topPeakHours = hourlyBookings.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new PeakHourInfo(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        
        report.setTopPeakHours(topPeakHours);
    }
    
    /**
     * Calculate temporal breakdown (daily/weekly)
     */
    private void calculateTemporalBreakdown(List<Booking> bookings, UsageReportResponse report, String reportType) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Daily booking breakdown
        Map<String, Long> dailyBreakdown = bookings.stream()
                .filter(b -> b.getEntryTime() != null)
                .collect(Collectors.groupingBy(
                    b -> b.getEntryTime().format(dateFormatter),
                    Collectors.counting()
                ));
        
        // Daily revenue breakdown
        Map<String, Double> dailyRevenueBreakdown = bookings.stream()
                .filter(b -> b.getEntryTime() != null && b.getParkingFee() != null 
                        && b.getStatus() == Booking.ParkingStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                    b -> b.getEntryTime().format(dateFormatter),
                    Collectors.summingDouble(Booking::getParkingFee)
                ));
        
        report.setDailyBreakdown(dailyBreakdown);
        report.setDailyRevenueBreakdown(dailyRevenueBreakdown);
    }
    
    /**
     * Calculate vehicle type breakdown
     */
    private void calculateVehicleTypeBreakdown(List<Booking> bookings, UsageReportResponse report) {
        Map<String, Long> vehicleTypeBreakdown = bookings.stream()
                .filter(b -> b.getVehicleType() != null)
                .collect(Collectors.groupingBy(
                    b -> b.getVehicleType().toString(),
                    Collectors.counting()
                ));
        
        report.setVehicleTypeBreakdown(vehicleTypeBreakdown);
    }
    
    /**
     * Calculate booking status distribution
     */
    private void calculateStatusDistribution(List<Booking> bookings, UsageReportResponse report) {
        Map<String, Long> statusDistribution = bookings.stream()
                .collect(Collectors.groupingBy(
                    b -> b.getStatus().toString(),
                    Collectors.counting()
                ));
        
        report.setStatusDistribution(statusDistribution);
    }

    /**
     * Generate daily usage report
     */
    public UsageReportResponse generateDailyUsageReport() {
        LocalDateTime startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endDate = LocalDateTime.now();
        return generateUsageReport(startDate, endDate, "DAILY");
    }

    /**
     * Generate weekly usage report
     */
    public UsageReportResponse generateWeeklyUsageReport() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        return generateUsageReport(startDate, endDate, "WEEKLY");
    }

    /**
     * Generate monthly usage report
     */
    public UsageReportResponse generateMonthlyUsageReport() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);
        return generateUsageReport(startDate, endDate, "MONTHLY");
    }

    /**
     * Generate CSV export for bookings with optional filters
     */
    public String generateBookingsCsv(LocalDateTime startDate, LocalDateTime endDate, Long slotId, Long userId) {
        List<Booking> bookings;

        // Prefer date range if both provided
        if (startDate != null && endDate != null) {
            bookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        } else if (slotId != null && userId == null) {
            bookings = bookingRepository.findBySlotId(slotId);
        } else if (userId != null && slotId == null) {
            bookings = bookingRepository.findByUserId(userId);
        } else {
            bookings = bookingRepository.findAll();
        }

        // Apply secondary in-memory filters when combined
        if (slotId != null) {
            bookings = bookings.stream().filter(b -> Objects.equals(b.getSlotId(), slotId)).collect(Collectors.toList());
        }
        if (userId != null) {
            bookings = bookings.stream().filter(b -> Objects.equals(b.getUserId(), userId)).collect(Collectors.toList());
        }

        // Sort by entry time ascending for readability
        bookings.sort(Comparator.comparing(Booking::getEntryTime, Comparator.nullsLast(Comparator.naturalOrder())));

        StringBuilder sb = new StringBuilder();
        sb.append("id,userId,slotId,vehicleType,entryTime,exitTime,status,parkingFee,transactionId,paymentStatus,paymentTime,durationMinutes\n");

        for (Booking b : bookings) {
            long durationMinutes = 0;
            if (b.getEntryTime() != null && b.getExitTime() != null) {
                durationMinutes = java.time.Duration.between(b.getEntryTime(), b.getExitTime()).toMinutes();
            }

            sb.append(safe(b.getId()))
              .append(',').append(safe(b.getUserId()))
              .append(',').append(safe(b.getSlotId()))
              .append(',').append(escapeCsv(b.getVehicleType() != null ? b.getVehicleType().toString() : null))
              .append(',').append(escapeCsv(formatDateTime(b.getEntryTime())))
              .append(',').append(escapeCsv(formatDateTime(b.getExitTime())))
              .append(',').append(escapeCsv(b.getStatus() != null ? b.getStatus().toString() : null))
              .append(',').append(safe(b.getParkingFee()))
              .append(',').append(escapeCsv(b.getTransactionId()))
              .append(',').append(escapeCsv(b.getPaymentStatus()))
              .append(',').append(escapeCsv(formatDateTime(b.getPaymentTime())))
              .append(',').append(durationMinutes)
              .append('\n');
        }

        return sb.toString();
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt == null ? null : dt.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\r", " ").replace("\n", " ");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }

    private String safe(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }
    
    // ==================== ADVANCED ANALYTICS METHODS ====================
    
    /**
     * Get location performance comparison
     */
    public List<LocationPerformanceDTO> getLocationComparison(LocalDateTime startDate, LocalDateTime endDate) {
        List<Location> locations = locationRepository.findAll();
        List<Booking> allBookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        
        return locations.stream().map(location -> {
            List<Slot> locationSlots = slotRepository.findByLocationId(location.getId());
            List<Booking> locationBookings = allBookings.stream()
                .filter(b -> locationSlots.stream().anyMatch(s -> s.getId().equals(b.getSlotId())))
                .collect(Collectors.toList());
            
            // Calculate metrics
            Double totalRevenue = locationBookings.stream()
                .filter(b -> b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
                .mapToDouble(Booking::getParkingFee)
                .sum();
            
            Integer totalBookings = locationBookings.size();
            
            Double avgDuration = locationBookings.stream()
                .filter(b -> b.getEntryTime() != null && b.getExitTime() != null)
                .mapToDouble(b -> Duration.between(b.getEntryTime(), b.getExitTime()).toMinutes())
                .average()
                .orElse(0.0);
            
            Double avgRevenue = totalBookings > 0 ? totalRevenue / totalBookings : 0.0;
            
            long availableSlots = locationSlots.stream()
                .filter(s -> s.isAvailable() && !s.isDisabled())
                .count();
            
            long occupiedSlots = locationSlots.stream()
                .filter(s -> !s.isAvailable() && !s.isDisabled())
                .count();
            
            Integer totalSlots = locationSlots.size();
            Double occupancyRate = totalSlots > 0 ? (occupiedSlots * 100.0 / totalSlots) : 0.0;
            Double utilizationRate = totalSlots > 0 ? (totalBookings * 100.0 / (totalSlots * 24)) : 0.0; // bookings per slot per day
            
            return new LocationPerformanceDTO(
                location.getId(),
                location.getName(),
                totalRevenue,
                totalBookings,
                occupancyRate,
                avgDuration,
                avgRevenue,
                utilizationRate,
                (int) availableSlots,
                totalSlots
            );
        }).collect(Collectors.toList());
    }
    
    /**
     * Get slot utilization analytics
     */
    public List<SlotUtilizationDTO> getSlotUtilization(LocalDateTime startDate, LocalDateTime endDate) {
        List<Slot> slots = slotRepository.findAllWithLocation();
        List<Booking> allBookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        
        long periodHours = Duration.between(startDate, endDate).toHours();
        
        return slots.stream().map(slot -> {
            List<Booking> slotBookings = allBookings.stream()
                .filter(b -> b.getSlotId().equals(slot.getId()))
                .collect(Collectors.toList());
            
            Integer totalBookings = slotBookings.size();
            
            Double totalBookedHours = slotBookings.stream()
                .filter(b -> b.getEntryTime() != null && b.getExitTime() != null)
                .mapToDouble(b -> Duration.between(b.getEntryTime(), b.getExitTime()).toHours())
                .sum();
            
            Double idleTimeHours = periodHours - totalBookedHours;
            Double utilizationPercentage = periodHours > 0 ? (totalBookedHours * 100.0 / periodHours) : 0.0;
            Double turnoverRate = periodHours > 0 ? (totalBookings * 24.0 / periodHours) : 0.0; // bookings per day
            
            LocalDateTime lastBookedDate = slotBookings.stream()
                .map(Booking::getEntryTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            Double revenueGenerated = slotBookings.stream()
                .filter(b -> b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
                .mapToDouble(Booking::getParkingFee)
                .sum();
            
            return new SlotUtilizationDTO(
                slot.getId(),
                slot.getSlotNumber(),
                slot.getLocation().getName(),
                totalBookings,
                Math.max(0, idleTimeHours),
                turnoverRate,
                lastBookedDate,
                utilizationPercentage,
                revenueGenerated
            );
        }).collect(Collectors.toList());
    }
    
    /**
     * Get top utilized slots
     */
    public List<SlotUtilizationDTO> getTopUtilizedSlots(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return getSlotUtilization(startDate, endDate).stream()
            .sorted(Comparator.comparing(SlotUtilizationDTO::getUtilizationPercentage).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get least utilized slots
     */
    public List<SlotUtilizationDTO> getLeastUtilizedSlots(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return getSlotUtilization(startDate, endDate).stream()
            .sorted(Comparator.comparing(SlotUtilizationDTO::getUtilizationPercentage))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get comprehensive revenue analytics
     */
    public RevenueAnalyticsDTO getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        
        Double totalRevenue = bookings.stream()
            .filter(b -> b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
            .mapToDouble(Booking::getParkingFee)
            .sum();
        
        long daysDiff = Duration.between(startDate, endDate).toDays();
        daysDiff = daysDiff == 0 ? 1 : daysDiff;
        
        Double dailyAvgRevenue = totalRevenue / daysDiff;
        Double weeklyAvgRevenue = totalRevenue / (daysDiff / 7.0);
        Double monthlyAvgRevenue = totalRevenue / (daysDiff / 30.0);
        
        // Revenue by location
        Map<String, Double> revenueByLocation = new HashMap<>();
        List<Location> locations = locationRepository.findAll();
        locations.forEach(loc -> {
            List<Slot> locationSlots = slotRepository.findByLocationId(loc.getId());
            Double locRevenue = bookings.stream()
                .filter(b -> locationSlots.stream().anyMatch(s -> s.getId().equals(b.getSlotId())))
                .filter(b -> b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
                .mapToDouble(Booking::getParkingFee)
                .sum();
            revenueByLocation.put(loc.getName(), locRevenue);
        });
        
        // Revenue by hour
        Map<Integer, Double> revenueByHour = bookings.stream()
            .filter(b -> b.getEntryTime() != null && b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
            .collect(Collectors.groupingBy(
                b -> b.getEntryTime().getHour(),
                Collectors.summingDouble(Booking::getParkingFee)
            ));
        
        // Peak hour revenue
        Map.Entry<Integer, Double> peakHour = revenueByHour.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        Integer peakHourValue = peakHour != null ? peakHour.getKey() : 0;
        Double peakHourRevenue = peakHour != null ? peakHour.getValue() : 0.0;
        
        // Simple growth calculation (compare first half vs second half)
        LocalDateTime midPoint = startDate.plus(Duration.between(startDate, endDate).dividedBy(2));
        Double firstHalfRevenue = bookings.stream()
            .filter(b -> b.getEntryTime() != null && b.getEntryTime().isBefore(midPoint))
            .filter(b -> b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
            .mapToDouble(Booking::getParkingFee)
            .sum();
        
        Double secondHalfRevenue = bookings.stream()
            .filter(b -> b.getEntryTime() != null && b.getEntryTime().isAfter(midPoint))
            .filter(b -> b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
            .mapToDouble(Booking::getParkingFee)
            .sum();
        
        Double revenueGrowth = firstHalfRevenue > 0 ? ((secondHalfRevenue - firstHalfRevenue) / firstHalfRevenue * 100) : 0.0;
        
        return new RevenueAnalyticsDTO(
            totalRevenue,
            dailyAvgRevenue,
            weeklyAvgRevenue,
            monthlyAvgRevenue,
            revenueByLocation,
            revenueByHour,
            revenueGrowth,
            peakHourRevenue,
            peakHourValue
        );
    }
    
    /**
     * Get user behavior analytics
     */
    public List<UserBehaviorDTO> getUserBehaviorAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        List<User> users = userRepository.findAll();
        
        return users.stream().map(user -> {
            List<Booking> userBookings = bookings.stream()
                .filter(b -> b.getUserId().equals(user.getId()))
                .collect(Collectors.toList());
            
            if (userBookings.isEmpty()) {
                return null; // Skip users with no bookings in this period
            }
            
            Integer totalBookings = userBookings.size();
            
            Double totalSpent = userBookings.stream()
                .filter(b -> b.getParkingFee() != null && b.getStatus() == Booking.ParkingStatus.COMPLETED)
                .mapToDouble(Booking::getParkingFee)
                .sum();
            
            Double avgDuration = userBookings.stream()
                .filter(b -> b.getEntryTime() != null && b.getExitTime() != null)
                .mapToDouble(b -> Duration.between(b.getEntryTime(), b.getExitTime()).toMinutes())
                .average()
                .orElse(0.0);
            
            LocalDateTime lastBookingDate = userBookings.stream()
                .map(Booking::getEntryTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            // User segmentation
            String userSegment;
            if (totalBookings == 1) {
                userSegment = "ONE_TIME";
            } else if (totalBookings >= 10 || totalSpent >= 1000) {
                userSegment = "VIP";
            } else {
                userSegment = "REGULAR";
            }
            
            // Avg booking frequency (bookings per month)
            long daysDiff = Duration.between(startDate, endDate).toDays();
            Double avgBookingFrequency = (totalBookings * 30.0) / Math.max(daysDiff, 1);
            
            return new UserBehaviorDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                totalBookings,
                totalSpent,
                avgDuration,
                lastBookingDate,
                userSegment,
                avgBookingFrequency
            );
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }
    
    /**
     * Get top users by total spent
     */
    public List<UserBehaviorDTO> getTopUsers(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return getUserBehaviorAnalytics(startDate, endDate).stream()
            .sorted(Comparator.comparing(UserBehaviorDTO::getTotalSpent).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get occupancy heatmap data (hour x day of week)
     */
    public List<OccupancyHeatmapDTO> getOccupancyHeatmap(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        List<OccupancyHeatmapDTO> heatmapData = new ArrayList<>();
        
        long totalSlots = slotRepository.count();
        
        // Create heatmap for each hour (0-23) and day of week (1-7)
        for (int hour = 0; hour < 24; hour++) {
            for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
                final int currentHour = hour;
                final int currentDay = dayOfWeek;
                
                List<Booking> filteredBookings = bookings.stream()
                    .filter(b -> b.getEntryTime() != null)
                    .filter(b -> b.getEntryTime().getHour() == currentHour)
                    .filter(b -> b.getEntryTime().getDayOfWeek().getValue() == currentDay)
                    .collect(Collectors.toList());
                
                Integer bookingCount = filteredBookings.size();
                
                Double avgDuration = filteredBookings.stream()
                    .filter(b -> b.getExitTime() != null)
                    .mapToDouble(b -> Duration.between(b.getEntryTime(), b.getExitTime()).toMinutes())
                    .average()
                    .orElse(0.0);
                
                Double occupancyPercentage = totalSlots > 0 ? (bookingCount * 100.0 / totalSlots) : 0.0;
                
                heatmapData.add(new OccupancyHeatmapDTO(
                    currentHour,
                    currentDay,
                    Math.min(100.0, occupancyPercentage),
                    bookingCount,
                    avgDuration
                ));
            }
        }
        
        return heatmapData;
    }
}
