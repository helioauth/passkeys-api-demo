package com.helioauth.passkeys.demo.service.dto;

import java.time.Instant;

public record UserCredentialDTO(
    String credentialId,
    String userHandle,
    Long signatureCount,
    String displayName,
    Instant createdAt,
    Instant lastUsedAt
) {
}
