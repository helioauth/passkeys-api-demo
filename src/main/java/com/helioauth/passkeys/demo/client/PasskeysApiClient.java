package com.helioauth.passkeys.demo.client;

import java.io.IOException;
import java.util.UUID;

public interface PasskeysApiClient {
    SignUpFinishResponse signUpFinish(SignUpFinishRequest signupFinishRequest) throws IOException;

    SignInFinishResponse signInFinish(SignInFinishRequest signinFinishRequest) throws IOException;

    ListPasskeysResponse listPasskeys(UUID userId) throws IOException;
}
