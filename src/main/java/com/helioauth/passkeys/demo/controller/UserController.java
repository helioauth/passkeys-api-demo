package com.helioauth.passkeys.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.*;
import com.helioauth.passkeys.demo.service.UserAuthenticator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Controller
@Slf4j
@AllArgsConstructor
public class UserController {

    UserAuthenticator userAuthenticator;

    @PostMapping(value = "/create-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateCredentialResponse postCreateCredential(@RequestBody CreateCredentialRequest createCredentialRequest) {
        try {
            return userAuthenticator.startRegistration(createCredentialRequest.name());
        } catch (JsonProcessingException e) {
            log.error("Create Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Create Credential failed");
        }
    }

    @PostMapping(value = "/register-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> postRegisterCredential(@RequestBody RegisterCredentialRequest request) {
        try {
            userAuthenticator.finishRegistration(request.getRequestId(), request.getPublicKeyCredential());
            return ResponseEntity.of(Optional.of(Map.of("requestId", request.getRequestId())));
        } catch (IOException e) {
            log.error("Register Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Register Credential failed");
        }
    }

    @PostMapping(value = "/signin-credential-options", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StartAssertionResponse postSignInCredential(@RequestBody StartAssertionRequest startAssertionRequest) {
        try {
            return userAuthenticator.startAssertion(startAssertionRequest.name());
        } catch (JsonProcessingException e) {
            log.error("Sign in Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sign in Credential failed");
        }
    }

    @PostMapping(value = "/signin-validate-key", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SignInSuccessResponse postSignInValidateKey(@RequestBody SignInValidateKeyRequest request) {
        try {
            String username = userAuthenticator.finishAssertion(request.getRequestId(), request.getPublicKeyCredentialWithAssertion());

            return SignInSuccessResponse.builder()
                    .requestId(request.getRequestId())
                    .username(username)
                    .build();
        } catch (IOException e) {
            log.error("Problem reading assertion", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem reading assertion");
        }
    }
}
