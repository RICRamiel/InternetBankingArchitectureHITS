package org.ricramiel.coreapi.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.service.CardAccountService;
import org.ricramiel.coreapi.service.TransactionOperationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionOperationController {
    private final TransactionOperationService transactionOperationService;
    private final CardAccountService cardAccountService;

    @GetMapping("/{accountId}")
    public ResponseEntity<Page<TransactionOperation>> getTransactionOperations(
            @PathVariable("accountId") @Param("accountId") UUID accountId,
            @RequestParam(required = false, defaultValue = "0", name = "pageIndex") int pageIndex,
            @RequestParam(required = false, defaultValue = "30", name = "pageSize") int pageSize) {
        return ResponseEntity.ok(transactionOperationService.findByAccountId(accountId, PageRequest.of(pageIndex, pageSize)));
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
