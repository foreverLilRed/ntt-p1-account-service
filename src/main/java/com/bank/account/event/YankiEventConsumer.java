package com.bank.account.event;

import com.bank.account.exception.BusinessException;
import com.bank.account.model.MovementType;
import com.bank.account.service.DebitCardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Handles Yanki requests without exposing REST to yanki-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YankiEventConsumer {

    public static final String LINK_REQUESTED = "yanki.debit-card-link-requested";
    public static final String LINK_RESULT = "yanki.debit-card-link-result";
    public static final String TOPUP_REQUESTED = "yanki.wallet-topup-requested";
    public static final String TOPUP_RESULT = "yanki.wallet-topup-result";
    public static final String WITHDRAW_REQUESTED = "yanki.wallet-withdraw-requested";
    public static final String WITHDRAW_RESULT = "yanki.wallet-withdraw-result";

    private final DebitCardService debitCardService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Validates a debit card for Yanki association.
     *
     * @param payload JSON event
     */
    @KafkaListener(topics = LINK_REQUESTED, groupId = "account-service")
    public void onLink(String payload) {
        YankiAccountEvent event = read(payload);
        try {
            String primary = debitCardService.findByCardNumber(event.getCardNumber()).block().getPrimaryAccountId();
            reply(LINK_RESULT, event, true, "Linked", primary);
        } catch (Exception ex) {
            reply(LINK_RESULT, event, false, messageOf(ex), null);
        }
    }

    /**
     * Credits the primary account (wallet withdraw to bank / receive).
     *
     * @param payload JSON event
     */
    @KafkaListener(topics = TOPUP_REQUESTED, groupId = "account-service")
    public void onTopup(String payload) {
        YankiAccountEvent event = read(payload);
        apply(event, MovementType.DEPOSIT, TOPUP_RESULT);
    }

    /**
     * Debits the primary account (wallet send funded by bank).
     *
     * @param payload JSON event
     */
    @KafkaListener(topics = WITHDRAW_REQUESTED, groupId = "account-service")
    public void onWithdraw(String payload) {
        YankiAccountEvent event = read(payload);
        apply(event, MovementType.WITHDRAWAL, WITHDRAW_RESULT);
    }

    private void apply(YankiAccountEvent event, MovementType type, String resultTopic) {
        try {
            BigDecimal amount = event.getAmount();
            debitCardService.applyOnPrimaryAccount(event.getCardNumber(), amount, type).block();
            reply(resultTopic, event, true, "Applied", null);
        } catch (Exception ex) {
            reply(resultTopic, event, false, messageOf(ex), null);
        }
    }

    private void reply(String topic, YankiAccountEvent source, boolean success, String message, String primary) {
        YankiAccountEvent result = YankiAccountEvent.builder()
                .correlationId(source.getCorrelationId())
                .walletId(source.getWalletId())
                .cardNumber(source.getCardNumber())
                .amount(source.getAmount())
                .success(success)
                .message(message)
                .primaryAccountId(primary)
                .build();
        try {
            kafkaTemplate.send(topic, source.getWalletId(), objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException ex) {
            log.error("Unable to serialize yanki result", ex);
        }
    }

    private YankiAccountEvent read(String payload) {
        try {
            return objectMapper.readValue(payload, YankiAccountEvent.class);
        } catch (Exception ex) {
            return YankiAccountEvent.builder().correlationId("unknown").message(ex.getMessage()).build();
        }
    }

    private String messageOf(Exception ex) {
        if (ex instanceof BusinessException business) {
            return business.getMessage();
        }
        if (ex.getCause() instanceof BusinessException business) {
            return business.getMessage();
        }
        return ex.getMessage();
    }
}
