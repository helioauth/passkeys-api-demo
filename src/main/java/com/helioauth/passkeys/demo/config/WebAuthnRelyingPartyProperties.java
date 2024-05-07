package com.helioauth.passkeys.demo.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "relyingparty")
@ConfigurationPropertiesScan
@Getter
@AllArgsConstructor
public class WebAuthnRelyingPartyProperties {

    private String hostname;

    private String displayName;
}
