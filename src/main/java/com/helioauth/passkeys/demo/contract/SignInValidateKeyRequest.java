package com.helioauth.passkeys.demo.contract;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SignInValidateKeyRequest {
    String requestId;
    String publicKeyCredentialWithAssertion;
}
