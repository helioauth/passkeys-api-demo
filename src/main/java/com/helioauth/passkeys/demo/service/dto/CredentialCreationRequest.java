package com.helioauth.passkeys.demo.service.dto;

import org.springframework.security.core.Authentication;

public record CredentialCreationRequest(
        Authentication authentication,
        String requestId,
        String publicKeyCredentialJson
) {
}
