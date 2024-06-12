package com.helioauth.passkeys.demo.service.dto;

import java.io.Serializable;

public record CredentialRegistrationResultDto(
        String name,
        String displayName,
        String credentialId,
        String userHandle,
        Long signatureCount,
        String publicKeyCose,
        String attestationObject,
        String clientDataJson,
        Boolean backupEligible,
        Boolean backupState,
        Boolean isDiscoverable
) implements Serializable {
}