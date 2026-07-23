package com.melkie.llm;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class GroqClient {

    // Groq는 OpenAI 호환 엔드포인트를 제공합니다.
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;

    public GroqClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String getModel() {
        return model;
    }

    public String generateContent(String requestBodyJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json; charset=utf-8")
                // Groq/OpenAI는 헤더에 Bearer 토큰으로 인증합니다.
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new IOException("Groq API 호출 실패 (HTTP " + response.statusCode() + "): " + response.body());
        }
        return response.body();
    }
}