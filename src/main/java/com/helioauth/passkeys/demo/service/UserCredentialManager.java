package com.helioauth.passkeys.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.CreateCredentialResponse;
import com.helioauth.passkeys.demo.domain.User;
import com.helioauth.passkeys.demo.domain.UserCredential;
import com.helioauth.passkeys.demo.domain.UserCredentialRepository;
import com.helioauth.passkeys.demo.domain.UserRepository;
import com.helioauth.passkeys.demo.mapper.UserCredentialMapper;
import com.helioauth.passkeys.demo.service.dto.CredentialCreationRequest;
import com.helioauth.passkeys.demo.service.dto.CredentialRegistrationResultDto;
import com.helioauth.passkeys.demo.service.exception.CreateCredentialFailedException;
import com.helioauth.passkeys.demo.service.exception.SignUpFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialManager {
    private final WebAuthnAuthenticator webAuthnAuthenticator;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserCredentialMapper usercredentialMapper;

    public CreateCredentialResponse createCredential(String name) {
        try {
            return webAuthnAuthenticator.startRegistration(name);
        } catch (JsonProcessingException e) {
            log.error("Creating a new credential failed", e);
            throw new CreateCredentialFailedException();
        }
    }

    public void finishCreateCredential(CredentialCreationRequest request) {
        try {
            CredentialRegistrationResultDto result = webAuthnAuthenticator.finishRegistration(
                    request.requestId(),
                    request.publicKeyCredentialJson()
            );

            String username = (String) request.authentication().getPrincipal();
            User user = userRepository.findByName(username).orElseThrow(CreateCredentialFailedException::new);

            UserCredential userCredential = usercredentialMapper.fromCredentialRegistrationResult(result);
            userCredential.setUser(user);
            userCredentialRepository.save(userCredential);
        } catch (IOException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }
}
