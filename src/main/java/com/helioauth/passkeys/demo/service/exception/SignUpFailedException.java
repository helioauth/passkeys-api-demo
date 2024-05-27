package com.helioauth.passkeys.demo.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Couldn't find a signup session. Restart the signup process.")
public class SignUpFailedException extends RuntimeException {

}