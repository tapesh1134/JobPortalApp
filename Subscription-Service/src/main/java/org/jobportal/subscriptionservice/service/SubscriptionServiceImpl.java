package org.jobportal.subscriptionservice.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.jobportal.subscriptionservice.dto.StripeResponseDto;
import org.jobportal.subscriptionservice.dto.SubscriptionRequestDto;
import org.jobportal.subscriptionservice.entity.*;
import org.jobportal.subscriptionservice.repository.InvoiceRepository;
import org.jobportal.subscriptionservice.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    @Autowired
    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, InvoiceRepository invoiceRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public Subscription subscribe(String recruiterEmail, SubscriptionPlan subscriptionPlan) {
        if (subscriptionRepository.existsByRecruiterEmailAndStatus(recruiterEmail, SubscriptionStatus.SUBSCRIBED)) {
            throw new RuntimeException("Active subscription already exists");
        }

        LocalDate today = LocalDate.now();
        Subscription subscription = Subscription.builder()
                .recruiterEmail(recruiterEmail)
                .plan(subscriptionPlan)
                .startDate(today)
                .endDate(today.plusMonths(subscriptionPlan.getDurationInMonths()))
                .amountPaid(subscriptionPlan.getPrice())
                .status(SubscriptionStatus.SUBSCRIBED)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
//        generateInvoice(saved.getSubscriptionId());
        return saved;
    }

    @Override
    public void cancelSubscription(Long subscriptionId, String recruiterEmail) {

        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (!subscription.getRecruiterEmail().equals(recruiterEmail)) {
            throw new RuntimeException("Unauthorized action");
        }

        subscription.setStatus(SubscriptionStatus.UNSUBSCRIBED);
        subscription.setEndDate(LocalDate.now());

        subscriptionRepository.save(subscription);
    }

    @Override
    public List<Subscription> getByRecruiterEmail(String recruiterEmail) {
        return subscriptionRepository.findByRecruiterEmail(recruiterEmail).orElse(null);
    }

    @Override
    public Subscription getBySubscriptionId(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId).orElseThrow(() -> new RuntimeException("Subscription not found"));
    }

    @Override
    public Subscription renewSubscription(Long subscriptionId, String recruiterEmail) {
        Subscription subscription = getBySubscriptionId(subscriptionId);

        if (!subscription.getRecruiterEmail().equals(recruiterEmail)) {
            throw new RuntimeException("Unauthorized action");
        }

        if (subscription.getStatus() != SubscriptionStatus.SUBSCRIBED) {
            throw new RuntimeException("Cannot renew inactive subscription");
        }

        SubscriptionPlan plan = subscription.getPlan();
        subscription.setEndDate(subscription.getEndDate().plusMonths(plan.getDurationInMonths()));
        subscription.setAmountPaid(plan.getPrice());
        subscription.setStatus(SubscriptionStatus.SUBSCRIBED);

        Subscription saved = subscriptionRepository.save(subscription);
//        generateInvoice(saved.getSubscriptionId());
        return saved;
    }

    @Override
    public Invoice generateInvoice(Long subscriptionId, String sessionId) {
        //  prevent duplicate invoice
        if (invoiceRepository.existsByStripeSessionId(sessionId)) {
            return invoiceRepository.findByStripeSessionId(sessionId).get();
        }
        Subscription subscription = getBySubscriptionId(subscriptionId);

        Invoice invoice = Invoice.builder()
                .subscriptionID(subscriptionId)
                .recruiterEmail(subscription.getRecruiterEmail())
                .amount(subscription.getAmountPaid())
                .paymentDate(LocalDateTime.now())
                .paymentMode(PaymentMode.CARD)
                .transactionId(UUID.randomUUID().toString())
                .stripeSessionId(sessionId)
                .build();
        return invoiceRepository.save(invoice);
    }

    @Override
    public List<Invoice> getInvoices(String recruiterEmail) {
        return invoiceRepository.findByRecruiterEmail(recruiterEmail).orElse(null);
    }

    @Override
    public Invoice getLatestInvoice(String recruiterEmail) {
        return invoiceRepository.findTopByRecruiterEmailOrderByPaymentDateDesc(recruiterEmail).orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    @Override
    public StripeResponseDto checkoutProducts(SubscriptionRequestDto dto, String email, SubscriptionPlan plan) {
        Stripe.apiKey = secretKey;
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                            // One-time payment (correct for dynamic pricing)
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl("http://localhost:8080")
                            .setCancelUrl("http://localhost:8080/")

                            // Important: link payment to user + plan
                            .putMetadata("email", email)
                            .putMetadata("plan", plan.name())
                            .addLineItem(SessionCreateParams.LineItem.builder()
                                            .setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1L)
                                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency(dto.getCurrency() != null
                                                                    ? dto.getCurrency().toLowerCase()
                                                                    : "inr")
                                                            .setUnitAmount(dto.getAmount()) // already in paise
                                                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName(dto.getName()).build()
                                                            ).build()
                                            ).build()
                            ).build();
            Session session = Session.create(params);
            Subscription subscription = subscribe(email, plan);
            generateInvoice(subscription.getSubscriptionId(), session.getId());
            return StripeResponseDto.builder().status("SUCCESS").message("Payment session created successfully").sessionId(session.getId()).sessionUrl(session.getUrl()).build();
        } catch (StripeException e) {
            e.printStackTrace();
            return StripeResponseDto.builder().status("FAILED").message("Error creating payment session: " + e.getMessage()).sessionId(null).sessionUrl(null).build();
        }
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
}