package org.jobportal.notificationservice.service;

import jakarta.transaction.Transactional;
import org.jobportal.notificationservice.rabbitMQ.RabbitMQConfig;
import org.jobportal.notificationservice.entity.Notification;
import org.jobportal.notificationservice.repository.NotificationRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService{
    @Value("${spring.mail.username}")
    private String adminEmail;
    public static final String USER_NOTIFICATIONS = "user_notifications";
    public static final String UNREAD_NOTIFICATIONS = "unread_notifications";
    public static final String UNREAD_COUNT = "unread_count";

    private final NotificationRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final CacheManager cacheManager;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, RabbitTemplate rabbitTemplate, SimpMessagingTemplate messagingTemplate, EmailService emailService, CacheManager cacheManager) {
        this.repository = notificationRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
        this.cacheManager = cacheManager;
    }

    @Override
    public void sendNotification(Notification notification) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, RabbitMQConfig.NOTIFICATION_ROUTING_KEY, notification);
    }

    @Override
    @Transactional
    public void processNotification(Notification notification) {
        Notification savedNote = repository.save(notification);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend(
                        "/topic/notifications/" + savedNote.getUserEmail(),
                        savedNote
                );
            }
        });
        emailService.sendEmail(adminEmail, notification.getUserEmail(), notification.getMessage());
    }

    @Override
    public void markAsRead(Long id) {
        repository.findById(id).ifPresent(n -> {
            n.setRead(true);
            repository.save(n);
            evictUserCaches(n.getUserEmail());
        });
    }

    @Override
    @Cacheable(value = USER_NOTIFICATIONS, key = "#email")
    public List<Notification> getByUser(String email) {
        return repository.findByUserEmailOrderByCreatedAtDesc(email);
    }

    @Override
    @Cacheable(value = UNREAD_NOTIFICATIONS, key = "#email")
    public List<Notification> getUnreadByUser(String email) {
        return repository.findByUserEmailAndIsReadFalse(email);
    }

    @Override
    @Cacheable(value = UNREAD_COUNT, key = "#email")
    public Long getUnReadCount(String email) {
        return repository.countByUserEmailAndIsReadFalse(email);
    }

    @Override
    public void markAllRead(String userEmail) {
        List<Notification> unread = repository.findByUserEmailAndIsReadFalse(userEmail);
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
        evictUserCaches(userEmail);
    }

    @Override
    public void deleteNotification(Long id) {
        repository.findById(id).ifPresent(n -> {
            repository.deleteById(id);
            evictUserCaches(n.getUserEmail());
        });
    }

    private void evictUserCaches(String email) {
        cacheManager.getCache(USER_NOTIFICATIONS).evict(email);
        cacheManager.getCache(UNREAD_NOTIFICATIONS).evict(email);
        cacheManager.getCache(UNREAD_COUNT).evict(email);
    }
}