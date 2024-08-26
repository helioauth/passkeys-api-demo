package com.helioauth.passkeys.demo.controller;

import com.helioauth.passkeys.demo.domain.UserCredential;
import com.helioauth.passkeys.demo.domain.UserCredentialRepository;
import com.helioauth.passkeys.demo.mapper.UserCredentialMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserCredentialRepository userCredentialRepository;

    private final UserCredentialMapper userCredentialMapper;

    @GetMapping("/")
    public String dashboard(Authentication user, Model model) {
        List<UserCredential> result = userCredentialRepository.findAllByUserName(user.getName());

        model.addAttribute("passkeys", userCredentialMapper.toDto(result));

        return "dashboard";
    }
}