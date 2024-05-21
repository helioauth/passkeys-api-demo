package com.helioauth.passkeys.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.demo.contract.CreateCredentialResponse;
import com.helioauth.passkeys.demo.domain.User;
import com.helioauth.passkeys.demo.domain.UserCredential;
import com.helioauth.passkeys.demo.domain.UserCredentialRepository;
import com.helioauth.passkeys.demo.domain.UserRepository;
import com.helioauth.passkeys.demo.service.exception.UsernameAlreadyRegisteredException;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserAuthenticator {

    private final RelyingParty relyingParty;

    private final UserCredentialRepository userCredentialRepository;

    private final UserRepository userRepository;

    // TODO Expire cached requests
    private final Map<String, String> cache = new HashMap<>();

    private static final SecureRandom random = new SecureRandom();

//    UserAuthenticator(
//            DatabaseCredentialRepository databaseCredentialRepository,
//            UserCredentialRepository userCredentialRepository,
//            UserRepository userRepository
//    ) {
//
//        this.userCredentialRepository = userCredentialRepository;
//        this.userRepository = userRepository;
//    }

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

        cache.putIfAbsent(id.getBase64Url(), request.toJson());

        return CreateCredentialResponse.builder()
                .requestId(id)
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
                    .credentialId(result.getKeyId().getId().getBase64Url())
                    .userHandle(userIdentity.getId().getBase64Url())
                    .publicKeyCose(result.getPublicKeyCose().getBase64Url())
                    .signatureCount(result.getSignatureCount())
                    .backupEligible(result.isBackupEligible())
                    .backupState(result.isBackedUp())
                    .isDiscoverable(result.isDiscoverable().orElse(false))
                    .attestationObject(pkc.getResponse().getAttestationObject().getBase64Url()) // Store attestation object for future reference
                    .clientDataJson(pkc.getResponse().getClientDataJSON().getBase64Url())    // Store client data for re-verifying signature if needed
                    .build();
            userCredentialRepository.save(userCredential);

//            storeCredential(              // Some database access method of your own design
//                    "alice",                    // Username or other appropriate user identifier
//                    result.getKeyId(),          // Credential ID and transports for allowCredentials
//                    result.getPublicKeyCose(),  // Public key for verifying authentication signatures
//                    result.getSignatureCount(), // Initial signature counter value
//                    result.isDiscoverable(),    // Is this a passkey?
//                    result.isBackupEligible(),  // Can this credential be backed up (synced)?
//                    result.isBackedUp(),        // Is this credential currently backed up?
//                    pkc.getResponse().getAttestationObject(), // Store attestation object for future reference
//                    pkc.getResponse().getClientDataJSON()     // Store client data for re-verifying signature if needed
//            );
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
