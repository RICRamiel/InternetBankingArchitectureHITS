package org.ricramiel.creditservice.service;

import org.ricramiel.creditservice.dto.CreditRuleDTO;
import org.ricramiel.creditservice.model.CreditRule;

import java.util.List;
import java.util.UUID;

public interface CreditRuleService {
    CreditRule createCreditRule(CreditRuleDTO creditRuleDTO);
    CreditRule editCreditRule(CreditRuleDTO creditRuleDTO, UUID creditRuleId);
    void deleteCreditRule(UUID creditRuleId);
    List<CreditRule> getAllCreditRules();
    CreditRule getCreditRuleById(UUID creditRuleId);
}
