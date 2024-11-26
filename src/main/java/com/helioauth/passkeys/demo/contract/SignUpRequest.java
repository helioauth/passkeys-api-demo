package com.helioauth.passkeys.demo.contract;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
    @NotBlank String displayName,
    @NotBlank @Email String email,
    @NotBlank String requestId,
    @NotBlank String publicKeyCredential
) {
}
