package org.ricramiel.coreapi.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.dto.CardAccountCreateDto;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.service.CardAccountServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cardaccount")
public class CardAccountController {
    private final CardAccountServiceImpl cardAccountService;

    @GetMapping("/all/{userId}")
    @PreAuthorize("hasRole('WORKER') or @accessChecker.isSelf(#userId)")
    public ResponseEntity<Page<CardAccount>> getUserCardAccounts(
            @PathVariable("userId") @Param("userId") UUID userId,
            @RequestParam(required = false, defaultValue = "0", name = "pageIndex") int pageIndex,
            @RequestParam(required = false, defaultValue = "30", name = "pageSize") int pageSize) {
        return ResponseEntity.ok(cardAccountService.getUserCardAccounts(userId, PageRequest.of(pageIndex, pageSize)));
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('WORKER') or @accessChecker.isCardAccountOwner(#accountId)")
    public ResponseEntity<CardAccount> getUserCardAccount(@PathVariable("accountId") @Param("accountId") UUID accountId) {
        return ResponseEntity.ok(cardAccountService.getAccountById(accountId));
    }

    @GetMapping("/exists/{accountId}")
    public ResponseEntity<Boolean> checkAccountExists(@PathVariable("accountId") @Param("accountId") UUID accountId) {
        return ResponseEntity.ok(cardAccountService.checkAccountExistance(accountId));
    }

    @PostMapping("/open/{userId}")
    @PreAuthorize("hasRole('WORKER') or @accessChecker.isSelf(#userId)")
    public ResponseEntity<CardAccount> openAccount(@PathVariable("userId") @Param("userId") UUID userId, @RequestBody CardAccountCreateDto dto) {
        return ResponseEntity.ok(cardAccountService.createAccount(userId, dto));
    }

    @PostMapping("/close/{accountId}")
    @PreAuthorize("hasRole('WORKER') or @accessChecker.isCardAccountOwner(#accountId)")
    public ResponseEntity<Boolean> closeAccount(@PathVariable("accountId") @Param("accountId") UUID accountId) {
        return ResponseEntity.ok(cardAccountService.closeAccount(accountId));
    }

    @PostMapping("/{accountId}/set-main")
    public ResponseEntity<CardAccount> setMainAccount(@PathVariable("accountId") @Param("accountId") UUID accountId){
        return ResponseEntity.ok(cardAccountService.setMainAccount(accountId));
    }
}
