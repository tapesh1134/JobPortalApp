package org.jobportal.subscriptionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequestDto {
    private Long amount;
    private Long quantity;
    private String name;
    private String currency;
}
