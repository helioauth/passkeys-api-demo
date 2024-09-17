package com.helioauth.passkeys.demo.service;

import com.helioauth.passkeys.demo.contract.SignInValidateKeyRequest;
import com.helioauth.passkeys.demo.domain.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class PasskeyAuthenticationToken extends AbstractAuthenticationToken {

    private User user;

    private SignInValidateKeyRequest signInRequest;

    public PasskeyAuthenticationToken(User user, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);

        this.user = user;
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

    public static Authentication authenticated(User user) {
        return new PasskeyAuthenticationToken(user, List.of(new SimpleGrantedAuthority("USER")));
    }

    @Override
    public Object getCredentials() {
        return signInRequest;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
}
