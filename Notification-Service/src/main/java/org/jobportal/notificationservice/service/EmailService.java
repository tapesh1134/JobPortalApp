package org.jobportal.notificationservice.service;

public interface EmailService {
    void sendEmail(String from, String to, String message);
}