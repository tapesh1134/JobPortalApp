package org.jobportal.analyticsservice;

import org.jobportal.analyticsservice.dto.ApiResponse;
import org.jobportal.analyticsservice.entity.Analytics;
import org.jobportal.analyticsservice.entity.Application;
import org.jobportal.analyticsservice.entity.ApplicationStatus;
import org.jobportal.analyticsservice.entity.Job;
import org.jobportal.analyticsservice.feign.ApplicationClient;
import org.jobportal.analyticsservice.feign.JobClient;
import org.jobportal.analyticsservice.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.cache.type=none") // for disable caching
@AutoConfigureMockMvc
class AnalyticsServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobClient jobClient;

    @MockitoBean
    private ApplicationClient applicationClient;

    @Autowired
    private AnalyticsService analyticsService;

    @Test
    void testGetRecruiterAnalytics_Logic() {
        String email = "recruiter@gmail.com";
        Job job1 = new Job(); job1.setJobId(1L); job1.setPostedBy(email);
        Job job2 = new Job(); job2.setJobId(2L); job2.setPostedBy("other@gmail.com");

        when(jobClient.getJobs()).thenReturn(new ApiResponse<>(true, "success", List.of(job1, job2)));

        Application app1 = new Application(); app1.setStatus(ApplicationStatus.SHORTLISTED);
        Application app2 = new Application(); app2.setStatus(ApplicationStatus.OFFERED);

        when(applicationClient.getByJobId(1L)).thenReturn(new ApiResponse<>(true, "success", List.of(app1, app2)));

        Analytics analytics = analyticsService.getAnalytics(email);
        assertEquals(1, analytics.getTotalJobs()); // Only job1 belongs to recruiter
        assertEquals(2, analytics.getTotalApplications());
    }

    @Test
    void testGetAdminAnalytics_Logic() {
        Job job1 = new Job(); job1.setJobId(1L);
        when(jobClient.getJobs()).thenReturn(new ApiResponse<>(true, "success", List.of(job1)));

        Application app1 = new Application(); app1.setStatus(ApplicationStatus.SHORTLISTED);
        when(applicationClient.getByJobId(1L)).thenReturn(new ApiResponse<>(true, "success", List.of(app1)));

        Analytics analytics = analyticsService.getAdminAnalytics();
        assertEquals(1, analytics.getTotalJobs());
        assertEquals(1, analytics.getTotalApplications());
    }

    @Test
    void testGetRecruiterAnalyticsAPI() throws Exception {
        String email = "recruiter@gmail.com";
        Job job = new Job(); job.setJobId(1L); job.setPostedBy(email);

        when(jobClient.getJobs()).thenReturn(new ApiResponse<>(true, "success", List.of(job)));
        when(applicationClient.getByJobId(1L)).thenReturn(new ApiResponse<>(true, "success", List.of()));

        mockMvc.perform(get("/analytics/recruiter/" + email).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recruiter analytics fetched successfully"))
                .andExpect(jsonPath("$.data.totalJobs").value(1));
    }

    @Test
    void testGetAdminAnalyticsAPI() throws Exception {
        when(jobClient.getJobs()).thenReturn(new ApiResponse<>(true, "success", List.of()));

        mockMvc.perform(get("/analytics/admin").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Admin analytics fetched successfully"));
    }
}