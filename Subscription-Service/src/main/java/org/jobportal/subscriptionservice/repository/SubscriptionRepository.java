package org.jobportal.subscriptionservice.repository;

import org.jobportal.subscriptionservice.entity.Subscription;
import org.jobportal.subscriptionservice.entity.SubscriptionPlan;
import org.jobportal.subscriptionservice.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<List<Subscription>> findByRecruiterEmail(String email);
    Optional<List<Subscription>> findByStatus(SubscriptionStatus status);
    Optional<Subscription> findActiveByRecruiterEmail(String recruiterEmail);
    boolean existsByRecruiterEmailAndStatus(String recruiterEmail, SubscriptionStatus status);
    long countByPlan(SubscriptionPlan plan);
}
