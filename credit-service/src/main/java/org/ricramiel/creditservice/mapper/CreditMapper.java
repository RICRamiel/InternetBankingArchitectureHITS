package org.ricramiel.creditservice.mapper;

import org.ricramiel.creditservice.dto.CreditDTO;
import org.ricramiel.creditservice.model.Credit;

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

        return credit;
    }
}
