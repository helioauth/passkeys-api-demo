package com.helioauth.passkeys.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.CreateCredentialRequest;
import com.helioauth.passkeys.demo.contract.CreateCredentialResponse;
import com.helioauth.passkeys.demo.contract.RegisterCredentialRequest;
import com.helioauth.passkeys.demo.service.UserAuthenticator;
import lombok.AllArgsConstructor;
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
    public String postRegisterCredential(@RequestBody RegisterCredentialRequest request) throws Exception {
        userAuthenticator.finishRegistration(request.getRequestId(), request.getPublicKeyCredential());
        return "{\"status\": \"success\"}";
    }
}
