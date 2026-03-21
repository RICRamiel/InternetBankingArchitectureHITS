package org.ricramiel.coreapi.service.implementation;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.dto.CurrencyClientDto;
import org.ricramiel.coreapi.dto.CurrencyConvertRequestDto;
import org.ricramiel.coreapi.dto.CurrencyConvertResponseDto;
import org.ricramiel.coreapi.dto.CurrencyDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyCacheService currencyCacheService;

    public CurrencyDto getCurrencyCoursesByCharCode(String code) {
        return currencyCacheService.getCurrencyCourses().getValuteList().get(code);
    }

    public CurrencyConvertResponseDto convertCurrency(CurrencyConvertRequestDto currencyConvertRequestDto) {

    }

    private BigDecimal calcRate(String fromCode, String toCode, CurrencyClientDto currencyClientDto) {
        return
    }
}
