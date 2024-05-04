package com.helioauth.passkeys.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.CreateCredentialRequest;
import com.helioauth.passkeys.demo.contract.CreateCredentialResponse;
import com.helioauth.passkeys.demo.contract.RegisterCredentialRequest;
import com.helioauth.passkeys.demo.service.UserAuthenticator;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@AllArgsConstructor
public class UserController {

    UserAuthenticator userAuthenticator;

    @GetMapping("/register")
    public String getRegisterUser(Model model) {

        model.addAttribute("creationOptions", "<empty>");

        return "register";
    }

    @PostMapping("/register")
    public String postRegisterUser(Model model) throws JsonProcessingException {
        model.addAttribute("success", true);
        return "register";
    }

    @PostMapping(value = "/create-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateCredentialResponse postCreateCredential(@RequestBody CreateCredentialRequest createCredentialRequest) throws JsonProcessingException {
        return userAuthenticator.startRegistration(createCredentialRequest.getName(), createCredentialRequest.getDisplayName());
    }

    @PostMapping(value = "/register-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegisterCredential(@RequestBody RegisterCredentialRequest request) throws Exception {
        userAuthenticator.finishRegistration(request.getRequestId(), request.getPublicKeyCredential());
        return "{\"status\": \"success\"}";
    }
}
