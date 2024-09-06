package com.helioauth.passkeys.demo.client;

public record SignUpFinishRequest(
    String requestId,
    String publicKeyCredential
) { }
