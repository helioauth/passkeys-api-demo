package com.helioauth.passkeys.demo.mapper;

import com.helioauth.passkeys.demo.controller.UserCredentialDTO;
import com.helioauth.passkeys.demo.domain.UserCredential;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserCredentialMapper {
    List<UserCredentialDTO> toDto(List<UserCredential> userCredentialList);
}