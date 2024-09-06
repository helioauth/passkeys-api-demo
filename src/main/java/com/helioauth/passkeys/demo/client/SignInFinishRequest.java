package com.helioauth.passkeys.demo.client;

public class SignInFinishRequest {
    public String requestId;
    public String publicKeyCredentialWithAssertion;

    public SignInFinishRequest(String requestId, String publicKeyCredentialWithAssertion) {
        this.requestId = requestId;
        this.publicKeyCredentialWithAssertion = publicKeyCredentialWithAssertion;
    }
}
