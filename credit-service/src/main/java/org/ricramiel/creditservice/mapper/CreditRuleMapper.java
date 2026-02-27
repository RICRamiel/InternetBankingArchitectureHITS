package org.ricramiel.creditservice.mapper;

import org.ricramiel.creditservice.dto.CreditRuleDTO;
import org.ricramiel.creditservice.model.CreditRule;

public class CreditRuleMapper {
    public static CreditRule toCreditRule(CreditRuleDTO creditRuleDTO) {
        if (creditRuleDTO == null) {
            return null;
        }

        CreditRule creditRule = new CreditRule();
        creditRule.setRuleName(creditRuleDTO.getRuleName());
        creditRule.setPercentage(creditRuleDTO.getPercentage());
        creditRule.setCollectionPeriodSeconds(creditRuleDTO.getCollectionPeriodSeconds());
        creditRule.setOpeningDate(creditRuleDTO.getOpeningDate());
        creditRule.setPercentageStrategy(creditRuleDTO.getPercentageStrategy());

        return creditRule;
    }
}
