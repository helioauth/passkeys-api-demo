package com.helioauth.passkeys.demo.contract;

public record StartAssertionRequest(String name) {
    public StartAssertionRequest(String name) {
        this.name = name.strip().toLowerCase();
    }
}
