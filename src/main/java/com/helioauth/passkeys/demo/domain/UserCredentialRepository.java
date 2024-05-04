package com.helioauth.passkeys.demo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findFirstByUserName(String username);

    List<UserCredential> findAllByUserHandle(String base64EncodedUserHandle);

    Optional<UserCredential> findFirstByUserHandleAndCredentialId(String base64UserHandle, String base64CredentialId);
}
