package org.ricramiel.coreapi.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.service.CardAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cardaccount")
public class CardAccountController {
    private final CardAccountService cardAccountService;

    @GetMapping
    public ResponseEntity<Page<CardAccount>> getUserCardAccounts(Long userId,Pageable pageable) {
        return ResponseEntity.ok(cardAccountService.getUserCardAccounts(userId,pageable));
    }

    @GetMapping
    public ResponseEntity<Boolean> checkAccountExists(Long accountId) {
        return ResponseEntity.ok(cardAccountService.checkAccountExistance(accountId));
    }

    @PostMapping("/open")
    public ResponseEntity<CardAccount> openAccount(Long userId) {
        return ResponseEntity.ok(cardAccountService.createAccount(userId));
    }

    @PostMapping("/close")
    public ResponseEntity<Boolean> closeAccount(Long accountId) {
        return ResponseEntity.ok(cardAccountService.closeAccount(accountId));
    }
}
