package com.helioauth.passkeys.demo.client;

import java.util.List;

public record ListPasskeysResponse(List<UserCredentialDTO> passkeys) {}