package com.helioauth.passkeys.demo.service;

import com.helioauth.passkeys.demo.contract.SignInValidateKeyRequest;
import com.helioauth.passkeys.demo.service.exception.SignInFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Slf4j
public class PasskeyAuthenticationProvider implements AuthenticationProvider {
    private final UserSignInService userSignInService;

    public PasskeyAuthenticationProvider(UserSignInService userSignInService) {
        this.userSignInService = userSignInService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SignInValidateKeyRequest request = (SignInValidateKeyRequest) authentication.getCredentials();

        // TODO Fix user handle to be the same on all
        try {
            String loggedInUsername = userSignInService.finishAssertion(request.getRequestId(), request.getPublicKeyCredentialWithAssertion());

            return new PasskeyAuthenticationToken(loggedInUsername, request, List.of(new SimpleGrantedAuthority("USER")));
        } catch (SignInFailedException e) {
            throw new BadCredentialsException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AuthenticationServiceException(e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasskeyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
