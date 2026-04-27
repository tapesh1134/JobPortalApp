package org.jobportal.interviewservice.controller;

import org.jobportal.interviewservice.dto.ApiResponse;
import org.jobportal.interviewservice.dto.InterviewDto;
import org.jobportal.interviewservice.entity.Interview;
import org.jobportal.interviewservice.entity.InterviewMode;
import org.jobportal.interviewservice.entity.InterviewStatus;
import org.jobportal.interviewservice.service.InterviewService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interview")
public class InterviewResource {
    private final InterviewService interviewService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public InterviewResource(InterviewService interviewService,  RabbitTemplate rabbitTemplate) {
        this.interviewService = interviewService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<Interview>> schedule(@RequestBody InterviewDto interviewDto, Authentication authentication) {
        String candidateMessage = "Your interview is scheduled on "
                + interviewDto.getScheduledAt()
                + " via " + interviewDto.getMode() + ".";

        if (interviewDto.getMode() == InterviewMode.ONLINE && interviewDto.getMeetLink() != null) {
            candidateMessage += " Join here: " + interviewDto.getMeetLink();
        }

        if (interviewDto.getMode() == InterviewMode.IN_PERSON && interviewDto.getLocation() != null) {
            candidateMessage += " Location: " + interviewDto.getLocation();
        }

        if (interviewDto.getNotes() != null) {
            candidateMessage += " Note: " + interviewDto.getNotes();
        }

        String recruiterMessage = "Interview scheduled with candidate ("
                + interviewDto.getCandidateEmail()
                + ") on " + interviewDto.getScheduledAt()
                + " via " + interviewDto.getMode() + ".";

        if (interviewDto.getMode() == InterviewMode.ONLINE && interviewDto.getMeetLink() != null) {
            recruiterMessage += " Meeting link: " + interviewDto.getMeetLink();
        }

        if (interviewDto.getMode() == InterviewMode.IN_PERSON && interviewDto.getLocation() != null) {
            recruiterMessage += " Location: " + interviewDto.getLocation();
        }

        if (interviewDto.getNotes() != null) {
            recruiterMessage += " Notes: " + interviewDto.getNotes();
        }

        Map<String, Object> candidateNotification = new HashMap<>();
        candidateNotification.put("userEmail", interviewDto.getCandidateEmail());
        candidateNotification.put("type", "INTERVIEW_SCHEDULED");
        candidateNotification.put("message", candidateMessage);
        candidateNotification.put("isRead", false);

        Map<String, Object> recruiterNotification = new HashMap<>();
        recruiterNotification.put("userEmail", authentication.getName());
        recruiterNotification.put("type", "INTERVIEW_SCHEDULED");
        recruiterNotification.put("message", recruiterMessage);
        recruiterNotification.put("isRead", false);

        System.out.println(authentication.getName());

        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                candidateNotification
        );
        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                recruiterNotification
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Interview scheduled successfully", interviewService.scheduleInterview(interviewDto, authentication.getName())));
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PutMapping("/confirm/{interviewId}")
    public ResponseEntity<ApiResponse<?>> confirmInterview(@PathVariable Long interviewId) {
        interviewService.confirmInterview(interviewId);

        Interview interview = interviewService.getByInterviewId(interviewId);
        String candidateMessage = "Your interview on " + interview.getScheduledAt() +
                " via " + interview.getMode() + " is now confirmed.";

        if (interview.getMode() == InterviewMode.ONLINE && interview.getMeetLink() != null) {
            candidateMessage += " Join here: " + interview.getMeetLink();
        }

        if (interview.getMode() == InterviewMode.IN_PERSON && interview.getLocation() != null) {
            candidateMessage += " Location: " + interview.getLocation();
        }

        String recruiterMessage = "Good news! The candidate (" + interview.getCandidateEmail() +
                ") has confirmed their availability for the interview on " +
                interview.getScheduledAt() + " via " + interview.getMode() + ".";

        if (interview.getMode() == InterviewMode.ONLINE && interview.getMeetLink() != null) {
            recruiterMessage += " Meeting link: " + interview.getMeetLink();
        }

        if (interview.getMode() == InterviewMode.IN_PERSON && interview.getLocation() != null) {
            recruiterMessage += " Location: " + interview.getLocation();
        }

        Map<String, Object> candidateNotification = new HashMap<>();
        candidateNotification.put("userEmail", interview.getCandidateEmail());
        candidateNotification.put("type", "INTERVIEW_CONFIRMED");
        candidateNotification.put("message", candidateMessage);
        candidateNotification.put("isRead", false);

        Map<String, Object> recruiterNotification = new HashMap<>();
        recruiterNotification.put("userEmail", interview.getRecruiterEmail());
        recruiterNotification.put("type", "INTERVIEW_CONFIRMED");
        recruiterNotification.put("message", recruiterMessage);
        recruiterNotification.put("isRead", false);

        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                candidateNotification
        );
        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                recruiterNotification
        );

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Interview Confirmed"));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/reschedule/{interviewId}")
    public ResponseEntity<ApiResponse<Interview>> rescheduleInterview(@PathVariable Long interviewId, @RequestParam LocalDateTime rescheduledAt) {
        Interview interview = interviewService.rescheduleInterview(interviewId, rescheduledAt);
        String candidateMessage = "Your interview has been rescheduled to "
                + interview.getScheduledAt() + " via " + interview.getMode() + ".";

        if (interview.getMode() == InterviewMode.ONLINE && interview.getMeetLink() != null) {
            candidateMessage += " Join here: " + interview.getMeetLink();
        }

        if (interview.getMode() == InterviewMode.IN_PERSON && interview.getLocation() != null) {
            candidateMessage += " New location: " + interview.getLocation();
        }

        String recruiterMessage = "The interview with candidate (" + interview.getCandidateEmail() +
                ") has been rescheduled to " + interview.getScheduledAt() +
                " via " + interview.getMode() + ".";

        if (interview.getMode() == InterviewMode.ONLINE && interview.getMeetLink() != null) {
            recruiterMessage += " Meeting link: " + interview.getMeetLink();
        }

        if (interview.getMode() == InterviewMode.IN_PERSON && interview.getLocation() != null) {
            recruiterMessage += " Location: " + interview.getLocation();
        }

        Map<String, Object> candidateNotification = new HashMap<>();
        candidateNotification.put("userEmail", interview.getCandidateEmail());
        candidateNotification.put("type", "INTERVIEW_RESCHEDULED");
        candidateNotification.put("message", candidateMessage);
        candidateNotification.put("isRead", false);

        Map<String, Object> recruiterNotification = new HashMap<>();
        recruiterNotification.put("userEmail", interview.getRecruiterEmail());
        recruiterNotification.put("type", "INTERVIEW_RESCHEDULED");
        recruiterNotification.put("message", recruiterMessage);
        recruiterNotification.put("isRead", false);

        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                candidateNotification
        );
        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                recruiterNotification
        );

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Interview rescheduled successfully", interview));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/complete/{interviewId}")
    public ResponseEntity<ApiResponse<?>> rescheduleInterview(@PathVariable Long interviewId) {
        interviewService.completeInterview(interviewId);
        Interview interview = interviewService.getByInterviewId(interviewId);
        String candidateMessage = "Your interview conducted on " + interview.getScheduledAt() + " has been successfully completed. Thank you for your time!";
        String recruiterMessage = "The interview with candidate (" + interview.getCandidateEmail() + ") scheduled on " + interview.getScheduledAt() + " has been completed.";
        Map<String, Object> candidateNotification = new HashMap<>();
        candidateNotification.put("userEmail", interview.getCandidateEmail());
        candidateNotification.put("type", "INTERVIEW_COMPLETED");
        candidateNotification.put("message", candidateMessage);
        candidateNotification.put("isRead", false);

        Map<String, Object> recruiterNotification = new HashMap<>();
        recruiterNotification.put("userEmail", interview.getRecruiterEmail());
        recruiterNotification.put("type", "INTERVIEW_COMPLETED");
        recruiterNotification.put("message", recruiterMessage);
        recruiterNotification.put("isRead", false);

        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                candidateNotification
        );
        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                recruiterNotification
        );
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Interview completed successfully"));
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<Interview>>> getCurrentInterview(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Candidate Interview Fetched", interviewService.getByCandidateEmail(authentication.getName())));
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PutMapping("/cancel/{interviewId}")
    public  ResponseEntity<ApiResponse<Interview>> cancelInterview(@PathVariable Long interviewId) {
        interviewService.cancelInterview(interviewId);

        Interview interview = interviewService.getByInterviewId(interviewId);
        String candidateMessage = "Your interview scheduled on " + interview.getScheduledAt() + " has been cancelled.";
        String recruiterMessage = "The interview with candidate (" + interview.getCandidateEmail() + ") scheduled on " + interview.getScheduledAt() + " has been cancelled.";
        Map<String, Object> candidateNotification = new HashMap<>();
        candidateNotification.put("userEmail", interview.getCandidateEmail());
        candidateNotification.put("type", "INTERVIEW_CANCELLED");
        candidateNotification.put("message", candidateMessage);
        candidateNotification.put("isRead", false);

        Map<String, Object> recruiterNotification = new HashMap<>();
        recruiterNotification.put("userEmail", interview.getRecruiterEmail());
        recruiterNotification.put("type", "INTERVIEW_CANCELLED");
        recruiterNotification.put("message", recruiterMessage);
        recruiterNotification.put("isRead", false);

        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                candidateNotification
        );
        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                recruiterNotification
        );

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Interview Cancelled"));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<List<Interview>>> getByApplicationId(@PathVariable Long applicationId) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Interviews found", interviewService.getByApplicationId(applicationId)));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Interview>>> getByStatus(@PathVariable InterviewStatus status) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Interviews found", interviewService.getByStatus(status)));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/recruiter/me")
    public ResponseEntity<ApiResponse<List<Interview>>> getByRecruiterEmail(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Interviews found", interviewService.getByRecruiterEmail(authentication.getName())));
    }

    @PreAuthorize("hasAnyRole('RECRUITER','CANDIDATE')")
    @GetMapping("/{interviewId}")
    public ResponseEntity<ApiResponse<Interview>> getById(@PathVariable Long interviewId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Interview fetched", interviewService.getByInterviewId(interviewId))
        );
    }
}
