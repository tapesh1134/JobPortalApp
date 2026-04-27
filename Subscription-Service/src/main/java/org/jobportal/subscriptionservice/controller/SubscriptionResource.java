package org.jobportal.subscriptionservice.controller;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.jobportal.subscriptionservice.dto.ApiResponse;
import org.jobportal.subscriptionservice.dto.StripeResponseDto;
import org.jobportal.subscriptionservice.dto.SubscriptionRequestDto;
import org.jobportal.subscriptionservice.entity.*;
import org.jobportal.subscriptionservice.service.SubscriptionService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subscription")
public class SubscriptionResource {
    private final SubscriptionService subscriptionService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public SubscriptionResource(SubscriptionService subscriptionService, RabbitTemplate rabbitTemplate) {
        this.subscriptionService = subscriptionService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<StripeResponseDto>> subscribe(Authentication authentication, @RequestParam SubscriptionPlan plan) {
        String email = authentication.getName();

        // Build DTO using enum
        SubscriptionRequestDto dto = new SubscriptionRequestDto();
        dto.setName(plan.name());
        dto.setAmount(Math.round(plan.getPrice() * 100)); // already in paise
        dto.setCurrency("inr");
        dto.setQuantity(1L);

        StripeResponseDto response = subscriptionService.checkoutProducts(dto, email, plan);

        String message = "Your " + plan.name() + " subscription is now active.";
        message += " You have been charged ₹" + plan.getPrice() + ".";
        message += " You can now access all premium features.";
        Map<String, Object> recruiterNotification = new HashMap<>();
        recruiterNotification.put("userEmail", authentication.getName());
        recruiterNotification.put("type", "SUBSCRIPTION_DONE");
        recruiterNotification.put("message", message);
        recruiterNotification.put("isRead", false);

        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                recruiterNotification
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Stripe session created", response));
    }
//
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
//
//        try {
//            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
//
//            if ("checkout.session.completed".equals(event.getType())) {
//
//                Session session = (Session) event.getDataObjectDeserializer()
//                        .getObject()
//                        .orElse(null);
//
//                String email = session.getMetadata().get("email");
//                String planStr = session.getMetadata().get("plan");
//
//                SubscriptionPlan plan = SubscriptionPlan.valueOf(planStr);
//
//                subscriptionService.subscribe(email, plan); // ✅ MOVE HERE
//            }
//
//            return ResponseEntity.ok("Success");
//
//        } catch (Exception e) {
//            return ResponseEntity.status(400).body("Webhook Error");
//        }
//    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{subscriptionId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(@PathVariable Long subscriptionId, Authentication authentication) {
        subscriptionService.cancelSubscription(subscriptionId, authentication.getName());
        String message = "Your subscription has been cancelled.";
        message += " You will continue to have access until the end of the current billing period.";
        Map<String, Object> recruiterNotification = new HashMap<>();
        recruiterNotification.put("userEmail", authentication.getName());
        recruiterNotification.put("type", "SUBSCRIPTION_CANCELLED");
        recruiterNotification.put("message", message);
        recruiterNotification.put("isRead", false);

        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                recruiterNotification
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Subscription cancelled successfully"));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{subscriptionId}/renew")
    public ResponseEntity<ApiResponse<Subscription>> renewSubscription(@PathVariable Long subscriptionId, Authentication authentication) {
        Subscription subscription = subscriptionService.renewSubscription(subscriptionId, authentication.getName());
        String message = "Your subscription has been renewed.";
        Map<String, Object> recruiterNotification = new HashMap<>();
        recruiterNotification.put("userEmail", authentication.getName());
        recruiterNotification.put("type", "SUBSCRIPTION_RENEWED");
        recruiterNotification.put("message", message);
        recruiterNotification.put("isRead", false);

        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "notification_routing_key",
                recruiterNotification
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Subscription renewed successfully", subscription));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/recruiter")
    public ResponseEntity<ApiResponse<List<Subscription>>> getByRecruiterEmail(Authentication authentication) {
        List<Subscription> subscriptions = subscriptionService.getByRecruiterEmail(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Subscriptions fetched successfully", subscriptions));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<ApiResponse<Subscription>> getById(@PathVariable Long subscriptionId) {
        Subscription subscription = subscriptionService.getBySubscriptionId(subscriptionId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Subscription fetched successfully", subscription));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<List<Invoice>>> getInvoices(Authentication authentication) {
        List<Invoice> invoices = subscriptionService.getInvoices(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Invoices fetched successfully", invoices));
    }

    @GetMapping("/invoices/latest")
    public ResponseEntity<ApiResponse<Invoice>> getLatestInvoice(Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Latest invoice fetched", subscriptionService.getLatestInvoice(authentication.getName())));
    }
}
