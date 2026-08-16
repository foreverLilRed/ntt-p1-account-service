package com.bank.account.controller;

import com.bank.account.dto.ProductMetricDto;
import com.bank.account.service.AccountService;
import io.reactivex.rxjava3.core.Observable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Reporting endpoints consumed by report-service.
 */
@RestController
@RequestMapping("/api/v1/account-analytics")
@RequiredArgsConstructor
@Tag(name = "Account Analytics", description = "Product report snapshots")
public class AccountAnalyticsController {

    private final AccountService accountService;

    /**
     * Returns product metrics for the given date range.
     *
     * @param from inclusive start date
     * @param to   inclusive end date
     * @return metrics stream
     */
    @GetMapping("/products")
    @Operation(summary = "Product report for accounts")
    public Observable<ProductMetricDto> products(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Instant start = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return accountService.productReport(start, end);
    }
}
