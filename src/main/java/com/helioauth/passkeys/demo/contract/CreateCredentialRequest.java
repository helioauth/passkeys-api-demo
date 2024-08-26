package com.helioauth.passkeys.demo.contract;

public record CreateCredentialRequest(String name) {
    public CreateCredentialRequest(String name) {
        this.name = name.strip().toLowerCase();
    }
}
