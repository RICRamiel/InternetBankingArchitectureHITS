package org.ricramiel.common.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginationDto {
    @NotNull
    int page;

    @NotNull
    int pageSize;

    @NotNull
    int count;
}