package org.ricramiel.coreapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CurrencyClientDto {
    @JsonProperty("Date")
    LocalDateTime date;
    @JsonProperty("PreviousDate")
    LocalDateTime prevDate;
    @JsonProperty("PreviousURL")
    String prevURL;
    @JsonProperty("Timestamp")
    LocalDateTime timestamp;
    @JsonProperty("Valute")
    Map<String, CurrencyDto> valuteList;
}

