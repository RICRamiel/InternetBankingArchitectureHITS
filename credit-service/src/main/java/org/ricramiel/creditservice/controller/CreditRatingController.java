package org.ricramiel.creditservice.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.dto.CreditRatingDTO;
import org.ricramiel.creditservice.mapper.CreditRatingMapper;
import org.ricramiel.creditservice.service.CreditRatingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/credit_rating")
public class CreditRatingController {

    private final CreditRatingService creditRatingService;

    @GetMapping("/{userId}/get_by_user")
    public CreditRatingDTO getByUserId(@PathVariable UUID userId){
        return CreditRatingMapper.toDto(creditRatingService.getByUserId(userId));
    }
}
