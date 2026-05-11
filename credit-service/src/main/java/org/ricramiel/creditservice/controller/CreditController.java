package org.ricramiel.creditservice.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.dto.CreditAnswerDTO;
import org.ricramiel.creditservice.dto.CreditCreateModelDto;
import org.ricramiel.creditservice.dto.CreditDTO;
import org.ricramiel.creditservice.infrastructure.ScheduledService;
import org.ricramiel.creditservice.mapper.CreditMapper;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.service.CreditService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/credit")
public class CreditController {

    private final CreditService creditService;
    private final ScheduledService scheduledService;

    @PreAuthorize("hasRole('WORKER')")
    @PostMapping("/create")
    public ResponseEntity<CreditAnswerDTO> createCredit(@RequestBody CreditCreateModelDto creditDTO) {
        return ResponseEntity.ok(CreditMapper.toAnswerDtoTemp(creditService.createCredit(creditDTO)));
    }

    @PreAuthorize("hasRole('WORKER')")
    @DeleteMapping("/{creditId}/delete")
    public void deleteCredit(@PathVariable("creditId") @Param("creditId") UUID creditId) {
        creditService.deleteCredit(creditId);
    }

    @PreAuthorize("hasRole('WORKER') OR @accessChecker.isSelf(#userId)")
    @GetMapping("/{userId}/get_by_user_id")
    public ResponseEntity<List<CreditAnswerDTO>> getByUserId(@PathVariable("userId") @Param("userId") UUID userId) {
        return ResponseEntity.ok(CreditMapper.toListDto(creditService.getByUserId(userId)));
    }

    @PreAuthorize("hasRole('WORKER') OR @accessChecker.isAccountOwner(#cardAccountId)")
    @GetMapping("/{cardAccountId}/get_by_card_account")
    public ResponseEntity<CreditAnswerDTO> getByCardAccountId(@PathVariable("cardAccountId") @Param("cardAccountId") UUID cardAccountId) {
        return ResponseEntity.ok(CreditMapper.toAnswerDto(creditService.getByCardAccountId(cardAccountId)));
    }

    @PreAuthorize("hasRole('WORKER') OR @accessChecker.isAccountOwner(#cardAccountId)")
    @PostMapping("/{cardAccountId}/enrollment")
    public ResponseEntity<CreditAnswerDTO> makeEnrollment(@PathVariable("cardAccountId") @Param("cardAccountId") UUID cardAccountId,
                                                          @RequestParam("money") BigDecimal money) {
        Credit credit = creditService.getByCardAccountId(cardAccountId);
        if (credit.getCurrentDebtSum().compareTo(BigDecimal.ZERO) > 0) {
            scheduledService.withdraw(credit.getCardAccount(), money);
        }
        return ResponseEntity.ok(CreditMapper.toAnswerDto(credit));
    }
}
