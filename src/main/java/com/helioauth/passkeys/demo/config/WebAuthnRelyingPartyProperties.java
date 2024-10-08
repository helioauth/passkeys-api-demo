package com.helioauth.passkeys.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "relyingparty")
@ConfigurationPropertiesScan
public record WebAuthnRelyingPartyProperties(
        String hostname,
        String displayName,
        boolean allowOriginPort
) { }
