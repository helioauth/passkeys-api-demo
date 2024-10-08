package com.helioauth.passkeys.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "demo_registered_credential")
@Builder
@Getter
@Setter
@AllArgsConstructor
public class UserCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String credentialId;

    @Column
    private String userHandle;

    @Column
    private String displayName;

    @Column
    private Long signatureCount;

    @Column(columnDefinition = "text")
    private String publicKeyCose;

    @Column(columnDefinition = "text")
    private String attestationObject;

    @Column(columnDefinition = "text")
    private String clientDataJson;

    @Column
    private Boolean backupEligible;

    @Column
    private Boolean backupState;

    @Column
    private Boolean isDiscoverable;

    @Column
    private Instant lastUsedAt;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected UserCredential() {

    }
}
