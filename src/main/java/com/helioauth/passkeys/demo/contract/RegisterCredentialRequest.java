package com.helioauth.passkeys.demo.contract;

public record RegisterCredentialRequest(String requestId, String publicKeyCredential) { }
