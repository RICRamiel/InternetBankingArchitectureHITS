package org.ricramiel.coreapi.idempotency;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.idempotency.HttpIdempotencyStore;
import org.ricramiel.common.idempotency.IdempotencyStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HttpIdempotencyStoreImpl implements HttpIdempotencyStore<HttpIdempotencyEntity> {

    private final HttpIdempotencyRepository repository;

    @Override
    public Optional<HttpIdempotencyEntity> find(String idempotencyKey, String userScope) {
        return repository.findByIdempotencyKeyAndUserScope(idempotencyKey, userScope);
    }

    @Override
    public HttpIdempotencyEntity createInProgress(String idempotencyKey, String userScope, String requestFingerprint) {
        return HttpIdempotencyEntity.builder()
                .idempotencyKey(idempotencyKey)
                .userScope(userScope)
                .requestFingerprint(requestFingerprint)
                .status(IdempotencyStatus.IN_PROGRESS)
                .build();
    }

    @Override
    public HttpIdempotencyEntity save(HttpIdempotencyEntity record) {
        return repository.saveAndFlush(record);
    }

    @Override
    public void delete(HttpIdempotencyEntity record) {
        repository.delete(record);
        repository.flush();
    }
}
