package org.jobportal.analyticsservice.feign;

import org.jobportal.analyticsservice.dto.ApiResponse;
import org.jobportal.analyticsservice.entity.Application;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "APPLICATION-SERVICE")
public interface ApplicationClient {

    @GetMapping("/application/job/{jobId}")
    ApiResponse<List<Application>> getByJobId(@PathVariable Long jobId);
}