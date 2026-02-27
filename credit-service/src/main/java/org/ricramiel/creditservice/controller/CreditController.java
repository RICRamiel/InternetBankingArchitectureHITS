package org.ricramiel.creditservice.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.dto.CreditDTO;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.service.CreditService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/credit")
public class CreditController {

    private final CreditService creditService;

    @PostMapping("/create")
    public ResponseEntity<Credit> createCredit(@RequestBody CreditDTO creditDTO){
        return ResponseEntity.ok(creditService.createCredit(creditDTO));
    }

    @DeleteMapping("/{creditId}/delete")
    public void deleteCredit(@PathVariable("creditId") @Param("creditId") UUID creditId){
        creditService.deleteCredit(creditId);
    }

    @GetMapping("/{userId}/get_by_user_id")
    public ResponseEntity<List<Credit>> getByUserId(@PathVariable("userId") @Param("userId") UUID userId){
        return ResponseEntity.ok(creditService.getByUserId(userId));
    }

    @GetMapping("/{cardAccountId}/get_by_card_account")
    public ResponseEntity<Credit> getByCardAccountId(@PathVariable("cardAccountId") @Param("cardAccountId") UUID cardAccountId){
        return ResponseEntity.ok(creditService.getByCardAccountId(cardAccountId));
    }

    @PostMapping("/{cardAccountId}/enrollment")
    public ResponseEntity<Credit> makeEnrollment(@PathVariable("cardAccountId") @Param("cardAccountId") UUID cardAccountId,@RequestParam BigDecimal money){
        return ResponseEntity.ok(creditService.makeEnrollment(cardAccountId, money));
    }
}
