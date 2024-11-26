package com.helioauth.passkeys.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helioauth.passkeys.demo.contract.SignInValidateKeyRequest;
import com.helioauth.passkeys.demo.service.PasskeyAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.Setter;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Setter
public class WebAuthnAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/login", "POST");

    private ObjectMapper objectMapper = new ObjectMapper();

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
            signInValidateKeyRequest = objectMapper.readValue(request.getReader(), SignInValidateKeyRequest.class);
        } catch (IOException e) {
            throw new BadCredentialsException("Unable to parse request body", e);
        }

        return this.getAuthenticationManager()
            .authenticate(
                PasskeyAuthenticationToken.unauthenticated(signInValidateKeyRequest)
            );
    }
}
