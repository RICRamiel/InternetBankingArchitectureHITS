package org.ricramiel.coreapi.service;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.dto.CurrencyClientDto;
import org.ricramiel.coreapi.dto.CurrencyConvertRequestDto;
import org.ricramiel.coreapi.dto.CurrencyConvertResponseDto;
import org.ricramiel.coreapi.dto.CurrencyDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyCacheService currencyCacheService;

    public CurrencyDto getCurrencyCoursesByCharCode(String code) {
        return currencyCacheService.getCurrencyCourses().getValuteList().get(code);
    }

    public CurrencyConvertResponseDto convertCurrency(CurrencyConvertRequestDto currencyConvertRequestDto) {
        return new CurrencyConvertResponseDto(
                currencyConvertRequestDto.getFromCurrency(),
                currencyConvertRequestDto.getToCurrency(),
                currencyConvertRequestDto.getAmount(),
                currencyConvertRequestDto.getAmount()
                        .multiply(calcRate(
                                currencyConvertRequestDto.getFromCurrency(),
                                currencyConvertRequestDto.getToCurrency(),
                                currencyCacheService.getCurrencyCourses()
                        )));
    }

    private BigDecimal calcRate(String fromCode, String toCode, CurrencyClientDto currencyClientDto) {
        if (fromCode.equalsIgnoreCase(toCode)) {
            return BigDecimal.ONE;
        }

        BigDecimal fromRate = getRateToRub(fromCode, currencyClientDto);
        BigDecimal toRate = getRateToRub(toCode, currencyClientDto);

        return fromRate.divide(toRate, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal getRateToRub(String code, CurrencyClientDto currencyClientDto) {
        if ("RUB".equalsIgnoreCase(code)) {
            return BigDecimal.ONE;
        }

        // Получаем данные о валюте
        CurrencyDto currency = currencyClientDto.getValuteList().get(code.toUpperCase());
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не найдена: " + code);
        }

        return currency.getCurrencyCourse()
                .divide(BigDecimal.valueOf(currency.getCurrencyNominal()), 6, RoundingMode.HALF_UP);
    }
}
