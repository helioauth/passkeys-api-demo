package com.helioauth.passkeys.demo.service;

import com.helioauth.passkeys.demo.contract.SignInValidateKeyRequest;
import com.helioauth.passkeys.demo.service.exception.SignInFailedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

public class PasskeyAuthenticationProvider implements AuthenticationProvider {
    private final WebAuthnAuthenticator webAuthnAuthenticator;

    public PasskeyAuthenticationProvider(WebAuthnAuthenticator webAuthnAuthenticator) {
        this.webAuthnAuthenticator = webAuthnAuthenticator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SignInValidateKeyRequest request = (SignInValidateKeyRequest) authentication.getCredentials();

        try {
            String loggedInUsername = webAuthnAuthenticator.finishAssertion(request.getRequestId(), request.getPublicKeyCredentialWithAssertion());
            return new PasskeyAuthenticationToken(loggedInUsername, request, List.of(new SimpleGrantedAuthority("USER")));
        } catch (IOException e) {
            throw new AuthenticationServiceException(e.getMessage());
        } catch (SignInFailedException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasskeyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
