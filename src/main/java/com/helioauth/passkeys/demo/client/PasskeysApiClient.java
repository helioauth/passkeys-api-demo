package com.helioauth.passkeys.demo.client;

import java.io.IOException;

public interface PasskeysApiClient {
    SignUpFinishResponse signUpFinish(SignUpFinishRequest signupFinishRequest) throws IOException;

    void signInFinish(SignInFinishRequest signinFinishRequest) throws IOException;
}
