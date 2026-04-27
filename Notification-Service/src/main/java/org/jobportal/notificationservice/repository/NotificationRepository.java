package org.jobportal.notificationservice.repository;

import org.jobportal.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String email);
    List<Notification> findByUserEmailAndIsReadFalse(String email);
    List<Notification> findByUserEmail(String email);
    Long countByUserEmailAndIsReadFalse(String email);
};
