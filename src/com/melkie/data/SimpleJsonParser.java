package com.melkie.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MelKie 프로젝트의 "제로 의존성" 철학에 따라 외부 JSON 라이브러리(Gson, Jackson 등)를
 * 전혀 사용하지 않고, 순수 Java SE 표준 API만으로 직접 구현한 재귀 하강(Recursive Descent)
 * JSON 파서입니다.
 *
 * 반환 타입 매핑:
 *   object -> LinkedHashMap<String, Object>
 *   array  -> ArrayList<Object>
 *   string -> String
 *   number -> Double
 *   true/false -> Boolean
 *   null -> null
 */
public final class SimpleJsonParser {

    private final String src; // 파싱할 원본 JSON 문자열
    private int pos; // 현재 읽고 있는 문자 위치(커서). 파싱이 진행될수록 계속 증가

    private SimpleJsonParser(String src) {
        this.src = src;
        this.pos = 0;
    }

    public static Object parse(String json) {
        SimpleJsonParser parser = new SimpleJsonParser(json);
        parser.skipWhitespace();
        Object result = parser.parseValue();
        parser.skipWhitespace();
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObjectRoot(String json) {
        Object result = parse(json);
        if (!(result instanceof Map)) {
            throw new IllegalArgumentException("JSON 루트가 객체가 아닙니다.");
        }
        return (Map<String, Object>) result;
    }

    private Object parseValue() {
        skipWhitespace();
        if (pos >= src.length()) {
            throw error("예기치 못한 JSON 종료");
        }
        char c = src.charAt(pos);
        switch (c) {
            case '{': return parseObject();
            case '[': return parseArray();
            case '"': return parseString();
            case 't':
            case 'f': return parseBoolean();
            case 'n': return parseNull();
            default: return parseNumber();
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
            } else if (c == '}') {
                pos++;
                break;
            } else {
                throw error("객체 파싱 중 ',' 또는 '}' 기대");
            }
        }
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            Object value = parseValue();
            list.add(value);
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
            } else if (c == ']') {
                pos++;
                break;
            } else {
                throw error("배열 파싱 중 ',' 또는 ']' 기대");
            }
        }
        return list;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= src.length()) {
                throw error("문자열이 닫히지 않았습니다.");
            }
            char c = src.charAt(pos++);
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        String hex = src.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                        break;
                    default:
                        throw error("알 수 없는 escape 문자: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Double parseNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        if (pos < src.length() && src.charAt(pos) == '.') {
            pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        }
        if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
            pos++;
            if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        }
        String numStr = src.substring(start, pos);
        if (numStr.isEmpty()) {
            throw error("숫자 파싱 실패");
        }
        return Double.parseDouble(numStr);
    }

    private Boolean parseBoolean() {
        if (src.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        } else if (src.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        throw error("boolean 파싱 실패");
    }

    private Object parseNull() {
        if (src.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw error("null 파싱 실패");
    }

    private void expect(char expected) {
        skipWhitespace();
        if (pos >= src.length() || src.charAt(pos) != expected) {
            throw error("'" + expected + "' 문자를 기대했습니다.");
        }
        pos++;
    }

    private char peek() {
        skipWhitespace();
        if (pos >= src.length()) {
            throw error("예기치 못한 JSON 종료");
        }
        return src.charAt(pos);
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private RuntimeException error(String msg) {
        int lineStart = Math.max(0, pos - 20);
        int lineEnd = Math.min(src.length(), pos + 20);
        String context = src.substring(lineStart, lineEnd);
        return new IllegalArgumentException("JSON 파싱 오류(위치 " + pos + "): " + msg + " ... 주변: " + context);
    }
}
