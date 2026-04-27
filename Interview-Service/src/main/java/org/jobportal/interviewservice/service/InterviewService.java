package org.jobportal.interviewservice.service;

import org.jobportal.interviewservice.dto.InterviewDto;
import org.jobportal.interviewservice.entity.Interview;
import org.jobportal.interviewservice.entity.InterviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewService {
    Interview scheduleInterview(InterviewDto dto, String recruiterEmail);
    void confirmInterview(Long interviewId);
    Interview rescheduleInterview(Long interviewId, LocalDateTime newTIme);
    void cancelInterview(Long interviewId);
    void completeInterview(Long interviewId);
    List<Interview> getByApplicationId(Long applicationId);
    List<Interview> getByStatus(InterviewStatus status);
    Interview getByInterviewId(Long interviewId);
    List<Interview> getByCandidateEmail(String candidateEmail);
    List<Interview> getByRecruiterEmail(String recruiterEmail);
}
