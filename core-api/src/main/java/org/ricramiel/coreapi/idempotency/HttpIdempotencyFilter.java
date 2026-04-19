package org.ricramiel.coreapi.idempotency;

import org.ricramiel.common.idempotency.IdempotencyFilter;
import org.springframework.stereotype.Component;

@Component
public class HttpIdempotencyFilter extends IdempotencyFilter<HttpIdempotencyEntity> {
    public HttpIdempotencyFilter(HttpIdempotencyStoreImpl store) {
        super(store);
    }
}
