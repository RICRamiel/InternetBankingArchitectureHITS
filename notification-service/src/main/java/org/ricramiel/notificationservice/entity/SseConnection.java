package org.ricramiel.notificationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ricramiel.common.enums.Role;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SseConnection {
    private SseEmitter emitter;
    private UUID userId;
    private Set<Role> roles;
    private final Object lock = new Object();
}
