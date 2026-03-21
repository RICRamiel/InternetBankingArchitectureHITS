package org.ricramiel.coreapi.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.model.TransferCurrencyRequest;
import org.ricramiel.coreapi.model.TransferRequest;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.service.ExternalTransactionsService;
import org.ricramiel.coreapi.service.TransactionOperationServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionOperationController {
    private final TransactionOperationServiceImpl transactionOperationService;
    private final ExternalTransactionsService externalTransactionsService;

    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('WORKER') Or @accessChecker.isCardAccountOwner(#accountId)")
    public ResponseEntity<Page<TransactionOperation>> getTransactionOperations(
            @PathVariable("accountId") @Param("accountId") UUID accountId,
            @RequestParam(required = false, defaultValue = "0", name = "pageIndex") int pageIndex,
            @RequestParam(required = false, defaultValue = "30", name = "pageSize") int pageSize) {
        return ResponseEntity.ok(transactionOperationService.findByAccountId(accountId, PageRequest.of(pageIndex, pageSize)));
    }

    @PostMapping("/transfer")
    public void transferMoney(@RequestBody TransferRequest dto) {
        externalTransactionsService.transfer(dto);
    }

    @PostMapping("/convert")
    public void convertCurrency(@RequestBody TransferCurrencyRequest dto) {
        externalTransactionsService.transfer(dto);
    }
}
