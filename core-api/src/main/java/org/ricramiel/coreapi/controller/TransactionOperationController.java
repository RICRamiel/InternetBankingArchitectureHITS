package org.ricramiel.coreapi.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.service.CardAccountService;
import org.ricramiel.coreapi.service.TransactionOperationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionOperationController {
    private final TransactionOperationService transactionOperationService;
    private final CardAccountService cardAccountService;

    @GetMapping()
    public ResponseEntity<Page<TransactionOperation>> getTransactionOperations(UUID userId, Pageable pageable) {
        return ResponseEntity.ok(transactionOperationService.findByAccountId(userId, pageable));
    }

    @PostMapping("/enroll")
    public void EnrollMoney(UUID accountId, BigDecimal amount) {
        cardAccountService.enroll(accountId, amount);
    }

    @PostMapping("/withdraw")
    public void WithdrawMoney(UUID accountId, BigDecimal amount) {
        cardAccountService.withdraw(accountId, amount);
    }
}
