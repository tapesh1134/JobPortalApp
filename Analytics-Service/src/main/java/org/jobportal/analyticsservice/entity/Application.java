package org.jobportal.analyticsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    private Long applicationId;
    private Long jobId;
    private String candidateEmail;
    private LocalDate appliedAt;
    private ApplicationStatus status;
    private String coverLetter;
    private String resumeUrl;
}
