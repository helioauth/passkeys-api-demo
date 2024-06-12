package com.helioauth.passkeys.demo.service.dto;

import java.time.Instant;

public record UserCredentialDTO(String credentialId, Instant lastUsedAt, String userHandle, Long signatureCount) {
}
