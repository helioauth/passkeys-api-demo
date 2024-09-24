package com.helioauth.passkeys.demo.controller;

import com.helioauth.passkeys.demo.client.ListPasskeysResponse;
import com.helioauth.passkeys.demo.client.PasskeyApiException;
import com.helioauth.passkeys.demo.client.PasskeysApiClient;
import com.helioauth.passkeys.demo.client.SignUpFinishRequest;
import com.helioauth.passkeys.demo.contract.SignUpRequest;
import com.helioauth.passkeys.demo.domain.User;
import com.helioauth.passkeys.demo.domain.UserCredentialRepository;
import com.helioauth.passkeys.demo.domain.UserRepository;
import com.helioauth.passkeys.demo.mapper.UserCredentialMapper;
import com.helioauth.passkeys.demo.service.PasskeyAuthenticationToken;
import com.helioauth.passkeys.demo.service.exception.UsernameAlreadyRegisteredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserCredentialRepository userCredentialRepository;

    private final UserCredentialMapper userCredentialMapper;
    private final PasskeysApiClient passkeysApiClient;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String dashboard(PasskeyAuthenticationToken user, Model model) {
        try {
            val response = passkeysApiClient.listPasskeys(user.getUser().getExternalId());
            model.addAttribute("passkeys", response.passkeys());
        } catch (IOException e) {
            log.error("Error getting passkeys", e);
        }

        return "dashboard";
    }

    @ResponseBody
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody @Valid SignUpRequest request) {
        userRepository.findByName(request.email()).ifPresent(user -> {
            throw new UsernameAlreadyRegisteredException();
        });

        try {
            var response = passkeysApiClient.signUpFinish(
                new SignUpFinishRequest(request.requestId(), request.publicKeyCredential())
            );

            User newUser = User.builder()
                .name(request.email())
                .displayName(request.displayName())
                .externalId(response.userId())
                .build();
            userRepository.save(newUser);

            return ResponseEntity.ok(Map.of("requestId", request.requestId()));
        } catch (PasskeyApiException e) {
            log.error("Passkeys API error", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error during signup", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}