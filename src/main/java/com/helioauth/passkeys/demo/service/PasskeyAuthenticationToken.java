package com.helioauth.passkeys.demo.service;

import com.helioauth.passkeys.demo.contract.SignInValidateKeyRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class PasskeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String name;

    private final SignInValidateKeyRequest credentials;

    public PasskeyAuthenticationToken(String name, SignInValidateKeyRequest credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);

        this.name = name;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    public PasskeyAuthenticationToken(String name, SignInValidateKeyRequest credentials) {
        super(null);

        this.name = name;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    public static Authentication unauthenticated(String principal, SignInValidateKeyRequest credentials) {
        return new PasskeyAuthenticationToken(principal, credentials);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return name;
    }
}
