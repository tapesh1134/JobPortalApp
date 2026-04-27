package org.jobportal.interviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jobportal.interviewservice.entity.InterviewMode;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewDto {
    private Long applicationId;
    private String candidateEmail;
    private String recruiterEmail;
    private LocalDateTime scheduledAt;
    private InterviewMode mode;
    private String meetLink;
    private String location;
    private String notes;
}
