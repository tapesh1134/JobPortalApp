package org.jobportal.analyticsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Analytics {
    private int totalJobs;
    private int totalApplications;
    private int shortlistedCount;
    private int offeredCount;
    private int rejectedCount;
}
