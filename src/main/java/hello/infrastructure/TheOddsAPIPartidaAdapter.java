package hello.infrastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import hello.domain.Partida;
import hello.domain.PartidaOdds;
import hello.domain.PartidaOdds.PartidaOddsBuilder;
import hello.domain.Odd;
import hello.dto.Outcome;

public class TheOddsAPIPartidaAdapter implements PartidaAdapter {

    private Logger _log = Logger.getLogger(getClass().getName());

    @Override
    public List<PartidaOdds> adapt(JsonNode oddsJsonNode) {
        List<PartidaOdds> partidasOdds = new ArrayList<>();

        for (JsonNode oddNode : oddsJsonNode) {
            List<Odd> odds = new ArrayList<>();
            Partida partida = _toPartida(oddNode);
            for (JsonNode bookmakerNode : oddNode.get("bookmakers")) {
                String bookmaker = bookmakerNode.get("key").asText();
                for (JsonNode marketNode : bookmakerNode.get("markets")) {
                    String market = marketNode.get("key").asText();
                    for (JsonNode outcomeNode : marketNode.get("outcomes")) {
                        Outcome outcome = _toOutcome(outcomeNode);
                        Odd odd = Odd.builder().odd(outcome.getOdd()).outcome(outcome.getName()).bookmaker(bookmaker)
                                .market(market).build();
                        odds.add(odd);
                    
                }
            }
        }
                    partidasOdds.add(PartidaOdds.builder().partida(partida).odds(odds).build());
    }

        // partidasOdds.forEach(p -> System.out.println(p + "\n\n"));

        return partidasOdds;
        // return Collections.emptyList();

    }

    private Outcome _toOutcome(JsonNode outcomeNode) {
        String totalsPoint = outcomeNode.get("point") != null ? Double.toString(outcomeNode.get("point").asDouble())
                : "";
        // String totalsPointString = totalsPoint == 0? "":Double.toString(totalsPoint);
        // String totalsPoint = Optional.ofNullable(outcomeNode.get("point").asDouble())
        // .filter(Objects::nonNull)
        // .map(pointDouble -> {
        // System.out.println(pointDouble);
        // return pointDouble.toString();
        // })
        // .orElse("");

        return Outcome.builder()
                .name(outcomeNode.get("name").asText() + " " + totalsPoint)
                .odd(outcomeNode.get("price").asDouble())
                .build();
    }

    private Partida _toPartida(JsonNode partidaJsonNode) {
        return Partida.builder()
                .id(partidaJsonNode.get("id").asText())
                .name(partidaJsonNode.get("home_team").asText() + " vs " +
                        partidaJsonNode.get("away_team").asText())
                .sportKey(partidaJsonNode.get("sport_key").asText())
                .torneio(partidaJsonNode.get("sport_title").asText())
                .awayTeam(partidaJsonNode.get("away_team").asText())
                .homeTeam(partidaJsonNode.get("home_team").asText())
                // .horario(partidaJsonNode.get("commence_time").asText())
                .build();

    }

}