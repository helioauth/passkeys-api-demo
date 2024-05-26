package com.helioauth.passkeys.demo.contract;

import com.yubico.webauthn.data.ByteArray;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateCredentialResponse {
    String requestId;
    String publicKeyCredentialCreationOptions;
}
