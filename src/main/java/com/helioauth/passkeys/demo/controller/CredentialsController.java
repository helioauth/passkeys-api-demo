package com.helioauth.passkeys.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.*;
import com.helioauth.passkeys.demo.service.UserCredentialManager;
import com.helioauth.passkeys.demo.service.UserSignInService;
import com.helioauth.passkeys.demo.service.UserSignupService;
import com.helioauth.passkeys.demo.service.dto.CredentialCreationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/credentials")
@Slf4j
@RequiredArgsConstructor
public class CredentialsController {

    private final UserSignInService userSignInService;
    private final UserSignupService userSignupService;
    private final UserCredentialManager userCredentialManager;

    @PostMapping(value = "/signup/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateCredentialResponse postCreateCredential(@RequestBody CreateCredentialRequest createCredentialRequest) {
        return userSignupService.startRegistration(createCredentialRequest.name());
    }

    @PostMapping(value = "/signup/finish", produces = MediaType.APPLICATION_JSON_VALUE)
    public FinishCredentialCreateResponse postRegisterCredential(@RequestBody RegisterCredentialRequest request) {
        userSignupService.finishRegistration(request.requestId(), request.publicKeyCredential());
        return new FinishCredentialCreateResponse(request.requestId());
    }

    @PostMapping(value = "/signin/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public StartAssertionResponse postSignInCredential(@RequestBody StartAssertionRequest startAssertionRequest) {
        try {
            return userSignInService.startAssertion(startAssertionRequest.name());
        } catch (JsonProcessingException e) {
            log.error("Sign in Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sign in Credential failed");
        }
    }

    @PostMapping(value = "/add/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateCredentialResponse credentialsAddStart(Authentication authenticationToken) {
        return userCredentialManager.createCredential(authenticationToken.getName());
    }

    @PostMapping(value = "/add/finish", produces = MediaType.APPLICATION_JSON_VALUE)
    public FinishCredentialCreateResponse credentialsAddFinish(@RequestBody RegisterCredentialRequest request, Authentication authentication) {
        userCredentialManager.finishCreateCredential(
                new CredentialCreationRequest(authentication, request.requestId(), request.publicKeyCredential())
        );
        return new FinishCredentialCreateResponse(request.requestId());
    }

}
