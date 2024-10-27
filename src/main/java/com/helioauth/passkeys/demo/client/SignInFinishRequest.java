package com.helioauth.passkeys.demo.client;

public record SignInFinishRequest(
    String requestId,
    String publicKeyCredentialWithAssertion
) {}
