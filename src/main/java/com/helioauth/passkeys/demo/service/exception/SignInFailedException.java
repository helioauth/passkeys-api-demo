package com.helioauth.passkeys.demo.service.exception;

public class SignInFailedException extends RuntimeException {
    public SignInFailedException() {
        super("Couldn't find an account with this email and passkey.");
    }
}
