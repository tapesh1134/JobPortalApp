package org.jobportal.applicationservice.service;

import org.jobportal.applicationservice.dto.ApplicationDto;
import org.jobportal.applicationservice.entity.Application;
import org.jobportal.applicationservice.entity.ApplicationStatus;
import org.jobportal.applicationservice.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final CacheManager cacheManager;

    @Autowired
    public ApplicationServiceImpl(ApplicationRepository applicationRepository, CacheManager cacheManager) {
        this.applicationRepository = applicationRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @Cacheable(value = "application", key = "#candidateEmail")
    public Application submitApplication(String candidateEmail, ApplicationDto dto) {
        if (dto.getJobId() == null) {
            throw new IllegalArgumentException("Job ID is required");
        }
        if (dto.getResumeUrl() == null || dto.getResumeUrl().isBlank()) {
            throw new IllegalArgumentException("Resume URL is required");
        }
        // Prevent duplicate application
        if (applicationRepository.findFirstByJobIdAndCandidateEmail(dto.getJobId(), candidateEmail).isPresent()) {
            throw new IllegalStateException("Already applied to this job");
        }

        Application application = Application.builder()
                .jobId(dto.getJobId())
                .candidateEmail(candidateEmail)
                .status(ApplicationStatus.APPLIED)
                .coverLetter(dto.getCoverLetter())
                .resumeUrl(dto.getResumeUrl())
                .build();
        return applicationRepository.save(application);
    }

    @Override
    @Cacheable(value = "allApplications", key = "#candidateEmail")
    public List<Application> getByCandidateEmail(String candidateEmail) {
        return applicationRepository.findByCandidateEmail(candidateEmail).orElseThrow(() -> new RuntimeException("Applications not found on candidateEmail: " + candidateEmail));
    }

    @Override
    @Cacheable(value = "applicationsByJobId", key = "#jobId")
    public List<Application> getByJobId(Long jobId) {
        return applicationRepository.findByJobId(jobId).orElseThrow(() -> new RuntimeException("Application not found with jobId: " + jobId));
    }

    private boolean isValidTransition(ApplicationStatus current, ApplicationStatus next) {
        switch (current) {
            case APPLIED:
                return next == ApplicationStatus.SHORTLISTED || next == ApplicationStatus.REJECTED;
            case SHORTLISTED:
                return next == ApplicationStatus.INTERVIEW_SCHEDULED || next == ApplicationStatus.REJECTED;
            case INTERVIEW_SCHEDULED:
                return next == ApplicationStatus.OFFERED || next == ApplicationStatus.REJECTED;
            case OFFERED:
            case REJECTED:
                return false;
            default:
                return false;
        }
    }

    @Override
    public void updateStatus(Long id, ApplicationStatus newStatus) {
        Application application = applicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        ApplicationStatus currentStatus = application.getStatus();

        // Prevent same status update
        if (currentStatus == newStatus) {
            throw new IllegalArgumentException("Application is already in status: " + newStatus);
        }

        // Prevent invalid transitions
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);
        clearAllApplicationCaches();
    }

    @Override
    public void withdrawApplication(Long id, String candidateEmail) {
        Application application = applicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        ApplicationStatus currentStatus = application.getStatus();

        // Ownership Check
        if (!application.getCandidateEmail().equals(candidateEmail)) {
            throw new SecurityException("You are not allowed to withdraw this application");
        }

        // Already withdrawn
        if (currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new IllegalStateException("Application is already withdrawn");
        }

        // Cannot withdraw after final states
        if (currentStatus == ApplicationStatus.OFFERED || currentStatus == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Cannot withdraw application after final decision");
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
        clearAllApplicationCaches();
    }

    @Override
    @Cacheable(value = "applicationById", key = "#id")
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
    }

    @Override
    @Cacheable(value = "applicationsByStatus", key = "#status")
    public List<Application> getApplicationByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status).orElseThrow(() -> new RuntimeException("Application not found with status: " + status));
    }

    @Override
    @Cacheable(value = "applicationCount", key = "#id")
    public int countByJobId(Long id) {
        return applicationRepository.countByJobId(id);
    }

    @CacheEvict(value = {"allApplications", "applicationsByJobId", "applicationById", "applicationsByStatus", "applicationCount"}, allEntries = true)
    public void clearAllApplicationCaches() {
        System.out.println("All application caches cleared");
    }
}
