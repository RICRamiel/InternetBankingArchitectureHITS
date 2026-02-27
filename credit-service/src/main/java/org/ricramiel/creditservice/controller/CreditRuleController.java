package org.ricramiel.creditservice.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.dto.CreditRuleDTO;
import org.ricramiel.creditservice.model.CreditRule;
import org.ricramiel.creditservice.service.CreditRuleService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/credit_rule")
public class CreditRuleController {

    private final CreditRuleService creditRuleService;

    @PostMapping("/create")
    public ResponseEntity<CreditRule> createCreditRule(@RequestBody CreditRuleDTO creditRuleDTO){
        return ResponseEntity.ok(creditRuleService.createCreditRule(creditRuleDTO));
    }

    @PutMapping("/{creditRuleId}/edit")
    public ResponseEntity<CreditRule> editCreditRule(@RequestBody CreditRuleDTO creditRuleDTO, @PathVariable @Param("creditRuleId") UUID creditRuleId){
        return ResponseEntity.ok(creditRuleService.editCreditRule(creditRuleDTO, creditRuleId));
    }

    @DeleteMapping("/{creditRuleId}/delete")
    public void deleteCreditRule(@PathVariable @Param("creditRuleId") UUID creditRuleId){
        creditRuleService.deleteCreditRule(creditRuleId);
    }

    @GetMapping("/get_all")
    public ResponseEntity<List<CreditRule>> getAllCreditRules(){
        return ResponseEntity.ok(creditRuleService.getAllCreditRules());
    }

    @GetMapping("/{creditRuleId}/get_by_id")
    public ResponseEntity<CreditRule> getCreditRuleById(@PathVariable @Param("creditRuleId") UUID creditRuleId){
        return ResponseEntity.ok(creditRuleService.getCreditRuleById(creditRuleId));
    }
}
