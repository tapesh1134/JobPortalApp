package org.jobportal.interviewservice.service;

import org.jobportal.interviewservice.dto.InterviewDto;
import org.jobportal.interviewservice.entity.Interview;
import org.jobportal.interviewservice.entity.InterviewStatus;
import org.jobportal.interviewservice.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewServiceImpl implements InterviewService {
    private final InterviewRepository interviewRepository;

    @Autowired
    public InterviewServiceImpl(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    @Override
    public Interview scheduleInterview(InterviewDto dto, String recruiterEmail) {
        if (dto.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Interview time cannot be in the past");
        }
        Interview interview = Interview.builder()
                .applicationId(dto.getApplicationId())
                .candidateEmail(dto.getCandidateEmail())
                .recruiterEmail(recruiterEmail)
                .scheduledAt(dto.getScheduledAt())
                .mode(dto.getMode())
                .meetLink(dto.getMeetLink())
                .location(dto.getLocation())
                .status(InterviewStatus.SCHEDULED)
                .notes(dto.getNotes())
                .build();
        return interviewRepository.save(interview);
    }

    @Override
    public void confirmInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow(() -> new RuntimeException("No interview found with id: " + interviewId));
        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled interview cannot be modified");
        }
        interview.setStatus(InterviewStatus.CONFIRMED);
        interviewRepository.save(interview);
    }

    @Override
    public Interview rescheduleInterview(Long interviewId, LocalDateTime newTIme) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow(() -> new RuntimeException("No interview found with id: " + interviewId));
        if (newTIme.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Interview time cannot be in the past");
        }
        interview.setStatus(InterviewStatus.RESCHEDULED);
        interview.setScheduledAt(newTIme);
        return interviewRepository.save(interview);
    }

    @Override
    public void cancelInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow(() -> new RuntimeException("No interview found with id: " + interviewId));
        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new IllegalStateException("Completed interview cannot be cancelled");
        }
        interview.setStatus(InterviewStatus.CANCELLED);
        interviewRepository.save(interview);
    }

    @Override
    public void completeInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow(() -> new RuntimeException("No interview found with id: " + interviewId));
        if (interview.getStatus() != InterviewStatus.CONFIRMED && interview.getStatus() != InterviewStatus.RESCHEDULED) {
            throw new IllegalStateException("Only confirmed interviews can be completed");
        }
        interview.setStatus(InterviewStatus.COMPLETED);
        interviewRepository.save(interview);
    }

    @Override
    public List<Interview> getByApplicationId(Long applicationId) {
        return interviewRepository.findByApplicationId(applicationId).orElse(List.of());
    }

    @Override
    public List<Interview> getByStatus(InterviewStatus status) {
        return interviewRepository.findByStatus(status).orElse(List.of());
    }

    @Override
    public Interview getByInterviewId(Long interviewId) {
        return interviewRepository.findById(interviewId).orElseThrow(() -> new RuntimeException("No interview found with id: " + interviewId));
    }

    @Override
    public List<Interview> getByCandidateEmail(String candidateEmail) {
        return interviewRepository.findByCandidateEmail(candidateEmail).orElse(List.of());
    }

    @Override
    public List<Interview> getByRecruiterEmail(String recruiterEmail) {
        return interviewRepository.findByRecruiterEmail(recruiterEmail).orElse(List.of());
    }
}
