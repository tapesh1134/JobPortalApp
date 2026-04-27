package org.jobportal.notificationservice.service;

import jakarta.transaction.Transactional;
import org.jobportal.notificationservice.rabbitMQ.RabbitMQConfig;
import org.jobportal.notificationservice.entity.Notification;
import org.jobportal.notificationservice.repository.NotificationRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Value("${spring.mail.username}") // Reads your email from properties
    private String adminEmail;

    private final NotificationRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, RabbitTemplate rabbitTemplate, SimpMessagingTemplate messagingTemplate, EmailService emailService) {
        this.repository = notificationRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
    }

    // 1. Send to RabbitMQ Queue
    @Override
    public void sendNotification(Notification notification) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                notification
        );
    }

    // 2. Processed by RabbitListener (Consumer)
    @Override
    @Transactional
    public void processNotification(Notification notification) {
        Notification savedNote = repository.save(notification);
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + notification.getUserEmail(),
                savedNote
        );
        emailService.sendEmail(adminEmail, notification.getUserEmail(), notification.getMessage());
    }

    // Standard JPA methods
    @Override
    public void markAsRead(Long id) {
        repository.findById(id).ifPresent(n -> { n.setRead(true); repository.save(n); });
    }

    @Override
    public List<Notification> getByUser(String email) {
        return repository.findByUserEmailOrderByCreatedAtDesc(email);
    }

    @Override
    public List<Notification> getUnreadByUser(String email) {
        return repository.findByUserEmailAndIsReadFalse(email);
    }

    @Override
    public Long getUnReadCount(String email) {
        return repository.countByUserEmailAndIsReadFalse(email);
    }

    @Override
    public void markAllRead(String userEmail) {
        List<Notification> unread = repository.findByUserEmailAndIsReadFalse(userEmail);
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
    }

    @Override
    public void deleteNotification(Long id) { repository.deleteById(id); }
}