package com.helioauth.passkeys.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "demo_users")
@Builder
@Getter
@Setter
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column
    private String name;

    @Column
    private String displayName;

    @Column
    private String password;

    @Column
    private Boolean enabled;

    @Column
    private UUID externalId;

    protected User() {

    }
}
