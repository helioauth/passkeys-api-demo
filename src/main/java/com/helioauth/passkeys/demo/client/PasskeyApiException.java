package com.helioauth.passkeys.demo.client;

public class PasskeyApiException extends RuntimeException {
    public PasskeyApiException(String s) {
        super(s);
    }

    public PasskeyApiException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
