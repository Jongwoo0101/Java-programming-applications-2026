package com.melkie.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Groq API 키와 사용할 모델명을 다음 우선순위로 로드한다.
 *   1) 환경변수 GROQ_API_KEY / GROQ_MODEL (우선순위 최상위)
 *   2) resources/groq.properties 파일
 */
public final class GroqConfigLoader {

    private static final String ENV_API_KEY = "GROQ_API_KEY";
    private static final String ENV_MODEL = "GROQ_MODEL";
    private static final String DEFAULT_PROPERTIES_PATH = "resources/groq.properties";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";

    private GroqConfigLoader() {}

    public static String loadApiKey() {
        String fromEnv = System.getenv(ENV_API_KEY);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        return readProperty("groq.api.key");
    }

    public static String loadModel() {
        String fromEnv = System.getenv(ENV_MODEL);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromFile = readProperty("groq.model");
        return (fromFile != null && !fromFile.isBlank()) ? fromFile : DEFAULT_MODEL;
    }

    private static String readProperty(String key) {
        Path path = Path.of(DEFAULT_PROPERTIES_PATH);
        if (!Files.exists(path)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            return null;
        }
        String value = props.getProperty(key);
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}