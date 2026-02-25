package org.ricramiel.creditservice.service;

import org.ricramiel.creditservice.dto.CreditDTO;
import org.ricramiel.creditservice.model.Credit;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CreditService {



    Credit createCredit(CreditDTO creditDTO);
    void deleteCredit(UUID creditId);
    List<Credit> getByUserId(UUID userId);
    Page<Credit> findAllPageable(int size, int offset);
    Credit getByCardAccountId(UUID cardAccountId);
    Credit makeEnrollment(UUID cardAccountId, BigDecimal money);
}

