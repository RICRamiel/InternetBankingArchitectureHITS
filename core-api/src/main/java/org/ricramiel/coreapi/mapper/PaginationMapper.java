package org.ricramiel.coreapi.mapper;

import org.ricramiel.common.dtos.PaginationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PaginationMapper {
    public <T> PaginationDto toDto(Page<T> domainPage) {
        return new PaginationDto(
                domainPage.getNumber(),
                domainPage.getSize(),
                domainPage.getTotalPages()
        );
    }
    public Pageable toPageable(PaginationDto dto) {
        return PageRequest.of(dto.getPage(), dto.getPageSize());
    }
}