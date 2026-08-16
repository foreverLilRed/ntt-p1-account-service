package com.bank.account.service.creation;

import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.CustomerDto;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.AccountType;
import com.bank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Validates account creation eligibility based on the customer type
 * (PERSONAL / BUSINESS), independently of the account product rules.
 */
@Component
@RequiredArgsConstructor
public class CustomerEligibilityValidator {

    private static final String PERSONAL = "PERSONAL";
    private static final String BUSINESS = "BUSINESS";

    private final AccountRepository accountRepository;

    /**
     * Validates whether the customer is allowed to open the requested account.
     *
     * @param customer customer projection from customer-service
     * @param request  create payload
     * @return completion signal, or error when an eligibility rule is violated
     */
    public Mono<Void> validate(CustomerDto customer, AccountRequest request) {
        String type = customer.getCustomerType();
        AccountType accountType = request.getAccountType();

        if (PERSONAL.equalsIgnoreCase(type)) {
            return validatePersonal(customer.getId(), accountType)
                    .then(validatePersonalHoldersAndSigners(request));
        }

        if (BUSINESS.equalsIgnoreCase(type)) {
            return validateBusiness(accountType);
        }

        return Mono.error(new BusinessException(
                "Unsupported customer type: " + type, HttpStatus.BAD_REQUEST));
    }

    /**
     * Validates holder/signer cardinality rules according to the customer type.
     * Only PERSONAL customers are restricted; BUSINESS customers are unrestricted.
     *
     * @param customerType customer type (PERSONAL / BUSINESS)
     * @param request      account payload
     * @return completion signal, or error when a rule is violated
     */
    public Mono<Void> validateHoldersAndSigners(String customerType, AccountRequest request) {
        if (PERSONAL.equalsIgnoreCase(customerType)) {
            return validatePersonalHoldersAndSigners(request);
        }
        return Mono.empty();
    }

    //RP1-001
    private Mono<Void> validatePersonal(String customerId, AccountType accountType) {
        return accountRepository.countByCustomerIdAndAccountType(customerId, accountType)
                .flatMap(count -> {
                    if (count >= 1) {
                        return Mono.error(new BusinessException(
                                "Personal customer already has a " + accountType + " account",
                                HttpStatus.CONFLICT));
                    }
                    return Mono.<Void>empty();
                });
    }

    //RP1-002
    private Mono<Void> validateBusiness(AccountType accountType) {
        if (accountType == AccountType.SAVINGS || accountType == AccountType.FIXED_TERM) {
            return Mono.error(new BusinessException(
                    "Business customers cannot open SAVINGS or FIXED_TERM accounts",
                    HttpStatus.BAD_REQUEST));
        }
        return Mono.empty();
    }

    //RP1-003
    private Mono<Void> validatePersonalHoldersAndSigners(AccountRequest request) {
        List<String> holders = request.getHolders();
        if (holders != null && holders.size() > 1) {
            return Mono.error(new BusinessException(
                    "Personal customers cannot have more than one holder",
                    HttpStatus.BAD_REQUEST));
        }
        List<String> signers = request.getAuthorizedSigners();
        if (signers != null && !signers.isEmpty()) {
            return Mono.error(new BusinessException(
                    "Personal customers cannot have authorized signers",
                    HttpStatus.BAD_REQUEST));
        }
        return Mono.empty();
    }
}
