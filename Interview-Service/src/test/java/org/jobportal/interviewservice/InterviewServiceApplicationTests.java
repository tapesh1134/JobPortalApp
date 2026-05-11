package org.jobportal.interviewservice;

import org.jobportal.interviewservice.entity.Interview;
import org.jobportal.interviewservice.entity.InterviewMode;
import org.jobportal.interviewservice.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InterviewServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterviewService interviewService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    @WithMockUser(username = "recruiter@gmail.com", roles = {"RECRUITER"})
    void testScheduleInterview_success() throws Exception {
        Interview interview = new Interview();
        interview.setCandidateEmail("candidate@gmail.com");
        interview.setScheduledAt(LocalDateTime.now());
        interview.setMode(InterviewMode.ONLINE);
        Mockito.when(interviewService.scheduleInterview(any(), anyString())).thenReturn(interview);

        mockMvc.perform(post("/interview/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "candidateEmail": "candidate@gmail.com",
                                    "mode": "ONLINE",
                                    "scheduledAt": "2026-05-01T10:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "candidate@gmail.com", roles = {"CANDIDATE"})
    void testConfirmInterview_success() throws Exception {
        Interview interview = new Interview();
        interview.setCandidateEmail("candidate@gmail.com");
        interview.setScheduledAt(LocalDateTime.now());
        interview.setMode(InterviewMode.ONLINE);
        Mockito.when(interviewService.getByInterviewId(anyLong())).thenReturn(interview);
        mockMvc.perform(put("/interview/confirm/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "recruiter@gmail.com", roles = {"RECRUITER"})
    void testReschedule_success() throws Exception {
        Interview interview = new Interview();
        interview.setCandidateEmail("candidate@gmail.com");
        interview.setRecruiterEmail("recruiter@gmail.com");
        interview.setScheduledAt(LocalDateTime.now());
        interview.setMode(InterviewMode.ONLINE);
        Mockito.when(interviewService.rescheduleInterview(anyLong(), any())).thenReturn(interview);

        mockMvc.perform(put("/interview/reschedule/1")
                        .param("rescheduledAt", "2026-05-02T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "recruiter@gmail.com", roles = {"RECRUITER"})
    void testCompleteInterview_success() throws Exception {
        Interview interview = new Interview();
        interview.setCandidateEmail("candidate@gmail.com");
        interview.setRecruiterEmail("recruiter@gmail.com");
        interview.setScheduledAt(LocalDateTime.now());
        Mockito.when(interviewService.getByInterviewId(anyLong())).thenReturn(interview);
        Mockito.doNothing().when(interviewService).completeInterview(anyLong());

        mockMvc.perform(put("/interview/complete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "candidate@gmail.com", roles = {"CANDIDATE"})
    void testGetMyInterviews_success() throws Exception {
        Mockito.when(interviewService.getByCandidateEmail(anyString())).thenReturn(List.of(new Interview()));

        mockMvc.perform(get("/interview/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "candidate@gmail.com", roles = {"CANDIDATE"})
    void testCancelInterview_success() throws Exception {
        Interview interview = new Interview();
        interview.setCandidateEmail("candidate@gmail.com");
        interview.setRecruiterEmail("recruiter@gmail.com");
        interview.setScheduledAt(LocalDateTime.now());
        Mockito.when(interviewService.getByInterviewId(anyLong())).thenReturn(interview);
        Mockito.doNothing().when(interviewService).cancelInterview(anyLong());

        mockMvc.perform(put("/interview/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "recruiter@gmail.com", roles = {"RECRUITER"})
    void testGetByApplication_success() throws Exception {
        Mockito.when(interviewService.getByApplicationId(anyLong())).thenReturn(List.of(new Interview()));

        mockMvc.perform(get("/interview/application/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "recruiter@gmail.com", roles = {"RECRUITER"})
    void testGetByStatus_success() throws Exception {
        Mockito.when(interviewService.getByStatus(any())).thenReturn(List.of(new Interview()));

        mockMvc.perform(get("/interview/status/SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "recruiter@gmail.com", roles = {"RECRUITER"})
    void testGetByRecruiter_success() throws Exception {
        Mockito.when(interviewService.getByRecruiterEmail(anyString())).thenReturn(List.of(new Interview()));

        mockMvc.perform(get("/interview/recruiter/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = {"RECRUITER", "CANDIDATE"})
    void testGetById_success() throws Exception {
        Mockito.when(interviewService.getByInterviewId(anyLong())).thenReturn(new Interview());

        mockMvc.perform(get("/interview/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}