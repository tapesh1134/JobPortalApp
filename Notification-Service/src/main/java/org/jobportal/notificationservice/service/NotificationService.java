package org.jobportal.notificationservice.service;

import org.jobportal.notificationservice.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Notification notification);
    void processNotification(Notification notification);
    void markAsRead(Long notificationId);
    void markAllRead(String userEmail);
    List<Notification> getByUser(String userEmail);
    List<Notification> getUnreadByUser(String userEmail);
    Long getUnReadCount(String userEmail);
    void deleteNotification(Long notificationId);
}
