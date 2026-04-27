package org.jobportal.analyticsservice.feign;

import org.jobportal.analyticsservice.dto.ApiResponse;
import org.jobportal.analyticsservice.entity.Job;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "JOB-SERVICE")
public interface JobClient {

    @GetMapping("/jobs")
    ApiResponse<List<Job>> getJobs();
}