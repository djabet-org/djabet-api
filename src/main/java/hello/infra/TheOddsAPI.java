package hello.infra;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import hello.Constants;
import hello.dto.Bookmaker;
import hello.dto.Market;
import hello.dto.Outcome;
import hello.dto.Partida;
import hello.dto.PartidaOdds;
import hello.dto.Torneio;
import hello.service.Helper;
import hello.service.HttpClient;

@Service
public class TheOddsAPI {

    @Value("${theodds.api.key}")
    private String apiKey;

    @Value("${theodds.api.base.url}")
    private String theOddsApiBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    private final HttpClient _httpClient = new HttpClient();

    private final String bookmakers = _getSelectedBookmakers();

    private final String SOCCER_SPORT = "soccer_brazil_campeonato";
        private final static String MARKETS = "h2h,spreads,totals";

    public List<Torneio> getAllTorneios() throws IOException {

        String torneiosURL = String.format("https://api.the-odds-api.com/v4/sports?apiKey=%s", apiKey);
        return _toStream(_httpClient.get(torneiosURL))
                // .peek(System.out::println)
                .map(this::_toTorneio)
                .filter(
                        torneio -> 
                                torneio.getGroup().equalsIgnoreCase(Constants.SOCCER_GROUP)
                                // torneio.getGroup().equalsIgnoreCase(Constants.CRICKET_GROUP) ||
                                // torneio.getGroup().equalsIgnoreCase(Constants.TENNIS_GROUP) ||
                                // torneio.getGroup().equalsIgnoreCase(Constants.BASKETBALL_GROUP) ||
                                // torneio.getGroup().equalsIgnoreCase(Constants.BASEBALL_GROUP)
                                )
                .filter(torneio -> !torneio.isHasOutrights())
                .collect(Collectors.toList());
    }

    public List<Torneio> getSoccerTorneios() throws IOException {
        return getAllTorneios().stream()
                .filter(torneio -> torneio.getGroup()
                        .equals(Constants.SOCCER_GROUP))
                .collect(Collectors.toList());
    }

    public List<Torneio> getBasketballTorneios() throws IOException {
        return getAllTorneios().stream()
                .filter(torneio -> torneio.getGroup()
                        .equals(Constants.BASKETBALL_GROUP))
                .collect(Collectors.toList());
    }

    public List<Torneio> getTennisTorneios() throws IOException {
        return getAllTorneios().stream()
                .filter(torneio -> torneio.getGroup()
                        .equals(Constants.TENNIS_GROUP))
                .collect(Collectors.toList());
    }

    public List<Partida> getPartidas(String torneio) {
        try {
            String eventsURL = String.format("https://api.the-odds-api.com/v4/sports/%s/events?apiKey=%s&all=true", torneio,
                    apiKey);
            return _toStream(_httpClient.get(eventsURL))
                    // .peek(System.out::println)
                    .map( node -> new TheOddsAPIPartidaAdapter().adapt(node))
                    .filter( partida -> Helper.happensInTwoDays(partida.getHorario()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Partida> getSoccerPartidas() throws IOException {
        return getPartidas(SOCCER_SPORT);
    }

    public PartidaOdds getOdds(Partida partida, String markets) {
        try {
            String url = String.format(
                    "https://api.the-odds-api.com/v4/sports/%s/events/%s/odds?apiKey=%s&markets=%s&regions=eu,uk",
                    partida.getSportKey(), partida.getId(), apiKey, markets, bookmakers);

            JsonNode odd = _httpClient.get(url);
            // System.out.println(odd.toPrettyString());
            return _toPartidaOdds(odd);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<PartidaOdds> getUpcomingOdds() {
            String url = String.format(
                    "%s/upcoming/odds?apiKey=%s&markets=%s&regions=eu,uk",
                    theOddsApiBaseUrl, apiKey, MARKETS);

            JsonNode odds = restTemplate.getForObject(url, JsonNode.class);
            // System.out.println(odds.toPrettyString());
            return StreamSupport.stream(odds.spliterator(), false).map(this::_toPartidaOdds).collect(Collectors.toList());
    }

    private PartidaOdds _toPartidaOdds(JsonNode odd) {
        return PartidaOdds.builder()
                .partida( new TheOddsAPIPartidaAdapter().adapt(odd))
                .bookmakers(_toStream(odd.get("bookmakers")).map(this::_toBookmaker)
                        .collect(Collectors.toList()))
                .build();

    }

    private Stream<JsonNode> _toStream(JsonNode node) {
        return StreamSupport.stream(node.spliterator(), false);
    }

    private String _getSelectedBookmakers() {
        return Arrays.asList(
                // "sport888",
                // "sbobet",
                // "betfair",
                // "betsson",
                // "suprabets",
                // "betway",
                "pinnacle",
                "onexbet").stream().collect(Collectors.joining(","));
    }

    private Torneio _toTorneio(JsonNode torneioNode) {
        return Torneio.builder()
                .key(torneioNode.get("key").asText())
                .group(torneioNode.get("group").asText())
                .title(torneioNode.get("title").asText())
                .description(torneioNode.get("description").asText())
                .active(torneioNode.get("active").asBoolean())
                .hasOutrights(torneioNode.get("has_outrights").asBoolean())
                .build();
    }

    private Market _toMarket(JsonNode marketJsonNode) {
        return Market.builder()
                .key(marketJsonNode.get("key").asText())
                .outcomes(_toStream(marketJsonNode.get("outcomes")).map(this::_toOutcome).collect(Collectors.toList()))
                .build();
    }

    private Outcome _toOutcome(JsonNode outcomeNode) {
        String totalsPoint = outcomeNode.get("point") != null ? Double.toString(outcomeNode.get("point").asDouble()) : "";
        // String totalsPointString = totalsPoint == 0? "":Double.toString(totalsPoint);
        // String totalsPoint = Optional.ofNullable(outcomeNode.get("point").asDouble())
        //         .filter(Objects::nonNull)
        //         .map(pointDouble -> {
        //             System.out.println(pointDouble);
        //             return pointDouble.toString();
        //         })
        //         .orElse("");

        return Outcome.builder()
                .name(outcomeNode.get("name").asText() + " " + totalsPoint)
                .odd(outcomeNode.get("price").asDouble())
                .build();
    }

    private Bookmaker _toBookmaker(JsonNode bookmaker) {
        return Bookmaker.builder()
                .key(bookmaker.get("key").asText())
                .name(bookmaker.get("title").asText())
                .markets(_toStream(bookmaker.get("markets")).map(this::_toMarket).collect(Collectors.toList()))
                .build();
    }

    public List<Partida> getUpcomingPartidas() {
        return getUpcomingOdds().stream().map(PartidaOdds::getPartida).collect(Collectors.toList());
    }
}
