package hello.infra;

import com.fasterxml.jackson.databind.JsonNode;

import hello.dto.Partida;

public class TheOddsAPIPartidaAdapter implements PartidaAdapter {

    @Override
    public Partida adapt(JsonNode partidaJsonNode) {
        return Partida.builder()
                .id(partidaJsonNode.get("id").asText())
                .name(partidaJsonNode.get("home_team").asText() + " vs " +
                        partidaJsonNode.get("away_team").asText())
                .sportKey(partidaJsonNode.get("sport_key").asText())
                .torneio(partidaJsonNode.get("sport_title").asText())
                .awayTeam(partidaJsonNode.get("away_team").asText())
                .homeTeam(partidaJsonNode.get("home_team").asText())
                .horario(partidaJsonNode.get("commence_time").asText())
                .build();
    }

}