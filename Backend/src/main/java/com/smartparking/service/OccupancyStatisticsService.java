package com.smartparking.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartparking.dto.OccupancyDataPoint;
import com.smartparking.dto.OccupancyStatisticsResponse;
import com.smartparking.model.Booking;
import com.smartparking.model.Location;
import com.smartparking.model.Slot;
import com.smartparking.repository.BookingRepository;
import com.smartparking.repository.SlotRepository;
import com.smartparking.repository.LocationRepository;

@Service
public class OccupancyStatisticsService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private LocationRepository locationRepository;

    public OccupancyStatisticsResponse getOccupancyStatistics(Long locationId, LocalDateTime startDate, LocalDateTime endDate, String timeGranularity) {
        long fetchStartTime = System.currentTimeMillis();

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        List<Slot> slots = slotRepository.findByLocationId(locationId);
        long totalSlots = slots.size();
        long currentlyOccupied = slots.stream().filter(s -> !s.isAvailable() && !s.isDisabled()).count();
        long disabledSlots = slots.stream().filter(Slot::isDisabled).count();
        long currentlyAvailable = totalSlots - currentlyOccupied - disabledSlots;

        List<Booking> bookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        List<Booking> locationBookings = filterBookingsByLocation(bookings, locationId);

        OccupancyStatisticsResponse response = new OccupancyStatisticsResponse();
        response.setLocationId(locationId);
        response.setLocationName(location.getName());
        response.setTotalSlots(totalSlots);
        response.setCurrentlyOccupied(currentlyOccupied);
        response.setCurrentlyAvailable(currentlyAvailable);
        response.setCurrentOccupancyPercentage(totalSlots > 0 ? (double) currentlyOccupied / totalSlots * 100 : 0);
        response.setTimeGranularity(timeGranularity);

        List<OccupancyDataPoint> dataPoints = generateDataPoints(locationBookings, totalSlots, timeGranularity, startDate, endDate);
        response.setDataPoints(dataPoints);

        calculateAggregateStatistics(response, locationBookings);

        response.setFetchTimeMs(System.currentTimeMillis() - fetchStartTime);

        return response;
    }

    public OccupancyStatisticsResponse getDailyOccupancyTrends(Long locationId, LocalDateTime startDate, LocalDateTime endDate) {
        return getOccupancyStatistics(locationId, startDate, endDate, "DAILY");
    }

    public OccupancyStatisticsResponse getHourlyOccupancyTrends(Long locationId, LocalDateTime startDate, LocalDateTime endDate) {
        return getOccupancyStatistics(locationId, startDate, endDate, "HOURLY");
    }

    public OccupancyStatisticsResponse getWeeklyOccupancyTrends(Long locationId, LocalDateTime startDate, LocalDateTime endDate) {
        return getOccupancyStatistics(locationId, startDate, endDate, "WEEKLY");
    }

    public OccupancyStatisticsResponse getMonthlyOccupancyTrends(Long locationId, LocalDateTime startDate, LocalDateTime endDate) {
        return getOccupancyStatistics(locationId, startDate, endDate, "MONTHLY");
    }

    public Map<String, Object> getPeakHours(Long locationId) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getSlotId() != null)
                .collect(Collectors.toList());

        Map<Integer, Long> hourlyCount = bookings.stream()
                .filter(b -> b.getEntryTime() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getEntryTime().getHour(),
                        Collectors.counting()
                ));

        Integer peakHour = hourlyCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);

        Long peakHourBookings = peakHour >= 0 ? hourlyCount.get(peakHour) : 0;

        return Map.of(
                "peakHour", peakHour,
                "bookingsAtPeakHour", peakHourBookings,
                "hourlyBreakdown", hourlyCount
        );
    }

    public Map<String, Object> getUsageTrends(Long locationId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findBookingsByDateRange(startDate, endDate);
        List<Booking> locationBookings = filterBookingsByLocation(bookings, locationId);

        long completedBookings = locationBookings.stream()
                .filter(b -> b.getStatus() == Booking.ParkingStatus.COMPLETED)
                .count();

        long cancelledBookings = locationBookings.stream()
                .filter(b -> b.getStatus() == Booking.ParkingStatus.CANCELLED)
                .count();

        double averageDuration = locationBookings.stream()
                .filter(b -> b.getEntryTime() != null && b.getExitTime() != null)
                .mapToDouble(b -> Duration.between(b.getEntryTime(), b.getExitTime()).toMinutes())
                .average()
                .orElse(0);

        double totalRevenue = locationBookings.stream()
                .filter(b -> b.getParkingFee() != null)
                .mapToDouble(Booking::getParkingFee)
                .sum();

        return Map.of(
                "locationId", locationId,
                "periodStart", startDate,
                "periodEnd", endDate,
                "totalBookings", locationBookings.size(),
                "completedBookings", completedBookings,
                "cancelledBookings", cancelledBookings,
                "averageDurationMinutes", averageDuration,
                "totalRevenue", totalRevenue,
                "completionRate", locationBookings.size() > 0 ? 
                                  (double) completedBookings / locationBookings.size() * 100 : 0
        );
    }

    private List<OccupancyDataPoint> generateDataPoints(List<Booking> bookings, long totalSlots, 
                                                        String granularity, LocalDateTime startDate, LocalDateTime endDate) {
        List<OccupancyDataPoint> dataPoints = new ArrayList<>();

        if ("HOURLY".equalsIgnoreCase(granularity)) {
            dataPoints = generateHourlyDataPoints(bookings, totalSlots, startDate, endDate);
        } else if ("DAILY".equalsIgnoreCase(granularity)) {
            dataPoints = generateDailyDataPoints(bookings, totalSlots, startDate, endDate);
        } else if ("WEEKLY".equalsIgnoreCase(granularity)) {
            dataPoints = generateWeeklyDataPoints(bookings, totalSlots, startDate, endDate);
        } else if ("MONTHLY".equalsIgnoreCase(granularity)) {
            dataPoints = generateMonthlyDataPoints(bookings, totalSlots, startDate, endDate);
        }

        return dataPoints;
    }

    private List<OccupancyDataPoint> generateHourlyDataPoints(List<Booking> bookings, long totalSlots,
                                                              LocalDateTime startDate, LocalDateTime endDate) {
        List<OccupancyDataPoint> points = new ArrayList<>();

        LocalDateTime currentTime = startDate;
        while (currentTime.isBefore(endDate)) {
            LocalDateTime hourEnd = currentTime.plusHours(1);
            final LocalDateTime periodStart = currentTime;
            final LocalDateTime periodEnd = hourEnd;
            
            long occupiedInHour = countOccupiedSlots(bookings, periodStart, periodEnd);

            OccupancyDataPoint point = new OccupancyDataPoint(
                    periodStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00")),
                    (int) totalSlots,
                    (int) occupiedInHour
            );

            long bookingsInHour = bookings.stream()
                    .filter(b -> b.getEntryTime().isAfter(periodStart) && b.getEntryTime().isBefore(periodEnd))
                    .count();
            point.setBookingsCount(bookingsInHour);

            points.add(point);
            currentTime = periodEnd;
        }

        return points;
    }

    private List<OccupancyDataPoint> generateDailyDataPoints(List<Booking> bookings, long totalSlots,
                                                             LocalDateTime startDate, LocalDateTime endDate) {
        List<OccupancyDataPoint> points = new ArrayList<>();

        LocalDate currentDate = startDate.toLocalDate();
        LocalDate endDateLocal = endDate.toLocalDate();

        while (!currentDate.isAfter(endDateLocal)) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.atStartOfDay().plusDays(1);
            final LocalDateTime periodStart = dayStart;
            final LocalDateTime periodEnd = dayEnd;

            long occupiedInDay = countOccupiedSlots(bookings, periodStart, periodEnd);

            OccupancyDataPoint point = new OccupancyDataPoint(
                    currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    (int) totalSlots,
                    (int) occupiedInDay
            );

            long bookingsInDay = bookings.stream()
                    .filter(b -> b.getEntryTime().isAfter(periodStart) && b.getEntryTime().isBefore(periodEnd))
                    .count();
            point.setBookingsCount(bookingsInDay);

            points.add(point);
            currentDate = currentDate.plusDays(1);
        }

        return points;
    }

    private List<OccupancyDataPoint> generateWeeklyDataPoints(List<Booking> bookings, long totalSlots,
                                                              LocalDateTime startDate, LocalDateTime endDate) {
        List<OccupancyDataPoint> points = new ArrayList<>();

        LocalDate currentDate = startDate.toLocalDate();
        LocalDate endDateLocal = endDate.toLocalDate();

        while (!currentDate.isAfter(endDateLocal)) {
            LocalDateTime weekStart = currentDate.atStartOfDay();
            LocalDateTime weekEnd = currentDate.atStartOfDay().plusWeeks(1);
            final LocalDateTime periodStart = weekStart;
            final LocalDateTime periodEnd = weekEnd;

            long occupiedInWeek = countOccupiedSlots(bookings, periodStart, periodEnd);

            OccupancyDataPoint point = new OccupancyDataPoint(
                    "Week of " + currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    (int) totalSlots,
                    (int) occupiedInWeek
            );

            long bookingsInWeek = bookings.stream()
                    .filter(b -> b.getEntryTime().isAfter(periodStart) && b.getEntryTime().isBefore(periodEnd))
                    .count();
            point.setBookingsCount(bookingsInWeek);

            points.add(point);
            currentDate = currentDate.plusWeeks(1);
        }

        return points;
    }

    private List<OccupancyDataPoint> generateMonthlyDataPoints(List<Booking> bookings, long totalSlots,
                                                               LocalDateTime startDate, LocalDateTime endDate) {
        List<OccupancyDataPoint> points = new ArrayList<>();

        YearMonth currentMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);

        while (!currentMonth.isAfter(endMonth)) {
            LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
            final LocalDateTime periodStart = monthStart;
            final LocalDateTime periodEnd = monthEnd;

            long occupiedInMonth = countOccupiedSlots(bookings, periodStart, periodEnd);

            OccupancyDataPoint point = new OccupancyDataPoint(
                    currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    (int) totalSlots,
                    (int) occupiedInMonth
            );

            long bookingsInMonth = bookings.stream()
                    .filter(b -> b.getEntryTime().isAfter(periodStart) && b.getEntryTime().isBefore(periodEnd))
                    .count();
            point.setBookingsCount(bookingsInMonth);

            points.add(point);
            currentMonth = currentMonth.plusMonths(1);
        }

        return points;
    }

    private long countOccupiedSlots(List<Booking> bookings, LocalDateTime start, LocalDateTime end) {
        return bookings.stream()
                .filter(b -> b.getEntryTime() != null && b.getExitTime() != null)
                .filter(b -> !b.getEntryTime().isAfter(end) && !b.getExitTime().isBefore(start))
                .count();
    }

    private List<Booking> filterBookingsByLocation(List<Booking> bookings, Long locationId) {
        List<Long> slotIds = slotRepository.findByLocationId(locationId).stream()
                .map(Slot::getId)
                .collect(Collectors.toList());

        return bookings.stream()
                .filter(b -> slotIds.contains(b.getSlotId()))
                .collect(Collectors.toList());
    }

    private void calculateAggregateStatistics(OccupancyStatisticsResponse response, List<Booking> bookings) {
        if (response.getDataPoints().isEmpty()) {
            response.setAverageOccupancyPercentage(0.0);
            response.setPeakOccupancyPercentage(0.0);
            return;
        }

        double averageOccupancy = response.getDataPoints().stream()
                .mapToDouble(OccupancyDataPoint::getOccupancyPercentage)
                .average()
                .orElse(0);

        double peakOccupancy = response.getDataPoints().stream()
                .mapToDouble(OccupancyDataPoint::getOccupancyPercentage)
                .max()
                .orElse(0);

        response.setAverageOccupancyPercentage(averageOccupancy);
        response.setPeakOccupancyPercentage(peakOccupancy);
        response.setTotalBookingsInPeriod((long) bookings.size());

        double avgDuration = bookings.stream()
                .filter(b -> b.getEntryTime() != null && b.getExitTime() != null)
                .mapToDouble(b -> Duration.between(b.getEntryTime(), b.getExitTime()).toMinutes())
                .average()
                .orElse(0);

        double totalRevenue = bookings.stream()
                .filter(b -> b.getParkingFee() != null)
                .mapToDouble(Booking::getParkingFee)
                .sum();

        response.setAverageSessionDurationMinutes(avgDuration);
        response.setTotalRevenueInPeriod(totalRevenue);
    }
}
