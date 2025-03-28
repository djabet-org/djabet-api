package hello.infrastructure;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hello.Constants;
import hello.domain.PartidaOdds;
import hello.dto.Bookmaker;
import hello.dto.Market;
import hello.dto.Odd;
import hello.dto.Outcome;
import hello.domain.Partida;
import hello.dto.Torneio;
import hello.model.EVFilter;
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

    private Logger _log = Logger.getLogger(getClass().getName());

    public List<Torneio> getAllTorneios(EVFilter evFilter) throws Throwable {
        String url = String.format("%s?apiKey=%s",
                theOddsApiBaseUrl, apiKey, evFilter.getMarkets());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        JsonNode response = new ObjectMapper().readTree(responseEntity.getBody());
        return _toStream(response)
                .map(this::_toTorneio)
                .filter(torneio -> !torneio.isHasOutrights())
                .collect(Collectors.toList());
    }

    public List<PartidaOdds> getEventOdds(String sportkey, String partidaId, String markets) {
        try {
            String url = String.format(
                    "https://api.the-odds-api.com/v4/sports/%s/events/%s/odds?apiKey=%s&markets=%s&regions=eu",
                    sportkey, partidaId, apiKey, markets, bookmakers);

            JsonNode oddsNode = _httpClient.get(url);
            return new TheOddsAPIOddsAdapter().adapt(oddsNode, EVFilter.builder().build());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<PartidaOdds> getUpcomingOdds(EVFilter evFilter) throws JsonMappingException, JsonProcessingException {
        StringBuilder urlBuilder = new StringBuilder(theOddsApiBaseUrl);
        urlBuilder.append("/upcoming/odds?apiKey="+apiKey);
        urlBuilder.append("&regions=eu");
        urlBuilder.append("&dateFormat=unix");
        urlBuilder.append("&markets="+evFilter.getMarkets());
        if (!evFilter.getBookmakers().isBlank()) {
            urlBuilder.append("&bookmakers="+evFilter.getBookmakers());
        }

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlBuilder.toString(), String.class);
        JsonNode odds = new ObjectMapper().readTree(responseEntity.getBody());
        return new TheOddsAPIOddsAdapter().adapt(odds, evFilter);
    }

    public List<PartidaOdds> getSportOdds(EVFilter evFilter) throws Throwable {
        String sport = evFilter.getSports();
        try {
            return getAllTorneios(evFilter).stream()
                    .filter(torneio -> torneio.getGroup().toLowerCase().contains(sport.toLowerCase()))
                    .limit(3)
                    .map(Torneio::getKey)
                    .peek(System.out::println)
                    .map(sportKey -> _getOdd(sportKey, evFilter.getMarkets()))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<PartidaOdds> _getOdd(String sport, String markets) {
        try {
            String url = String.format("%s/%s/odds?apiKey=%s&markets=%s&regions=eu&dateFormat=unix",
                    theOddsApiBaseUrl, sport, apiKey, markets);

            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            JsonNode odds = new ObjectMapper().readTree(responseEntity.getBody());
            return new TheOddsAPIOddsAdapter().adapt(odds, EVFilter.builder().build());
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Collections.emptyList();

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

    private Market _toMarket(JsonNode marketJsonNode) {
        return Market.builder()
                .key(marketJsonNode.get("key").asText())
                .outcomes(_toStream(marketJsonNode.get("outcomes")).map(this::_toOutcome).collect(Collectors.toList()))
                .build();
    }

    private Bookmaker _toBookmaker(JsonNode bookmaker) {
        return Bookmaker.builder()
                .key(bookmaker.get("key").asText())
                .name(bookmaker.get("title").asText())
                .markets(_toStream(bookmaker.get("markets")).map(this::_toMarket).collect(Collectors.toList()))
                .build();
    }

    public List<Partida> getUpcomingPartidas(EVFilter evFilter) throws JsonMappingException, JsonProcessingException {
        return getUpcomingOdds(evFilter).stream().map(PartidaOdds::getPartida).collect(Collectors.toList());
    }
}
