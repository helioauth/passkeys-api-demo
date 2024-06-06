package com.helioauth.passkeys.demo.config;

import com.helioauth.passkeys.demo.webauthn.DatabaseCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuthnRelyingPartyConfig {

    @Bean
    public RelyingParty defaultRelyingParty(
        DatabaseCredentialRepository databaseCredentialRepository,
        WebAuthnRelyingPartyProperties properties
    ) {
        RelyingPartyIdentity rpId = RelyingPartyIdentity.builder()
                .id(properties.hostname())
                .name(properties.displayName())
                .build();

        return RelyingParty.builder()
                .identity(rpId)
                .credentialRepository(databaseCredentialRepository)
                .allowOriginPort(properties.allowOriginPort())
                .build();
    }
}
