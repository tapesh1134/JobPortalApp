package org.jobportal.jobservice;

import org.jobportal.jobservice.entity.Job;
import org.jobportal.jobservice.service.JobService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JobServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void testGetAllJobs_success() throws Exception {
        Mockito.when(jobService.getAllJobs()).thenReturn(List.of(new Job()));

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetJobById_success() throws Exception {
        Mockito.when(jobService.getJobById(anyLong())).thenReturn(new Job());

        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetByCategory_success() throws Exception {
        Mockito.when(jobService.getJobsByCategory(anyString())).thenReturn(List.of(new Job()));

        mockMvc.perform(get("/jobs/category/IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetByLocation_success() throws Exception {
        Mockito.when(jobService.getJobsByLocation(anyString())).thenReturn(List.of(new Job()));

        mockMvc.perform(get("/jobs/location/Delhi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSearchJobs_success() throws Exception {
        Mockito.when(jobService.searchJobs(any(), any(), any(), any(), any(), any())).thenReturn(List.of(new Job()));

        mockMvc.perform(get("/jobs/search")
                        .param("title", "Developer")
                        .param("category", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
