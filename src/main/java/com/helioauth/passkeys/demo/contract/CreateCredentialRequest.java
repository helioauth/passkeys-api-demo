package com.helioauth.passkeys.demo.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateCredentialRequest {
    private String name;
    private String displayName;
}
