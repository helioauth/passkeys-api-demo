package com.helioauth.passkeys.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "registered_credential")
@Builder
@Getter
@Setter
@AllArgsConstructor
public class UserCredential {
    @Id
    private Long id;

    @Column
    private String credentialId;

    @Column
    private String userHandle;

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

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected UserCredential() {

    }
}
