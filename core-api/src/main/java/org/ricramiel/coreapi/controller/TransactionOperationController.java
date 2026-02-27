package org.ricramiel.coreapi.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.service.CardAccountService;
import org.ricramiel.coreapi.service.TransactionOperationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public void enrollMoney(@RequestBody EnrollDto enrollDto) {
        cardAccountService.enroll(enrollDto);
    }

    @PostMapping("/withdraw")
    public void withdrawMoney(@RequestBody WithdrawDto withdrawDto) {
        cardAccountService.withdraw(withdrawDto);
    }
}
