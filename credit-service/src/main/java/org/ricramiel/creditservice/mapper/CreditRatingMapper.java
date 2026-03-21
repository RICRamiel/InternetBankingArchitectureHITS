package org.ricramiel.creditservice.mapper;

import org.ricramiel.creditservice.dto.CreditRatingDTO;
import org.ricramiel.creditservice.model.CreditRating;

public class CreditRatingMapper {
    public static CreditRatingDTO toDto(CreditRating creditRating){

        if(creditRating == null){
            return null;
        }

        CreditRatingDTO creditRatingDTO = new CreditRatingDTO();
        creditRatingDTO.setRating(creditRating.getRating());
        creditRatingDTO.setUserId(creditRating.getUserId());
        creditRatingDTO.setId(creditRating.getId());

        return creditRatingDTO;
    }
}
