package org.jobportal.subscriptionservice.service;

import org.jobportal.subscriptionservice.dto.StripeResponseDto;
import org.jobportal.subscriptionservice.dto.SubscriptionRequestDto;
import org.jobportal.subscriptionservice.entity.Invoice;
import org.jobportal.subscriptionservice.entity.Subscription;
import org.jobportal.subscriptionservice.entity.SubscriptionPlan;

import java.util.List;

public interface SubscriptionService {
    Subscription subscribe(String recruiterEmail, SubscriptionPlan subscriptionPlan);
    void cancelSubscription(Long subscriptionId, String recruiterEmail);
    List<Subscription> getByRecruiterEmail(String recruiterEmail);
    Subscription getBySubscriptionId(Long subscriptionId);
    Subscription renewSubscription(Long subscriptionId, String recruiterEmail);
    Invoice generateInvoice(Long subscriptionId, String sessionId);
    List<Invoice> getInvoices(String recruiterEmail);
    Invoice getLatestInvoice(String recruiterEmail);
    StripeResponseDto checkoutProducts(SubscriptionRequestDto dto, String email, SubscriptionPlan plan);
    List<Invoice> getAllInvoices();
}