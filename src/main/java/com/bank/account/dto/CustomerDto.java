package com.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal customer projection obtained from customer-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private String id;
    private String customerType;
    private String fullName;
}
