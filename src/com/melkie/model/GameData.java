package com.melkie.model;

import java.util.List;

/** personas.json 전체를 로드한 결과를 담는 컨테이너. */
public class GameData {
    private final List<Persona> personas;
    private final GameConfig config;

    public GameData(List<Persona> personas, GameConfig config) {
        this.personas = personas;
        this.config = config;
    }

    public List<Persona> getPersonas() { return personas; }
    public GameConfig getConfig() { return config; }
}
