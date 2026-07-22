package com.melkie.data;

import com.melkie.model.Gender;
import com.melkie.model.GameConfig;
import com.melkie.model.GameData;
import com.melkie.model.Persona;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * personas.json 파일을 읽어 SimpleJsonParser로 파싱한 뒤,
 * 제네릭 트리(Map/List/Double/String)를 타입 안전한 Persona / GameConfig 객체로 변환한다.
 * Jackson/Gson 등 외부 매핑 라이브러리 없이 순수 자바 반복문으로 직접 매핑한다.
 */
public final class PersonaRepository {

    private PersonaRepository() {}

    public static GameData loadFromPath(String path) throws IOException {
        String json = new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
        return parse(json);
    }

    public static GameData loadFromClasspath(String resourceName) throws IOException {
        try (InputStream is = PersonaRepository.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("클래스패스에서 리소스를 찾을 수 없습니다: " + resourceName);
            }
            byte[] bytes = is.readAllBytes();
            return parse(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    @SuppressWarnings("unchecked")
    public static GameData parse(String json) {
        Map<String, Object> root = SimpleJsonParser.parseObjectRoot(json);

        Map<String, Integer> globalPositive = toIntMap((Map<String, Object>) root.get("globalPositiveKeywords"));
        Map<String, Integer> globalNegative = toIntMap((Map<String, Object>) root.get("globalNegativeKeywords"));
        Map<String, Integer> dominanceWords = toIntMap((Map<String, Object>) root.get("dominanceWords"));
        Map<String, Integer> submissionWords = toIntMap((Map<String, Object>) root.get("submissionWords"));
        Map<String, Integer> assertiveWords = toIntMap((Map<String, Object>) root.get("assertiveWords"));

        Map<String, Object> prefixesRaw = (Map<String, Object>) root.get("prefixes");
        Map<String, List<String>> prefixes = new LinkedHashMap<>();
        if (prefixesRaw != null) {
            for (Map.Entry<String, Object> e : prefixesRaw.entrySet()) {
                prefixes.put(e.getKey(), toStringList((List<Object>) e.getValue()));
            }
        }

        GameConfig config = new GameConfig(globalPositive, globalNegative, dominanceWords,
                submissionWords, assertiveWords, prefixes);

        List<Object> personasRaw = (List<Object>) root.get("personas");
        List<Persona> personas = new ArrayList<>();
        if (personasRaw != null) {
            for (Object personaObj : personasRaw) {
                personas.add(toPersona((Map<String, Object>) personaObj));
            }
        }

        return new GameData(personas, config);
    }

    @SuppressWarnings("unchecked")
    private static Persona toPersona(Map<String, Object> map) {
        int id = ((Double) map.get("id")).intValue();
        String name = (String) map.get("name");
        int age = ((Double) map.get("age")).intValue();
        String mbti = (String) map.get("mbti");
        Gender gender = Gender.fromJsonValue((String) map.get("gender"));
        String tagline = (String) map.get("tagline");

        List<Object> targetVectorRaw = (List<Object>) map.get("targetVector");
        double[] targetVector = new double[targetVectorRaw.size()];
        for (int i = 0; i < targetVectorRaw.size(); i++) {
            targetVector[i] = ((Double) targetVectorRaw.get(i)).doubleValue();
        }

        Map<String, Integer> loveKeywords = toIntMap((Map<String, Object>) map.get("loveKeywords"));
        Map<String, Integer> hateKeywords = toIntMap((Map<String, Object>) map.get("hateKeywords"));

        Map<String, Object> dialoguesRaw = (Map<String, Object>) map.get("dialogues");
        Map<String, List<String>> dialogues = new LinkedHashMap<>();
        if (dialoguesRaw != null) {
            for (Map.Entry<String, Object> e : dialoguesRaw.entrySet()) {
                dialogues.put(e.getKey(), toStringList((List<Object>) e.getValue()));
            }
        }

        return new Persona(id, name, age, mbti, gender, tagline, targetVector,
                loveKeywords, hateKeywords, dialogues);
    }

    private static Map<String, Integer> toIntMap(Map<String, Object> raw) {
        Map<String, Integer> result = new LinkedHashMap<>();
        if (raw == null) {
            return result;
        }
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            result.put(e.getKey(), ((Double) e.getValue()).intValue());
        }
        return result;
    }

    private static List<String> toStringList(List<Object> raw) {
        List<String> result = new ArrayList<>();
        if (raw == null) {
            return result;
        }
        for (Object o : raw) {
            result.add((String) o);
        }
        return result;
    }
}
