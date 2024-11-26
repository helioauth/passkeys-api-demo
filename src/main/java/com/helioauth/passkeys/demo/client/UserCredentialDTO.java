package com.helioauth.passkeys.demo.client;

import java.time.Instant;

public record UserCredentialDTO(
    String userHandle,
    Long signatureCount,
    String displayName,
    Instant createdAt,
    Instant lastUsedAt
) {
}
