package com.helioauth.passkeys.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.CreateCredentialResponse;
import com.helioauth.passkeys.demo.domain.User;
import com.helioauth.passkeys.demo.domain.UserCredential;
import com.helioauth.passkeys.demo.domain.UserCredentialRepository;
import com.helioauth.passkeys.demo.domain.UserRepository;
import com.helioauth.passkeys.demo.mapper.UserCredentialMapper;
import com.helioauth.passkeys.demo.service.dto.CredentialRegistrationResultDto;
import com.helioauth.passkeys.demo.service.exception.SignUpFailedException;
import com.helioauth.passkeys.demo.service.exception.UsernameAlreadyRegisteredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSignupService {
    private final UserRepository userRepository;

    private final UserCredentialRepository userCredentialRepository;

    private final WebAuthnAuthenticator webAuthnAuthenticator;

    private final UserCredentialMapper usercredentialMapper;

    public CreateCredentialResponse startRegistration(String name) {
        userRepository.findByName(name).ifPresent(user -> {
            throw new UsernameAlreadyRegisteredException();
        });

        try {
            return webAuthnAuthenticator.startRegistration(name);
        } catch (JsonProcessingException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }

    @Transactional
    public void finishRegistration(String requestId, String publicKeyCredentialJson) {
        try {
            // TODO fix user handle to be the same for all registered credentials and save in user entity
            CredentialRegistrationResultDto result = webAuthnAuthenticator.finishRegistration(requestId, publicKeyCredentialJson);

            User user = User.builder()
                    .name(result.name())
                    .displayName(result.displayName())
                    .build();
            userRepository.save(user);

            UserCredential userCredential = usercredentialMapper.fromCredentialRegistrationResult(result);
            userCredential.setUser(user);
            userCredentialRepository.save(userCredential);
        } catch (IOException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }
}
