package hello.infrastructure;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import hello.domain.PartidaOdds;
import hello.dto.Odd;
import hello.dto.Partida;

public interface PartidaAdapter {
    List<PartidaOdds> adapt(JsonNode node);
}
