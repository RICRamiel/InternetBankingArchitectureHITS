package org.ricramiel.coreapi.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.model.TransferRequest;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.service.CardAccountServiceImpl;
import org.ricramiel.coreapi.service.ExternalTransactionsService;
import org.ricramiel.coreapi.service.TransactionOperationServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionOperationController {
    private final TransactionOperationServiceImpl transactionOperationService;
    private final ExternalTransactionsService externalTransactionsService;

    @GetMapping("/{accountId}")
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
}
