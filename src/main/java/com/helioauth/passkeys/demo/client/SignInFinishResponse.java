package com.helioauth.passkeys.demo.client;

public record SignInFinishResponse(
    String requestId,
    String username
) {
}
