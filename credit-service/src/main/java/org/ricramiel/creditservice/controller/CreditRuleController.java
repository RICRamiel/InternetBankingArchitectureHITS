package org.ricramiel.creditservice.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.dto.CreditRuleAnswerDTO;
import org.ricramiel.creditservice.dto.CreditRuleDTO;
import org.ricramiel.creditservice.mapper.CreditRuleMapper;
import org.ricramiel.creditservice.model.CreditRule;
import org.ricramiel.creditservice.service.CreditRuleService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/credit_rule")
public class CreditRuleController {

    private final CreditRuleService creditRuleService;

    @PreAuthorize("hasRole('WORKER')")
    @PostMapping("/create")
    public ResponseEntity<CreditRuleAnswerDTO> createCreditRule(@RequestBody CreditRuleDTO creditRuleDTO){
        return ResponseEntity.ok(CreditRuleMapper.toAnswerDto(creditRuleService.createCreditRule(creditRuleDTO)));
    }

    @PreAuthorize("hasRole('WORKER')")
    @PutMapping("/{creditRuleId}/edit")
    public ResponseEntity<CreditRuleAnswerDTO> editCreditRule(
            @RequestBody CreditRuleDTO creditRuleDTO,
            @PathVariable("creditRuleId") @Param("creditRuleId") UUID creditRuleId){
        return ResponseEntity.ok(CreditRuleMapper.toAnswerDto(creditRuleService.editCreditRule(creditRuleDTO, creditRuleId)));
    }

    @PreAuthorize("hasRole('WORKER')")
    @DeleteMapping("/{creditRuleId}/delete")
    public void deleteCreditRule(@PathVariable("creditRuleId") @Param("creditRuleId") UUID creditRuleId){
        creditRuleService.deleteCreditRule(creditRuleId);
    }

    @GetMapping("/get_all")
    public ResponseEntity<List<CreditRuleAnswerDTO>> getAllCreditRules(){
        return ResponseEntity.ok(CreditRuleMapper.toListDto(creditRuleService.getAllCreditRules()));
    }

    @GetMapping("/{creditRuleId}/get_by_id")
    public ResponseEntity<CreditRuleAnswerDTO> getCreditRuleById(@PathVariable("creditRuleId") @Param("creditRuleId") UUID creditRuleId){
        return ResponseEntity.ok(CreditRuleMapper.toAnswerDto(creditRuleService.getCreditRuleById(creditRuleId)));
    }
}
