package org.ricramiel.coreapi.service;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.client.CurrencyClient;
import org.ricramiel.coreapi.dto.CurrencyClientDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CurrencyCacheService {

    private final CurrencyClient currencyClient;
    //получение списка валют и его кэширование
    @Cacheable(value = "currencies", unless = "#result == null")
    public CurrencyClientDto getCurrencyCourses() {
        return currencyClient.getCurrencies();
    }
}
