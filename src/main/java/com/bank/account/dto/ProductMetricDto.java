package com.bank.account.dto;

import com.bank.account.model.AccountProductVariant;
import com.bank.account.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Aggregated product metrics for reporting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetricDto {

    private String product;
    private AccountType accountType;
    private AccountProductVariant variant;
    private long count;
    private long movementCount;
    private BigDecimal movementAmount;
    private BigDecimal commissions;
}
