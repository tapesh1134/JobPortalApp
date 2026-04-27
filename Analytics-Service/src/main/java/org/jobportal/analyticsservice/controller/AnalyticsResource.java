package org.jobportal.analyticsservice.controller;

import org.jobportal.analyticsservice.dto.ApiResponse;
import org.jobportal.analyticsservice.entity.Analytics;
import org.jobportal.analyticsservice.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsResource {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsResource(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Recruiter Analytics
    @GetMapping("/recruiter/{id}")
    public ResponseEntity<ApiResponse<Analytics>> getRecruiterAnalytics(@PathVariable String id) {
        Analytics analytics = analyticsService.getAnalytics(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Recruiter analytics fetched successfully", analytics));
    }

    // Admin Analytics
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Analytics>> getAdminAnalytics() {
        Analytics analytics = analyticsService.getAdminAnalytics();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Admin analytics fetched successfully", analytics));
    }
}
