package com.helioauth.passkeys.demo.controller;

import com.helioauth.passkeys.demo.client.PasskeyApiException;
import com.helioauth.passkeys.demo.client.PasskeysApiClient;
import com.helioauth.passkeys.demo.client.SignUpFinishRequest;
import com.helioauth.passkeys.demo.contract.SignUpRequest;
import com.helioauth.passkeys.demo.domain.User;
import com.helioauth.passkeys.demo.domain.UserCredential;
import com.helioauth.passkeys.demo.domain.UserCredentialRepository;
import com.helioauth.passkeys.demo.domain.UserRepository;
import com.helioauth.passkeys.demo.mapper.UserCredentialMapper;
import com.helioauth.passkeys.demo.service.exception.UsernameAlreadyRegisteredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import java.util.List;
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
    public String dashboard(Authentication user, Model model) {
        List<UserCredential> result = userCredentialRepository.findAllByUserName(user.getName());

        model.addAttribute("passkeys", userCredentialMapper.toDto(result));

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
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}