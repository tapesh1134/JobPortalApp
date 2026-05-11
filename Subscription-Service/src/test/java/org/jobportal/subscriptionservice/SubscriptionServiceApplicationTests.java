package org.jobportal.subscriptionservice;

import org.jobportal.subscriptionservice.entity.*;
import org.jobportal.subscriptionservice.repository.InvoiceRepository;
import org.jobportal.subscriptionservice.repository.SubscriptionRepository;
import org.jobportal.subscriptionservice.service.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.cache.type=none"})
class SubscriptionServiceApplicationTests {

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    @MockitoBean
    private SubscriptionRepository subscriptionRepository;

    @MockitoBean
    private InvoiceRepository invoiceRepository;

    private final String recruiterEmail = "recruiter@test.com";
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        testSubscription = Subscription.builder()
                .subscriptionId(1L)
                .recruiterEmail(recruiterEmail)
                .plan(SubscriptionPlan.MONTHLY)
                .status(SubscriptionStatus.SUBSCRIBED)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .amountPaid(999.0)
                .build();
    }

    @Test
    void contextLoads() {
        assertNotNull(subscriptionService);
    }

    @Test
    void testSubscribe_Success() {
        when(subscriptionRepository.existsByRecruiterEmailAndStatus(recruiterEmail, SubscriptionStatus.SUBSCRIBED)).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        Subscription result = subscriptionService.subscribe(recruiterEmail, SubscriptionPlan.HALF_YEARLY);
        assertNotNull(result);
        assertEquals(SubscriptionStatus.SUBSCRIBED, result.getStatus());
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testSubscribe_AlreadyActive_ShouldThrowException() {
        when(subscriptionRepository.existsByRecruiterEmailAndStatus(recruiterEmail, SubscriptionStatus.SUBSCRIBED)).thenReturn(true);
        assertThrows(RuntimeException.class, () -> subscriptionService.subscribe(recruiterEmail, SubscriptionPlan.FULL_YEAR));
    }

    @Test
    void testCancelSubscription_Success() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        subscriptionService.cancelSubscription(1L, recruiterEmail);
        assertEquals(SubscriptionStatus.UNSUBSCRIBED, testSubscription.getStatus());
        verify(subscriptionRepository, times(1)).save(testSubscription);
    }

    @Test
    void testCancelSubscription_UnauthorizedUser_ShouldThrowException() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        assertThrows(RuntimeException.class, () -> subscriptionService.cancelSubscription(1L, "wrong@email.com"));
    }

    @Test
    void testRenewSubscription_Success() {
        LocalDate oldEndDate = testSubscription.getEndDate();
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);
        Subscription renewed = subscriptionService.renewSubscription(1L, recruiterEmail);
        assertTrue(renewed.getEndDate().isAfter(oldEndDate));
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testGenerateInvoice_PreventDuplicate() {
        String sessionId = "sess_123";
        Invoice existingInvoice = new Invoice();
        when(invoiceRepository.existsByStripeSessionId(sessionId)).thenReturn(true);
        when(invoiceRepository.findByStripeSessionId(sessionId)).thenReturn(Optional.of(existingInvoice));
        Invoice result = subscriptionService.generateInvoice(1L, sessionId);
        assertEquals(existingInvoice, result);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testGenerateInvoice_NewInvoice_Success() {
        String sessionId = "sess_999";
        when(invoiceRepository.existsByStripeSessionId(sessionId)).thenReturn(false);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);
        Invoice result = subscriptionService.generateInvoice(1L, sessionId);
        assertNotNull(result);
        assertEquals(sessionId, result.getStripeSessionId());
        assertEquals(recruiterEmail, result.getRecruiterEmail());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testGetInvoices_ShouldReturnList() {
        when(invoiceRepository.findByRecruiterEmail(recruiterEmail)).thenReturn(Optional.of(List.of(new Invoice(), new Invoice())));
        List<Invoice> results = subscriptionService.getInvoices(recruiterEmail);
        assertEquals(2, results.size());
    }

    @Test
    void testGetLatestInvoice_NotFound_ShouldThrowException() {
        when(invoiceRepository.findTopByRecruiterEmailOrderByPaymentDateDesc(recruiterEmail)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> subscriptionService.getLatestInvoice(recruiterEmail));
    }
}