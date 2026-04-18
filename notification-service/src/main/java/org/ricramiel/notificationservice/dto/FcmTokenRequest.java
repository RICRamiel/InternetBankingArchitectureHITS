package org.ricramiel.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String token;

    private String platform; //храним WEB_WORKER \ WEB_CLIENT чтобы резолвить кто куда
}