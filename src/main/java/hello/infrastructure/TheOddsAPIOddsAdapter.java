package hello.infrastructure;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

import hello.domain.Odd;
import hello.domain.Partida;
import hello.domain.PartidaOdds;
import hello.model.EVFilter;
import hello.domain.Outcome;

public class TheOddsAPIOddsAdapter implements TheOddsAPIOddsAdapterService {

    private Logger _log = Logger.getLogger(getClass().getName());

    @Override
    public List<PartidaOdds> adapt(JsonNode oddsJsonNode, EVFilter evFilter) {
        List<PartidaOdds> partidasOdds = new ArrayList<>();

        for (JsonNode oddNode : oddsJsonNode) {
            List<Odd> odds = new ArrayList<>();
            Partida partida = _toPartida(oddNode);
            for (JsonNode bookmakerNode : oddNode.get("bookmakers")) {
                String bookmaker = bookmakerNode.get("key").asText();
                Optional<JsonNode> optLink = Optional.ofNullable(bookmakerNode.get("link"));
                for (JsonNode marketNode : bookmakerNode.get("markets")) {
                    String market = marketNode.get("key").asText();
                    for (JsonNode outcomeNode : marketNode.get("outcomes")) {
                        Outcome outcome = _toOutcome(outcomeNode);
                        Odd odd = Odd.builder()
                            .odd(outcome.getOdd())
                            .outcome(outcome)
                            .bookmaker(bookmaker)
                            .market(market)
                            .link(optLink.map(JsonNode::asText).orElse(""))
                            .build();
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
        double totalsPoint = outcomeNode.has("point") ? outcomeNode.get("point").asDouble() : 0;
        String name = totalsPoint > 0 ? outcomeNode.get("name").asText() + " " + totalsPoint 
                : outcomeNode.get("name").asText();

        return Outcome.builder()
                .name(name)
                .odd(outcomeNode.get("price").asDouble())
                .totalsPoint(totalsPoint)
                .build();
    }

    private Partida _toPartida(JsonNode partidaJsonNode) {
        long timeInUnix = partidaJsonNode.get("commence_time").asLong();

        return Partida.builder()
                .id(partidaJsonNode.get("id").asText())
                .name(partidaJsonNode.get("home_team").asText() + " vs " +
                        partidaJsonNode.get("away_team").asText())
                .sportKey(partidaJsonNode.get("sport_key").asText())
                .torneio(partidaJsonNode.get("sport_title").asText())
                .awayTeam(partidaJsonNode.get("away_team").asText())
                .homeTeam(partidaJsonNode.get("home_team").asText())
                .horario(_toISO(timeInUnix))
                .horarionUnix(timeInUnix)
                .build();

    }

    private String _toISO(long timeInUnix) {
        // Convert unix timestamp to Instant
        Instant timeInstant = Instant.ofEpochSecond(timeInUnix);
        ZoneId brasiliaZoneId = ZoneId.of("America/Sao_Paulo"); // Replace with "America/Sao_Paulo" if needed
        LocalDateTime horarioBrasilia = timeInstant.atZone(brasiliaZoneId).toLocalDateTime();
        return horarioBrasilia.toString();
    }
}