package org.ricramiel.coreapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CurrencyClientDto {
    @JsonProperty("Date")
    OffsetDateTime date;
    @JsonProperty("PreviousDate")
    OffsetDateTime prevDate;
    @JsonProperty("PreviousURL")
    String prevURL;
    @JsonProperty("Timestamp")
    OffsetDateTime timestamp;
    @JsonProperty("Valute")
    Map<String, CurrencyDto> valuteList;
}

