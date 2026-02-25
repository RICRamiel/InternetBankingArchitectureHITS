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
        credit.setDebt(creditDTO.getTotalDebt());
        credit.setTotalDebt(creditDTO.getTotalDebt());
        credit.setUserId(creditDTO.getUserId());
        credit.setCardAccount(creditDTO.getCardAccount());

        return credit;
    }
}
