package com.helioauth.passkeys.demo.service;

import com.helioauth.passkeys.demo.client.PasskeyApiException;
import com.helioauth.passkeys.demo.client.PasskeysApiClient;
import com.helioauth.passkeys.demo.client.SignInFinishRequest;
import com.helioauth.passkeys.demo.contract.SignInValidateKeyRequest;
import com.helioauth.passkeys.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@RequiredArgsConstructor
public class PasskeyAuthenticationProvider implements AuthenticationProvider {
    private final UserRepository userRepository;
    private final PasskeysApiClient passkeysApiClient;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SignInValidateKeyRequest request = (SignInValidateKeyRequest) authentication.getCredentials();

        try {
            var response = passkeysApiClient.signInFinish(
                new SignInFinishRequest(request.getRequestId(), request.getPublicKeyCredentialWithAssertion())
            );

            return userRepository.findByName(response.username())
                .map(PasskeyAuthenticationToken::authenticated)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        } catch (PasskeyApiException e) {
            log.error(e.getMessage(), e);
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
