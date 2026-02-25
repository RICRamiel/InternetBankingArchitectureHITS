package org.ricramiel.coreapi.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.service.CardAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cardaccount")
public class CardAccountController {
    private final CardAccountService cardAccountService;

    @GetMapping
    public ResponseEntity<Page<CardAccount>> getUserCardAccounts(UUID userId, Pageable pageable) {
        return ResponseEntity.ok(cardAccountService.getUserCardAccounts(userId,pageable));
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkAccountExists(@Param("accountId") UUID accountId) {
        return ResponseEntity.ok(cardAccountService.checkAccountExistance(accountId));
    }

    @PostMapping("/open")
    public ResponseEntity<CardAccount> openAccount(UUID userId) {
        return ResponseEntity.ok(cardAccountService.createAccount(userId));
    }

    @PostMapping("/close")
    public ResponseEntity<Boolean> closeAccount(UUID accountId) {
        return ResponseEntity.ok(cardAccountService.closeAccount(accountId));
    }
}
