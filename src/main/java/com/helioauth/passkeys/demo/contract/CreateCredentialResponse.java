package com.helioauth.passkeys.demo.contract;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateCredentialResponse {
    ByteArray requestId;

    PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;
}
