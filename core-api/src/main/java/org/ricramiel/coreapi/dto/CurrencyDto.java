package org.ricramiel.coreapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class CurrencyDto {
    @JsonProperty("ID")
    private String currencyId;

    @JsonProperty("NumCode")
    private String currencyNumCode;

    @JsonProperty("CharCode")
    private String currencyCharCode;

    @JsonProperty("Nominal")
    private Integer currencyNominal;

    @JsonProperty("Name")
    private String currencyName;

    @JsonProperty("Value")
    private BigDecimal currencyCourse;

    @JsonProperty("Previous")
    private BigDecimal currencyPrevCourse;
}
