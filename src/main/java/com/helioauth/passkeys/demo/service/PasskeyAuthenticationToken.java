package com.helioauth.passkeys.demo.service;

import com.helioauth.passkeys.demo.contract.SignInValidateKeyRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class PasskeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String name;

    private SignInValidateKeyRequest signInRequest;

    public PasskeyAuthenticationToken(String name, SignInValidateKeyRequest credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);

        this.name = name;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    public PasskeyAuthenticationToken(SignInValidateKeyRequest request) {
        super(null);

        this.signInRequest = request;
        setAuthenticated(false);
    }

    public static Authentication unauthenticated(SignInValidateKeyRequest request) {
        return new PasskeyAuthenticationToken(request);
    }

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
