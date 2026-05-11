package org.jobportal.applicationservice;

import org.jobportal.applicationservice.entity.Application;
import org.jobportal.applicationservice.entity.ApplicationStatus;
import org.jobportal.applicationservice.service.ApplicationService;
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


import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    @WithMockUser(username = "test@gmail.com")
    void testSubmitApplication_success() throws Exception {
        Application app = new Application();
        app.setJobId(1L);
        app.setCandidateEmail("test@gmail.com");
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(LocalDate.now());
        Mockito.when(applicationService.submitApplication(anyString(), any())).thenReturn(app);

        mockMvc.perform(post("/application/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "jobId": 1,
                                    "resumeUrl": "test.pdf"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void testGetMyApplications_success() throws Exception {
        Mockito.when(applicationService.getByCandidateEmail(anyString())).thenReturn(List.of(new Application()));
        mockMvc.perform(get("/application/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetByJobId_success() throws Exception {
        Mockito.when(applicationService.getByJobId(anyLong())).thenReturn(List.of(new Application()));
        mockMvc.perform(get("/application/job/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUpdateStatus_success() throws Exception {
        Application app = new Application();
        app.setApplicationId(1L);
        app.setCandidateEmail("test@gmail.com");
        Mockito.when(applicationService.getApplicationById(anyLong())).thenReturn(app);

        mockMvc.perform(put("/application/1/status")
                        .param("status", "SHORTLISTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void testWithdraw_success() throws Exception {
        Mockito.doNothing().when(applicationService).withdrawApplication(anyLong(), anyString());
        mockMvc.perform(put("/application/1/withdraw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetByStatus_success() throws Exception {
        Mockito.when(applicationService.getApplicationByStatus(any())).thenReturn(List.of(new Application()));
        mockMvc.perform(get("/application/status/APPLIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetById_success() throws Exception {
        Application app = new Application();
        app.setApplicationId(1L);
        Mockito.when(applicationService.getApplicationById(anyLong())).thenReturn(app);
        mockMvc.perform(get("/application/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
