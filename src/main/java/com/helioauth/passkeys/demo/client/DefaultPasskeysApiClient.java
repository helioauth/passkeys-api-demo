package com.helioauth.passkeys.demo.client;

import lombok.val;
import okhttp3.*;
import okhttp3.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

public class DefaultPasskeysApiClient implements PasskeysApiClient {
    public static final String SIGNUP_FINISH_ENDPOINT = "v1/signup/finish";
    public static final String SIGNIN_FINISH_ENDPOINT = "v1/signin/finish";
    public static final String LIST_PASSKEYS_ENDPOINT = "v1/users/%s/credentials";

    private final OkHttpClient client = new OkHttpClient();
    private final String apiUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public DefaultPasskeysApiClient(String apiUrl) {
        this.apiUrl = apiUrl.endsWith("/") ? apiUrl : apiUrl + "/";
    }

    @Override
    public SignUpFinishResponse signUpFinish(SignUpFinishRequest request) throws IOException {
        val response = post(objectMapper.writeValueAsString(request), SIGNUP_FINISH_ENDPOINT);
        return objectMapper.readValue(response, SignUpFinishResponse.class);
    }

    @Override
    public SignInFinishResponse signInFinish(SignInFinishRequest request) throws IOException {
        val response = post(objectMapper.writeValueAsString(request), SIGNIN_FINISH_ENDPOINT);
        return objectMapper.readValue(response, SignInFinishResponse.class);
    }

    @Override
    public ListPasskeysResponse listPasskeys(UUID userId) throws IOException {
        val response = get(LIST_PASSKEYS_ENDPOINT.formatted(userId.toString()));
        return objectMapper.readValue(response, ListPasskeysResponse.class);
    }

    private String post(String json, String endpoint) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
            .url(apiUrl + endpoint)
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new PasskeyApiException("Unexpected code %d %s".formatted(response.code(), response.message()));
            }

            return response.body().string();
        }
    }

    private String get(String endpoint) throws IOException {
        Request request = new Request.Builder()
            .url(apiUrl + endpoint)
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new PasskeyApiException("Unexpected code %d %s".formatted(response.code(), response.message()));
            }

            return response.body().string();
        }
    }
}
