package com.helioauth.passkeys.demo.client;

import java.io.IOException;

public interface PasskeysApiClient {
    void signUpFinish(SignUpFinishRequest signupFinishRequest);

    void signInFinish(SignInFinishRequest signinFinishRequest) throws IOException;
}
