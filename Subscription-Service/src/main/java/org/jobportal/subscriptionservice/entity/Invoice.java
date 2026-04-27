package org.jobportal.subscriptionservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceID;
    @Column(nullable = false, unique = true)
    private Long subscriptionID;
    @Column(nullable = false)
    private String recruiterEmail;
    @Column(nullable = false)
    private double amount;
    private LocalDateTime paymentDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode;
    private String transactionId;
    private String stripeSessionId;
}