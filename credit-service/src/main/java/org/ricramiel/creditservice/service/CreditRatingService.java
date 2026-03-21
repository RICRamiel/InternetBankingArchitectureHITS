package org.ricramiel.creditservice.service;

import org.ricramiel.creditservice.model.CreditRating;

import java.util.UUID;

public interface CreditRatingService {
    CreditRating getByUserId(UUID userId);
}
