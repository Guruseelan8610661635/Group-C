package com.smartparking.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.smartparking.dto.*;
import com.smartparking.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/daily")
    public ResponseEntity<?> getDailyReport() {
        try {
            ReportResponse report = reportService.generateDailyReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyReport() {
        try {
            ReportResponse report = reportService.generateWeeklyReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport() {
        try {
            ReportResponse report = reportService.generateMonthlyReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/custom")
    public ResponseEntity<?> getCustomReport(@RequestBody ReportRequest reportRequest) {
        try {
            ReportResponse report = reportService.generateReport(
                    reportRequest.getStartDate(),
                    reportRequest.getEndDate(),
                    reportRequest.getReportType()
            );
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Usage Reports Endpoints (Comprehensive Metrics) ==========

    @GetMapping("/usage/daily")
    public ResponseEntity<?> getDailyUsageReport() {
        try {
            UsageReportResponse report = reportService.generateDailyUsageReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/usage/weekly")
    public ResponseEntity<?> getWeeklyUsageReport() {
        try {
            UsageReportResponse report = reportService.generateWeeklyUsageReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/usage/monthly")
    public ResponseEntity<?> getMonthlyUsageReport() {
        try {
            UsageReportResponse report = reportService.generateMonthlyUsageReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/usage/custom")
    public ResponseEntity<?> getCustomUsageReport(@RequestBody ReportRequest reportRequest) {
        try {
            UsageReportResponse report = reportService.generateUsageReport(
                    reportRequest.getStartDate(),
                    reportRequest.getEndDate(),
                    reportRequest.getReportType()
            );
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== CSV Export Endpoint ==========

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<?> exportBookingsCsv(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "slotId", required = false) Long slotId,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        try {
            LocalDateTime start = null;
            LocalDateTime end = null;
            if (startDateStr != null && endDateStr != null) {
                try {
                    start = LocalDateTime.parse(startDateStr);
                    end = LocalDateTime.parse(endDateStr);
                } catch (DateTimeParseException ex) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Invalid date format. Use ISO-8601, e.g., 2025-01-01T00:00:00"));
                }
            }

            String csv = reportService.generateBookingsCsv(start, end, slotId, userId);

            String filename = "bookings_export" +
                    (start != null && end != null ? ("_" + start.toLocalDate() + "-" + end.toLocalDate()) : "") +
                    (slotId != null ? ("_slot-" + slotId) : "") +
                    (userId != null ? ("_user-" + userId) : "") +
                    ".csv";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(csv.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== ADVANCED ANALYTICS ENDPOINTS ==========
    
    /**
     * Get location performance comparison
     */
    @GetMapping("/analytics/locations")
    public ResponseEntity<?> getLocationComparison(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        try {
            LocalDateTime start = startDateStr != null ? LocalDateTime.parse(startDateStr) : LocalDateTime.now().minusDays(7);
            LocalDateTime end = endDateStr != null ? LocalDateTime.parse(endDateStr) : LocalDateTime.now();
            
            List<LocationPerformanceDTO> locations = reportService.getLocationComparison(start, end);
            return ResponseEntity.ok(locations);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid date format. Use ISO-8601, e.g., 2025-01-01T00:00:00"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get slot utilization analytics
     */
    @GetMapping("/analytics/slots/utilization")
    public ResponseEntity<?> getSlotUtilization(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        try {
            LocalDateTime start = startDateStr != null ? LocalDateTime.parse(startDateStr) : LocalDateTime.now().minusDays(7);
            LocalDateTime end = endDateStr != null ? LocalDateTime.parse(endDateStr) : LocalDateTime.now();
            
            List<SlotUtilizationDTO> slots = reportService.getSlotUtilization(start, end);
            return ResponseEntity.ok(slots);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid date format"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get top utilized slots
     */
    @GetMapping("/analytics/slots/top")
    public ResponseEntity<?> getTopSlots(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        try {
            LocalDateTime start = startDateStr != null ? LocalDateTime.parse(startDateStr) : LocalDateTime.now().minusDays(7);
            LocalDateTime end = endDateStr != null ? LocalDateTime.parse(endDateStr) : LocalDateTime.now();
            
            List<SlotUtilizationDTO> slots = reportService.getTopUtilizedSlots(start, end, limit);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get least utilized slots
     */
    @GetMapping("/analytics/slots/least")
    public ResponseEntity<?> getLeastSlots(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        try {
            LocalDateTime start = startDateStr != null ? LocalDateTime.parse(startDateStr) : LocalDateTime.now().minusDays(7);
            LocalDateTime end = endDateStr != null ? LocalDateTime.parse(endDateStr) : LocalDateTime.now();
            
            List<SlotUtilizationDTO> slots = reportService.getLeastUtilizedSlots(start, end, limit);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get revenue analytics
     */
    @GetMapping("/analytics/revenue")
    public ResponseEntity<?> getRevenueAnalytics(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        try {
            LocalDateTime start = startDateStr != null ? LocalDateTime.parse(startDateStr) : LocalDateTime.now().minusDays(7);
            LocalDateTime end = endDateStr != null ? LocalDateTime.parse(endDateStr) : LocalDateTime.now();
            
            RevenueAnalyticsDTO revenue = reportService.getRevenueAnalytics(start, end);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get user behavior analytics
     */
    @GetMapping("/analytics/users")
    public ResponseEntity<?> getUserBehaviorAnalytics(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        try {
            LocalDateTime start = startDateStr != null ? LocalDateTime.parse(startDateStr) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endDateStr != null ? LocalDateTime.parse(endDateStr) : LocalDateTime.now();
            
            List<UserBehaviorDTO> users = reportService.getUserBehaviorAnalytics(start, end);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get top users
     */
    @GetMapping("/analytics/users/top")
    public ResponseEntity<?> getTopUsers(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        try {
            LocalDateTime start = startDateStr != null ? LocalDateTime.parse(startDateStr) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endDateStr != null ? LocalDateTime.parse(endDateStr) : LocalDateTime.now();
            
            List<UserBehaviorDTO> users = reportService.getTopUsers(start, end, limit);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get occupancy heatmap
     */
    @GetMapping("/analytics/heatmap")
    public ResponseEntity<?> getOccupancyHeatmap(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        try {
            LocalDateTime start = startDateStr != null ? LocalDateTime.parse(startDateStr) : LocalDateTime.now().minusDays(7);
            LocalDateTime end = endDateStr != null ? LocalDateTime.parse(endDateStr) : LocalDateTime.now();
            
            List<OccupancyHeatmapDTO> heatmap = reportService.getOccupancyHeatmap(start, end);
            return ResponseEntity.ok(heatmap);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
