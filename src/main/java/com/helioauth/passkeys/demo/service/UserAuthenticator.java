package com.helioauth.passkeys.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.CreateCredentialResponse;
import com.helioauth.passkeys.demo.contract.StartAssertionResponse;
import com.helioauth.passkeys.demo.domain.User;
import com.helioauth.passkeys.demo.domain.UserCredential;
import com.helioauth.passkeys.demo.domain.UserCredentialRepository;
import com.helioauth.passkeys.demo.domain.UserRepository;
import com.helioauth.passkeys.demo.service.exception.UsernameAlreadyRegisteredException;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class UserAuthenticator {

    private final RelyingParty relyingParty;

    private final UserCredentialRepository userCredentialRepository;

    private final UserRepository userRepository;

    // TODO Expire cached requests
    private final Map<String, String> cache = new HashMap<>();

    private static final SecureRandom random = new SecureRandom();

    public CreateCredentialResponse startRegistration(String name) throws JsonProcessingException {
        userRepository.findByName(name).ifPresent(user -> {
            throw new UsernameAlreadyRegisteredException();
        });

        ByteArray id = generateRandom(32);

        ResidentKeyRequirement residentKeyRequirement = ResidentKeyRequirement.PREFERRED;

        PublicKeyCredentialCreationOptions request = relyingParty.startRegistration(StartRegistrationOptions.builder()
            .user(
                UserIdentity.builder()
                    .name(name)
                    .displayName(name)
                    .id(id)
                    .build()
            )
            .authenticatorSelection(
                    AuthenticatorSelectionCriteria.builder()
                            .residentKey(residentKeyRequirement)
                            .build()
            )
            .build()
        );

        String requestId = id.getHex();
        cache.putIfAbsent(requestId, request.toJson());

        return CreateCredentialResponse.builder()
                .requestId(requestId)
                .publicKeyCredentialCreationOptions(request.toCredentialsCreateJson())
                .build();
    }

    public void finishRegistration(String requestId, String publicKeyCredentialJson) throws Exception {
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                PublicKeyCredential.parseRegistrationResponseJson(publicKeyCredentialJson);

        String requestJson = cache.get(requestId);
        if (requestJson == null) {
            throw new Exception("registration not found");
        }
        cache.remove(requestId);

        try {
            PublicKeyCredentialCreationOptions request = PublicKeyCredentialCreationOptions.fromJson(requestJson);

            RegistrationResult result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                    .request(request)  // The PublicKeyCredentialCreationOptions from startRegistration above
                    // NOTE: Must be stored in server memory or otherwise protected against tampering
                    .response(pkc)
                    .build());

            UserIdentity userIdentity = request.getUser();

            User user = User.builder()
                    .name(userIdentity.getName())
                    .displayName(userIdentity.getDisplayName())
                    .build();
            userRepository.save(user);

            UserCredential userCredential = UserCredential.builder()
                    .user(user)
                    .credentialId(result.getKeyId().getId().getBase64())
                    .userHandle(userIdentity.getId().getBase64())
                    .publicKeyCose(result.getPublicKeyCose().getBase64())
                    .signatureCount(result.getSignatureCount())
                    .backupEligible(result.isBackupEligible())
                    .backupState(result.isBackedUp())
                    .isDiscoverable(result.isDiscoverable().orElse(false))
                    .attestationObject(pkc.getResponse().getAttestationObject().getBase64()) // Store attestation object for future reference
                    .clientDataJson(pkc.getResponse().getClientDataJSON().getBase64())    // Store client data for re-verifying signature if needed
                    .build();
            userCredentialRepository.save(userCredential);

        } catch (RegistrationFailedException e) {
            throw e;
        } finally {
            cache.remove(pkc.getId().getBase64Url());
        }
    }

    private static ByteArray generateRandom(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }
}
