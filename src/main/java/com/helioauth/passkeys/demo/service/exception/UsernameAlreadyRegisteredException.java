package com.helioauth.passkeys.demo.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Account already exists")
public class UsernameAlreadyRegisteredException extends RuntimeException {

}
