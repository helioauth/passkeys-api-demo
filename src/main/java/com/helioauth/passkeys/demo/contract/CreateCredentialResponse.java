package com.helioauth.passkeys.demo.contract;

import com.yubico.webauthn.data.ByteArray;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateCredentialResponse {
    ByteArray requestId;
    String publicKeyCredentialCreationOptions;
}
