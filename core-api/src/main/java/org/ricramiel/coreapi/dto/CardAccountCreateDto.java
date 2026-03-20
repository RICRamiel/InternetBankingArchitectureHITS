package org.ricramiel.coreapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardAccountCreateDto {
    private String name;
    private String currency;
    private Boolean isMain;
}
