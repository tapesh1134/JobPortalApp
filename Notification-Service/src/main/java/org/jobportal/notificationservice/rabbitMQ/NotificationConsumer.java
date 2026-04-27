package org.jobportal.notificationservice.rabbitMQ;

import org.jobportal.notificationservice.entity.Notification;
import org.jobportal.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationConsumer.class);

    @Autowired
    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "notification_queue")
    public void consumeNotification(Map<String, Object> data) {
        LOGGER.info(String.format("Received notification %s", data.toString()));

        // Convert Map to Entity
        Notification notification = new Notification();
        notification.setUserEmail((String) data.get("userEmail"));
        notification.setType((String) data.get("type"));
        notification.setMessage((String) data.get("message"));
        notification.setRead(false);

        // This method saves to DB, sends WebSocket, and sends Email
        notificationService.processNotification(notification);
    }
}