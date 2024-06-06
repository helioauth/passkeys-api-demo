package com.helioauth.passkeys.demo.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SignInValidateKeyRequest {
    String requestId;
    String publicKeyCredentialWithAssertion;
}
