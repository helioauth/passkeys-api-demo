package com.helioauth.passkeys.demo.client;

import java.util.UUID;

public record SignUpFinishResponse(
    String requestId,
    UUID userId
) {
}
