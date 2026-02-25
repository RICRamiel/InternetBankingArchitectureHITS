package org.ricramiel.creditservice.client;

import org.ricramiel.common.dtos.WithdrawDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "core-client", url = "${url.core-client-url}")
public interface CoreClient {
    @PostMapping("/transactions/withdraw")
    ResponseEntity<?> askForWithdraw(@RequestBody WithdrawDto withdrawDto);
}
