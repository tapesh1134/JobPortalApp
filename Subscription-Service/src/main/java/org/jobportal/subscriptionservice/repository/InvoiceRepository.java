package org.jobportal.subscriptionservice.repository;

import org.jobportal.subscriptionservice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<List<Invoice>> findBySubscriptionID(Long subscriptionId);
    Optional<Invoice> findByTransactionId(String transactionId);
    Optional<List<Invoice>> findByRecruiterEmail(String recruiterEmail);
    Optional<Invoice> findTopByRecruiterEmailOrderByPaymentDateDesc(String recruiterEmail);
    boolean existsByStripeSessionId(String sessionId);
    Optional<Invoice> findByStripeSessionId(String sessionId);
}
