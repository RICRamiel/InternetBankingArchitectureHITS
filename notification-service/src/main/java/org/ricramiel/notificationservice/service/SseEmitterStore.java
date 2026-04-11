package org.ricramiel.notificationservice.service;

import org.ricramiel.common.enums.Role;
import org.ricramiel.notificationservice.entity.SseConnection;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterStore {
    private final Map<UUID, SseConnection> clientEmitters = new ConcurrentHashMap<>();
    private final List<SseConnection> employeeEmitters = new CopyOnWriteArrayList<>();

    public void addConnection(SseConnection connection) {
        Set<Role> roles = connection.getRoles();
        if (roles.contains(Role.CLIENT)) {
            clientEmitters.put(connection.getUserId(), connection);
        }

        if (roles.contains(Role.WORKER)) {
            employeeEmitters.add(connection);
        }
    }

    public void removeConnection(SseConnection connection) {
        Set<Role> roles = connection.getRoles();

        if (roles.contains(Role.CLIENT)) {
            clientEmitters.remove(connection.getUserId());
        }

        if (roles.contains(Role.WORKER)) {
            employeeEmitters.remove(connection);
        }
    }

    public void sendToClient(UUID clientId, Object payload) {
        SseConnection conn = clientEmitters.get(clientId);
        if (conn != null) sendSafely(conn, payload);
    }

    public void sendToAllEmployees(Object payload) {
        employeeEmitters.forEach(conn -> sendSafely(conn, payload));
    }

    private void sendSafely(SseConnection connection, Object payload) {
        synchronized (connection.getLock()) {
            try {
                connection.getEmitter().send(payload, MediaType.APPLICATION_JSON);
            } catch (IOException e) {
                connection.getEmitter().completeWithError(e);
            }
        }
    }
}