package com.bank.account.event;

import com.bank.account.service.DebtStatusStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Updates the local overdue-debt read-model from Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebtStatusConsumer {

    public static final String TOPIC = "credit.debt-status-changed";

    private final DebtStatusStore debtStatusStore;
    private final ObjectMapper objectMapper;

    /**
     * Consumes debt-status events.
     *
     * @param payload JSON event
     */
    @KafkaListener(topics = TOPIC, groupId = "account-service")
    public void onMessage(String payload) {
        try {
            DebtStatusChangedEvent event = objectMapper.readValue(payload, DebtStatusChangedEvent.class);
            debtStatusStore.save(event.getCustomerId(), event.isHasOverdueDebt()).block();
            log.info("Updated debt status for customerId={}", event.getCustomerId());
        } catch (Exception ex) {
            log.error("Failed to consume debt status event", ex);
        }
    }
}
