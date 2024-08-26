package com.helioauth.passkeys.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helioauth.passkeys.demo.contract.SignInValidateKeyRequest;
import com.helioauth.passkeys.demo.service.PasskeyAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Collections;

public class WebAuthnAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/v1/credentials/signin/finish", "POST");

    public WebAuthnAuthenticationProcessingFilter() {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        SignInValidateKeyRequest signInValidateKeyRequest;
        try {
            signInValidateKeyRequest = (new ObjectMapper()).readValue(request.getReader(), SignInValidateKeyRequest.class);
        } catch (IOException e) {
            throw new BadCredentialsException("Unable to parse request body", e);
        }

        PasskeyAuthenticationToken authRequest = new PasskeyAuthenticationToken(
                signInValidateKeyRequest.getRequestId(),
                signInValidateKeyRequest,
                Collections.emptyList()
        );
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
