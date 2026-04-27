package org.jobportal.analyticsservice.service;

import org.jobportal.analyticsservice.entity.Analytics;
import org.springframework.stereotype.Service;

public interface AnalyticsService {
    Analytics getAnalytics(String recruiterEmail);
    Analytics getAdminAnalytics();
}
