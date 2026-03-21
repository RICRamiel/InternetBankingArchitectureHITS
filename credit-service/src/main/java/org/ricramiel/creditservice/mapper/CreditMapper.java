package org.ricramiel.creditservice.mapper;

import org.ricramiel.creditservice.dto.CreditAnswerDTO;
import org.ricramiel.creditservice.dto.CreditDTO;
import org.ricramiel.creditservice.model.Credit;

import java.util.List;
import java.util.stream.Collectors;

public class CreditMapper {

    public static Credit toCredit(CreditDTO creditDTO) {
        if (creditDTO == null) {
            return null;
        }

        Credit credit = new Credit();
        credit.setCreditRule(creditDTO.getCreditRule());
        credit.setCurrentDebtSum(creditDTO.getTotalDebt());
        credit.setInitialDebt(creditDTO.getTotalDebt());
        credit.setUserId(creditDTO.getUserId());
        credit.setCardAccount(creditDTO.getCardAccount());
        credit.setCurrency(creditDTO.getCurrency());

        return credit;
    }

    public static CreditAnswerDTO toAnswerDto(Credit credit){
        if(credit == null){
            return null;
        }

        CreditAnswerDTO creditAnswerDTO = new CreditAnswerDTO();
        creditAnswerDTO.setId(credit.getId());
        creditAnswerDTO.setLastInterestUpdate(credit.getLastInterestUpdate());
        creditAnswerDTO.setUserId(credit.getUserId());
        creditAnswerDTO.setCardAccount(credit.getCardAccount());
        creditAnswerDTO.setCurrentDebtSum(credit.getCurrentDebtSum());
        creditAnswerDTO.setInitialDebt(credit.getInitialDebt());
        creditAnswerDTO.setCurrency(credit.getCurrency());
        creditAnswerDTO.setInterestDebtSum(credit.getInterestDebtSum());
        creditAnswerDTO.setCreditRule(CreditRuleMapper.toAnswerDto(credit.getCreditRule()));

        return creditAnswerDTO;
    }

    public static List<CreditAnswerDTO> toListDto(List<Credit> credits){
        if(credits == null){
            return null;
        }

        return credits.stream().map(CreditMapper::toAnswerDto).collect(Collectors.toList());
    }
}
