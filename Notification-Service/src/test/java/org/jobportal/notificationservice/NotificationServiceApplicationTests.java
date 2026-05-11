package org.jobportal.notificationservice;

import org.jobportal.notificationservice.entity.Notification;
import org.jobportal.notificationservice.rabbitMQ.RabbitMQConfig;
import org.jobportal.notificationservice.repository.NotificationRepository;
import org.jobportal.notificationservice.service.EmailService;
import org.jobportal.notificationservice.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.cache.type=none"})
class NotificationServiceApplicationTests {

    @Autowired
    private NotificationServiceImpl notificationService;

    @MockitoBean
    private NotificationRepository repository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private Cache mockCache;

    private Notification testNotification;
    private final String userEmail = "test@user.com";

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setNotificationId(1L);
        testNotification.setUserEmail(userEmail);
        testNotification.setMessage("Test Message");
        testNotification.setRead(false);
        when(cacheManager.getCache(anyString())).thenReturn(mockCache);
    }

    @Test
    void contextLoads() {
        assertNotNull(notificationService);
    }

    @Test
    void testSendNotification_ShouldCallRabbitTemplate() {
        notificationService.sendNotification(testNotification);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                eq(testNotification)
        );
    }

    @Test
    void testProcessNotification_ShouldSaveAndSendEmail() {
        when(repository.save(any(Notification.class))).thenReturn(testNotification);
        notificationService.processNotification(testNotification);

        verify(repository, times(1)).save(testNotification);
        verify(emailService, times(1)).sendEmail(any(), eq(userEmail), anyString());
    }

    @Test
    void testMarkAsRead_ShouldUpdateDatabaseAndEvictCache() {
        when(repository.findById(1L)).thenReturn(Optional.of(testNotification));

        notificationService.markAsRead(1L);
        assertTrue(testNotification.isRead());
        verify(repository, times(1)).save(testNotification);
        verify(cacheManager, times(3)).getCache(anyString());
        verify(mockCache, times(3)).evict(userEmail);
    }

    @Test
    void testGetByUser_ShouldReturnList() {
        when(repository.findByUserEmailOrderByCreatedAtDesc(userEmail)).thenReturn(List.of(testNotification));

        List<Notification> result = notificationService.getByUser(userEmail);

        assertFalse(result.isEmpty());
        assertEquals(userEmail, result.get(0).getUserEmail());
        verify(repository, times(1)).findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    @Test
    void testGetUnReadCount_ShouldReturnCount() {
        when(repository.countByUserEmailAndIsReadFalse(userEmail)).thenReturn(5L);

        Long count = notificationService.getUnReadCount(userEmail);

        assertEquals(5L, count);
        verify(repository, times(1)).countByUserEmailAndIsReadFalse(userEmail);
    }

    @Test
    void testMarkAllRead_ShouldUpdateAllUnread() {
        Notification n1 = new Notification(); n1.setRead(false); n1.setUserEmail(userEmail);
        Notification n2 = new Notification(); n2.setRead(false); n2.setUserEmail(userEmail);
        when(repository.findByUserEmailAndIsReadFalse(userEmail)).thenReturn(List.of(n1, n2));

        notificationService.markAllRead(userEmail);

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(repository, times(1)).saveAll(any());
        verify(mockCache, atLeastOnce()).evict(userEmail);
    }

    @Test
    void testDeleteNotification_ShouldInvokeRepository() {
        when(repository.findById(1L)).thenReturn(Optional.of(testNotification));

        notificationService.deleteNotification(1L);

        verify(repository, times(1)).deleteById(1L);
        verify(mockCache, atLeastOnce()).evict(userEmail);
    }
}