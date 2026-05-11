package org.jobportal.notificationservice.controller;

import org.jobportal.notificationservice.entity.Notification;
import org.jobportal.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationResource {

    private final NotificationService notificationService;

    @Autowired
    public NotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getByUser(authentication.getName()));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getUnreadByUser(authentication.getName()));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getUnReadCount(authentication.getName()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        notificationService.markAllRead(authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}