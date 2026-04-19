package org.ricramiel.userservice.infrastructure.idempotency;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.ricramiel.common.idempotency.HttpIdempotencyRecord;
import org.ricramiel.common.idempotency.IdempotencyStatus;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
        name = "http_idempotency_key",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idempotency_key", "user_scope"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpIdempotencyEntity implements HttpIdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 200)
    private String idempotencyKey;

    @Column(name = "user_scope", nullable = false, length = 200)
    private String userScope;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IdempotencyStatus status;

    @Column(name = "response_status")
    private Integer responseStatus;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "response_body")
    private byte[] responseBody;

    @Column(name = "response_content_type")
    private String responseContentType;
}
