package org.jobportal.interviewservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Interview implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewId;
    @Column(nullable = false)
    private Long applicationId;
    @Column(nullable = false)
    private String candidateEmail;
    @Column(nullable = false)
    private String recruiterEmail;
    private LocalDateTime scheduledAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewMode mode;
    private String meetLink;
    private String location;
    @Enumerated(EnumType.STRING)
    private InterviewStatus status;
    private String notes;
}
