package org.ricramiel.coreapi.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.dto.CurrencyClientDto;
import org.ricramiel.coreapi.dto.CurrencyDto;
import org.ricramiel.coreapi.service.CurrencyCacheService;
import org.ricramiel.coreapi.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/currency")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;
    private final CurrencyCacheService currencyCacheService;

    @GetMapping("/all")
    public CurrencyClientDto getCurrencyList() {
        return currencyCacheService.getCurrencyCourses();
    }

    @GetMapping("/{charcode}")
    public CurrencyDto getCurrencyByCharCode(@PathVariable String charcode) {
        return currencyService.getCurrencyCoursesByCharCode(charcode);
    }
}
