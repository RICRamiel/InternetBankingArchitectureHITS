package org.ricramiel.coreapi.client;

import org.ricramiel.coreapi.config.FeignConfig;
import org.ricramiel.coreapi.dto.CurrencyClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "currency-client", url = "${external.currency.url}", configuration = FeignConfig.class)
public interface CurrencyClient {
    @GetMapping()
    CurrencyClientDto getCurrencies();
}
