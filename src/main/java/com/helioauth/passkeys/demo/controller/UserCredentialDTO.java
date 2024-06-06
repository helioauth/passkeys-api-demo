package com.helioauth.passkeys.demo.controller;

import java.time.Instant;

public record UserCredentialDTO(String credentialId, Instant lastUsedAt, String userHandle, Long signatureCount) {
}
