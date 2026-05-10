package org.jobportal.analyticsservice.service;

import org.jobportal.analyticsservice.entity.Application;
import org.jobportal.analyticsservice.entity.ApplicationStatus;
import org.jobportal.analyticsservice.entity.Job;
import org.jobportal.analyticsservice.entity.Analytics;
import org.jobportal.analyticsservice.feign.ApplicationClient;
import org.jobportal.analyticsservice.feign.JobClient;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final JobClient jobClient;
    private final ApplicationClient applicationClient;

    public AnalyticsServiceImpl(JobClient jobClient, ApplicationClient applicationClient) {
        this.jobClient = jobClient;
        this.applicationClient = applicationClient;
    }

    @Cacheable(value = "recruiterAnalytics", key = "#recruiterEmail")
    public Analytics getAnalytics(String recruiterEmail) {
        List<Job> allJobs = jobClient.getJobs().getData();
        List<Job> recruiterJobs = allJobs.stream().filter(job -> job.getPostedBy().equals(recruiterEmail)).toList();

        int totalJobs = recruiterJobs.size();
        int totalApplications = 0;
        int shortlisted = 0;
        int offered = 0;
        int rejected = 0;

        for (Job job : recruiterJobs) {
            List<Application> applications = applicationClient.getByJobId(job.getJobId()).getData();
            totalApplications += applications.size();
            shortlisted += applications.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count();
            offered += applications.stream().filter(a -> a.getStatus() == ApplicationStatus.OFFERED).count();
            rejected += applications.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();
        }

        return new Analytics(totalJobs, totalApplications, shortlisted, offered, rejected);
    }

    @Cacheable(value = "adminAnalytics")
    public Analytics getAdminAnalytics() {
        List<Job> jobs = jobClient.getJobs().getData();
        System.out.println(jobs);
        int totalJobs = jobs.size();

        int totalApplications = 0;
        int shortlistedCount = 0;
        int offeredCount = 0;
        int rejectedCount = 0;

        for (Job job : jobs) {
            List<Application> applications = applicationClient.getByJobId(job.getJobId()).getData();
            System.out.println(applications);
            totalApplications += applications.size();
            shortlistedCount += applications.stream().filter(app -> app.getStatus().name().equals("SHORTLISTED")).count();
            offeredCount += applications.stream().filter(app -> app.getStatus().name().equals("OFFERED")).count();
            rejectedCount += applications.stream().filter(app -> app.getStatus().name().equals("REJECTED")).count();
        }

        return new Analytics(totalJobs, totalApplications, shortlistedCount, offeredCount, rejectedCount);
    }
}