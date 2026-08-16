package com.bank.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Redis read-model of overdue debt consumed from Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebtStatusStore {

    static final String KEY_PREFIX = "debt-status:";
    private static final Duration TTL = Duration.ofHours(24);

    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Persists the overdue flag.
     *
     * @param customerId customer
     * @param overdue    flag
     * @return completion
     */
    public Mono<Void> save(String customerId, boolean overdue) {
        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + customerId, Boolean.toString(overdue), TTL)
                .then()
                .onErrorResume(error -> {
                    log.warn("Unable to persist debt status: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Reads the overdue flag.
     *
     * @param customerId customer
     * @return true when overdue
     */
    public Mono<Boolean> hasOverdueDebt(String customerId) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + customerId)
                .map(Boolean::parseBoolean)
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }
}
