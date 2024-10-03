package hello.infra;

import com.fasterxml.jackson.databind.JsonNode;

import hello.dto.Partida;

public interface PartidaAdapter {
    Partida adapt(JsonNode node);
}
