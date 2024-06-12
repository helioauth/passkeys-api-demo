package com.helioauth.passkeys.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.*;
import com.helioauth.passkeys.demo.domain.UserCredential;
import com.helioauth.passkeys.demo.domain.UserCredentialRepository;
import com.helioauth.passkeys.demo.mapper.UserCredentialMapper;
import com.helioauth.passkeys.demo.service.PasskeyAuthenticationToken;
import com.helioauth.passkeys.demo.service.WebAuthnAuthenticator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@Slf4j
@AllArgsConstructor
public class UserController {

    WebAuthnAuthenticator webAuthnAuthenticator;

    UserCredentialRepository userCredentialRepository;

    UserCredentialMapper userCredentialMapper;

    @GetMapping("/")
    public String dashboard(Authentication user, Model model) {

        PasskeyAuthenticationToken token = (PasskeyAuthenticationToken) user;

        List<UserCredential> result = userCredentialRepository.findAllByUserName(token.getName());

        model.addAttribute("passkeys", userCredentialMapper.toDto(result));

        return "dashboard";
    }

    @PostMapping(value = "/create-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateCredentialResponse postCreateCredential(@RequestBody CreateCredentialRequest createCredentialRequest) {
        try {
            return webAuthnAuthenticator.startRegistration(createCredentialRequest.name());
        } catch (JsonProcessingException e) {
            log.error("Create Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Create Credential failed");
        }
    }

    @PostMapping(value = "/register-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> postRegisterCredential(@RequestBody RegisterCredentialRequest request) {
        try {
            webAuthnAuthenticator.finishRegistration(request.requestId(), request.publicKeyCredential());
            return ResponseEntity.of(Optional.of(Map.of("requestId", request.requestId())));
        } catch (IOException e) {
            log.error("Register Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Register Credential failed");
        }
    }

    @PostMapping(value = "/signin-credential-options", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StartAssertionResponse postSignInCredential(@RequestBody StartAssertionRequest startAssertionRequest) {
        try {
            return webAuthnAuthenticator.startAssertion(startAssertionRequest.name());
        } catch (JsonProcessingException e) {
            log.error("Sign in Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sign in Credential failed");
        }
    }
}
