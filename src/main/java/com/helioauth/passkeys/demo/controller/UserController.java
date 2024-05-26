package com.helioauth.passkeys.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.*;
import com.helioauth.passkeys.demo.service.UserAuthenticator;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
public class UserController {

    UserAuthenticator userAuthenticator;

    @PostMapping(value = "/create-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateCredentialResponse postCreateCredential(@RequestBody CreateCredentialRequest createCredentialRequest) throws JsonProcessingException {
        return userAuthenticator.startRegistration(createCredentialRequest.name());
    }

    @PostMapping(value = "/register-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String postRegisterCredential(@RequestBody RegisterCredentialRequest request) throws Exception {
        userAuthenticator.finishRegistration(request.getRequestId(), request.getPublicKeyCredential());
        return "{\"requestId\":\"" + request.getRequestId() + "\"}";
    }

    @PostMapping(value = "/signin-credential-options", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StartAssertionResponse postSignInCredential(@RequestBody StartAssertionRequest startAssertionRequest) throws JsonProcessingException {
        return userAuthenticator.startAssertion(startAssertionRequest.name());
    }

    @PostMapping(value = "/signin-validate-key", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SignInSuccessResponse postSignInValidateKey(@RequestBody SignInValidateKeyRequest request) throws Exception {
        String username = userAuthenticator.finishAssertion(request.getRequestId(), request.getPublicKeyCredentialWithAssertion());
        return SignInSuccessResponse.builder()
                .requestId(request.getRequestId())
                .username(username)
                .build();
    }
}
