package org.ricramiel.common.idempotency;

import java.util.Optional;

public interface HttpIdempotencyStore<T extends HttpIdempotencyRecord> {
    Optional<T> find(String idempotencyKey, String userScope);
    T createInProgress(String idempotencyKey, String userScope, String requestFingerprint);
    T save(T record);
    void delete(T record);
}
