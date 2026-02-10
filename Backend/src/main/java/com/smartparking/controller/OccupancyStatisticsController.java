package com.smartparking.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.smartparking.dto.OccupancyStatisticsResponse;
import com.smartparking.service.OccupancyStatisticsService;

@RestController
@RequestMapping("/api/statistics/occupancy")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin
public class OccupancyStatisticsController {

    @Autowired
    private OccupancyStatisticsService occupancyStatisticsService;

    @GetMapping("/location/{locationId}")
    public ResponseEntity<?> getOccupancyStatistics(
            @PathVariable Long locationId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "DAILY") String granularity) {
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }

            OccupancyStatisticsResponse response = occupancyStatisticsService.getOccupancyStatistics(
                    locationId, startDate, endDate, granularity);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/location/{locationId}/daily")
    public ResponseEntity<?> getDailyTrends(
            @PathVariable Long locationId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }

            OccupancyStatisticsResponse response = occupancyStatisticsService.getDailyOccupancyTrends(
                    locationId, startDate, endDate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/location/{locationId}/hourly")
    public ResponseEntity<?> getHourlyTrends(
            @PathVariable Long locationId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusHours(24);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }

            OccupancyStatisticsResponse response = occupancyStatisticsService.getHourlyOccupancyTrends(
                    locationId, startDate, endDate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/location/{locationId}/weekly")
    public ResponseEntity<?> getWeeklyTrends(
            @PathVariable Long locationId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusWeeks(12);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }

            OccupancyStatisticsResponse response = occupancyStatisticsService.getWeeklyOccupancyTrends(
                    locationId, startDate, endDate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/location/{locationId}/monthly")
    public ResponseEntity<?> getMonthlyTrends(
            @PathVariable Long locationId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusYears(1);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }

            OccupancyStatisticsResponse response = occupancyStatisticsService.getMonthlyOccupancyTrends(
                    locationId, startDate, endDate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/location/{locationId}/peak-hours")
    public ResponseEntity<?> getPeakHours(@PathVariable Long locationId) {
        try {
            Map<String, Object> peakHours = occupancyStatisticsService.getPeakHours(locationId);
            return ResponseEntity.ok(peakHours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/location/{locationId}/usage-trends")
    public ResponseEntity<?> getUsageTrends(
            @PathVariable Long locationId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }

            Map<String, Object> usageTrends = occupancyStatisticsService.getUsageTrends(
                    locationId, startDate, endDate);

            return ResponseEntity.ok(usageTrends);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/location/{locationId}/quick")
    public ResponseEntity<?> getQuickOccupancyStats(@PathVariable Long locationId) {
        try {
            OccupancyStatisticsResponse response = occupancyStatisticsService.getOccupancyStatistics(
                    locationId,
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now(),
                    "HOURLY");

            return ResponseEntity.ok(Map.of(
                    "locationId", locationId,
                    "currentlyOccupied", response.getCurrentlyOccupied(),
                    "currentlyAvailable", response.getCurrentlyAvailable(),
                    "occupancyPercentage", response.getCurrentOccupancyPercentage(),
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
