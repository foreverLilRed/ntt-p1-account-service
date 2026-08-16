package com.bank.account.controller;

import com.bank.account.dto.TransferRequest;
import com.bank.account.dto.TransferResponse;
import com.bank.account.service.TransferService;
import io.reactivex.rxjava3.core.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for intra-bank transfers.
 */
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Own-account and third-party transfers")
public class TransferController {

    private final TransferService transferService;

    /**
     * Executes a transfer between two accounts of the same bank.
     *
     * @param request transfer payload
     * @return transfer result
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer between bank accounts")
    public Single<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}
