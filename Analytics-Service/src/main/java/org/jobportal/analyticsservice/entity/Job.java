package org.jobportal.analyticsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job implements Serializable {
    private Long jobId;
    private String title;
    private String category;
    private JobType type;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
    private List<String> skills;
    private Long experienceRequired;
    private String postedBy;
    private JobStatus status;
    private LocalDateTime postedAt;
}
