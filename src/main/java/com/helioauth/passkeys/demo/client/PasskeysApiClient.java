package com.helioauth.passkeys.demo.client;

import java.io.IOException;

public interface PasskeysApiClient {
    SignUpFinishResponse signUpFinish(SignUpFinishRequest signupFinishRequest) throws IOException;

    SignInFinishResponse signInFinish(SignInFinishRequest signinFinishRequest) throws IOException;
}
