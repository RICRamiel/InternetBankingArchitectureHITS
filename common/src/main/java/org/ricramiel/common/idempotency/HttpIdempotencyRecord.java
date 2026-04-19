package org.ricramiel.common.idempotency;

public interface HttpIdempotencyRecord {
    String getIdempotencyKey();
    String getUserScope();
    String getRequestFingerprint();
    IdempotencyStatus getStatus();
    Integer getResponseStatus();
    byte[] getResponseBody();
    String getResponseContentType();

    void setStatus(IdempotencyStatus status);
    void setResponseStatus(Integer responseStatus);
    void setResponseBody(byte[] responseBody);
    void setResponseContentType(String responseContentType);
}
