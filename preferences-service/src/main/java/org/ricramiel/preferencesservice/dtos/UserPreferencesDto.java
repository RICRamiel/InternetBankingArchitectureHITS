package org.ricramiel.preferencesservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesDto {
    
    @Builder.Default
    private String theme = "LIGHT";
    
    @Builder.Default
    private Set<String> hiddenAccounts = new HashSet<>();
}