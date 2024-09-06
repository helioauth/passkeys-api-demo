package com.helioauth.passkeys.demo.client;

public interface PasskeysApiClient {
    void signUpFinish(SignUpFinishRequest signupFinishRequest);

    void signInFinish(SignInFinishRequest signinFinishRequest);
}
