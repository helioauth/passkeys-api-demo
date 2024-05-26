package com.helioauth.passkeys.demo.contract;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StartAssertionResponse {
    String requestId;
    String credentialsGetOptions;
}
