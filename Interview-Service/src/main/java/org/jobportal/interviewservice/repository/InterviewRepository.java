package org.jobportal.interviewservice.repository;

import org.jobportal.interviewservice.entity.Interview;
import org.jobportal.interviewservice.entity.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Optional<List<Interview>> findByApplicationId(Long id);
    Optional<List<Interview>> findByStatus(InterviewStatus status);
    Optional<List<Interview>> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
    Optional<List<Interview>> findByCandidateEmail(String candidateEmail);
    Optional<List<Interview>> findByRecruiterEmail(String recruiterEmail);
    void deleteByInterviewId(Long id);
}
