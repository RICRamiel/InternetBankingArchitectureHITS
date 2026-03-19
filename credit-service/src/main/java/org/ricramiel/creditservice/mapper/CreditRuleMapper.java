package org.ricramiel.creditservice.mapper;

import org.ricramiel.creditservice.dto.CreditRuleAnswerDTO;
import org.ricramiel.creditservice.dto.CreditRuleDTO;
import org.ricramiel.creditservice.model.CreditRule;

import java.util.List;
import java.util.stream.Collectors;

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

        return creditRule;
    }

    public static CreditRuleAnswerDTO toAnswerDto(CreditRule creditRule) {
        if (creditRule == null) {
            return null;
        }

        CreditRuleAnswerDTO creditRuleAnswerDTO = new CreditRuleAnswerDTO();
        creditRuleAnswerDTO.setId(creditRule.getId());
        creditRuleAnswerDTO.setRuleName(creditRule.getRuleName());
        creditRuleAnswerDTO.setOpeningDate(creditRule.getOpeningDate());
        creditRuleAnswerDTO.setPercentage(creditRule.getPercentage());
        creditRuleAnswerDTO.setCollectionPeriodSeconds(creditRule.getCollectionPeriodSeconds());

        return creditRuleAnswerDTO;
    }

    public static List<CreditRuleAnswerDTO> toListDto(List<CreditRule> creditRules) {
        if (creditRules == null) {
            return null;
        }

        return creditRules.stream().map(CreditRuleMapper::toAnswerDto).collect(Collectors.toList());
    }
}
