package com.bank.account.service;

import com.bank.account.dto.TransferRequest;
import com.bank.account.dto.TransferResponse;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.Account;
import com.bank.account.model.AccountStatus;
import com.bank.account.model.MovementType;
import com.bank.account.model.TransferType;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountService accountService;
    @Mock
    private AccountLedger accountLedger;

    @InjectMocks
    private TransferService transferService;

    @Test
    void rejectsSameAccount() {
        TransferRequest request = TransferRequest.builder()
                .sourceAccountId("a1")
                .destinationAccountId("a1")
                .amount(BigDecimal.TEN)
                .transferType(TransferType.OWN)
                .build();
        TestObserver<TransferResponse> observer = transferService.transfer(request).test();
        observer.assertError(BusinessException.class);
    }

    @Test
    void ownTransferSameCustomer() {
        Account source = account("a1", "c1");
        Account dest = account("a2", "c1");
        stubHappyPath(source, dest);
        TransferRequest request = TransferRequest.builder()
                .sourceAccountId("a1")
                .destinationAccountId("a2")
                .amount(BigDecimal.TEN)
                .transferType(TransferType.OWN)
                .build();
        TestObserver<TransferResponse> observer = transferService.transfer(request).test();
        observer.assertComplete();
        observer.assertValue(response -> response.getTransferType() == TransferType.OWN);
    }

    @Test
    void thirdPartyRequiresDifferentCustomers() {
        Account source = account("a1", "c1");
        Account dest = account("a2", "c1");
        when(accountService.findAccount("a1")).thenReturn(Mono.just(source));
        when(accountService.findAccount("a2")).thenReturn(Mono.just(dest));
        when(accountLedger.apply(any(), any(), any(), any())).thenReturn(Mono.just(source));
        TransferRequest request = TransferRequest.builder()
                .sourceAccountId("a1")
                .destinationAccountId("a2")
                .amount(BigDecimal.TEN)
                .transferType(TransferType.THIRD_PARTY)
                .build();
        transferService.transfer(request).test().assertError(BusinessException.class);
    }

    @Test
    void thirdPartyDifferentCustomersSucceeds() {
        Account source = account("a1", "c1");
        Account dest = account("a2", "c2");
        stubHappyPath(source, dest);
        TransferRequest request = TransferRequest.builder()
                .sourceAccountId("a1")
                .destinationAccountId("a2")
                .amount(BigDecimal.TEN)
                .transferType(TransferType.THIRD_PARTY)
                .build();
        transferService.transfer(request).test().assertComplete();
    }

    @Test
    void compensatesWhenDestinationCreditFails() {
        Account source = account("a1", "c1");
        Account dest = account("a2", "c2");
        when(accountService.findAccount("a1")).thenReturn(Mono.just(source));
        when(accountService.findAccount("a2")).thenReturn(Mono.just(dest));
        when(accountLedger.apply(eq(source), eq(BigDecimal.TEN), eq(MovementType.TRANSFER_OUT), any()))
                .thenReturn(Mono.just(source));
        when(accountLedger.apply(eq(dest), eq(BigDecimal.TEN), eq(MovementType.TRANSFER_IN), any()))
                .thenReturn(Mono.error(new BusinessException("fail", org.springframework.http.HttpStatus.BAD_REQUEST)));
        when(accountLedger.apply(eq(source), eq(BigDecimal.TEN), eq(MovementType.TRANSFER_IN), any()))
                .thenReturn(Mono.just(source));

        transferService.transfer(TransferRequest.builder()
                        .sourceAccountId("a1")
                        .destinationAccountId("a2")
                        .amount(BigDecimal.TEN)
                        .transferType(TransferType.THIRD_PARTY)
                        .build())
                .test()
                .assertError(BusinessException.class);
        verify(accountLedger, times(1))
                .apply(eq(source), eq(BigDecimal.TEN), eq(MovementType.TRANSFER_IN), any());
    }

    private void stubHappyPath(Account source, Account dest) {
        when(accountService.findAccount(source.getId())).thenReturn(Mono.just(source));
        when(accountService.findAccount(dest.getId())).thenReturn(Mono.just(dest));
        when(accountLedger.apply(eq(source), eq(BigDecimal.TEN), eq(MovementType.TRANSFER_OUT), any()))
                .thenReturn(Mono.just(source));
        when(accountLedger.apply(eq(dest), eq(BigDecimal.TEN), eq(MovementType.TRANSFER_IN), any()))
                .thenReturn(Mono.just(dest));
    }

    private Account account(String id, String customerId) {
        return Account.builder().id(id).customerId(customerId).status(AccountStatus.ACTIVE).build();
    }
}
