package com.helioauth.passkeys.demo.contract;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateCredentialResponse {
    String requestId;
    String publicKeyCredentialCreationOptions;
}
