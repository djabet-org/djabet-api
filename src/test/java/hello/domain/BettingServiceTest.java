package hello.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import hello.infrastructure.TheOddsAPI;
import hello.model.EVFilter;

@ExtendWith(MockitoExtension.class)
public class BettingServiceTest {

    private static final String TEAM_HOME = "Sport";
    private static final String TEAM_AWAY = "Nautico";

    @Mock
    private TheOddsAPI theOddsAPI;

    @InjectMocks
    private BettingService bettingService = new BettingServiceImpl();

    private Partida _prematchPartida;

    @BeforeEach
    public void setup() {
        _prematchPartida = _newPartida("prematch-id", Instant.now().plus(Duration.ofHours(2)).toEpochMilli()/1000);
    }

    @Test
    public void itShouldGetOdds() throws JsonMappingException, JsonProcessingException {
        List<PartidaOdds> expecteOdds = _newPartidaOdds();
        EVFilter evFilter = EVFilter.builder().build();
        when(theOddsAPI.getUpcomingOdds()).thenReturn(expecteOdds);
        List<PartidaOdds> odds = bettingService.getOdds(evFilter);

        verify(theOddsAPI).getUpcomingOdds();
        assertEquals(expecteOdds, odds);
    }

    @Test
    public void testGetValueBetsIfPositive() {

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.0)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.2)
                .build();

        PartidaOdds partidaOdd = PartidaOdds.builder().partida(_prematchPartida)
                .odds(List.of(odd1, odd2)).build();

        List<PartidaEVs> result = bettingService.calculateEVs(List.of(partidaOdd), EVFilter.builder().build());

        assertEquals(0, result.size());
    }

    @Test
    public void itShouldGetValueBetsFilteredByMarkets() {

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2o")
                .outcome(TEAM_HOME)
                .odd(3.4)
                .build();

        Odd odd4 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.2)
                .build();

        Odd odd5 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2o")
                .outcome(TEAM_HOME)
                .odd(3.2)
                .build();

        PartidaOdds partidaOdd = PartidaOdds.builder().partida(_prematchPartida)
                .odds(List.of(odd1, odd2, odd4, odd5)).build();

        EVFilter evFilter = EVFilter.builder().markets("h2h").build();
        List<PartidaEVs> result = bettingService.calculateEVs(List.of(partidaOdd), evFilter);

        List<ValueBet> evs = result.get(0).getEvs();
        assertEquals(1, evs.size());
        assertEquals(evs.get(0).getMarket(), "h2h");
    }

    @Test
    public void itShouldGetValueBetsFilteredByEvPercentage() {

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.3)
                .build();

        Odd pinnacleOdd = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.2)
                .build();

        PartidaOdds partidaOdd = PartidaOdds.builder()
                .partida(_prematchPartida)
                .odds(List.of(odd1, odd2, pinnacleOdd))
                .build();

        EVFilter evFilter = EVFilter.builder().minEv(4).build();

        List<PartidaEVs> result = bettingService.calculateEVs(List.of(partidaOdd), evFilter);

        List<ValueBet> evs = result.get(0).getEvs();

        assertEquals(1, evs.size());
        assertTrue(evs.get(0).getEv() >= 4);
    }

    @Test
    public void itShouldGetValueBetsFilteredByOdd() {

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.3)
                .build();

        Odd odd3 = Odd.builder()
                .bookmaker("Bookmaker C")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.8)
                .build();

        Odd pinnacleOdd = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.2)
                .build();

        PartidaOdds partidaOdd = PartidaOdds.builder()
                .partida(_prematchPartida)
                .odds(List.of(odd1, odd2, odd3, pinnacleOdd))
                .build();

        EVFilter evFilter = EVFilter.builder().minOdd(3.32).maxOdd(3.7).build();

        List<PartidaEVs> result = bettingService.calculateEVs(List.of(partidaOdd), evFilter);

        List<ValueBet> evs = result.get(0).getEvs();

        assertEquals(1, evs.size());
        assertEquals(evs.get(0).getBookmaker(), "Bookmaker A");
    }

    @Test
    public void itShouldGetPrematchValueBets() throws JsonMappingException, JsonProcessingException {

        Partida livePartida = _newPartida("live-id",Instant.now().minus(Duration.ofMinutes(20)).toEpochMilli()/1000);

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.3)
                .build();

        Odd pinnacleOdd = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.2)
                .build();

        PartidaOdds prelivePartidaOdd = PartidaOdds.builder()
                .partida(_prematchPartida)
                .odds(List.of(odd1, pinnacleOdd))
                .build();

        PartidaOdds livePartidaOdd = PartidaOdds.builder()
                .partida(livePartida)
                .odds(List.of(odd2, pinnacleOdd))
                .build();

        when(theOddsAPI.getUpcomingOdds()).thenReturn(List.of(prelivePartidaOdd, livePartidaOdd));

        List<PartidaOdds> prematchResult = bettingService.getOdds(EVFilter.builder().prematch(true).build());

    assertEquals(1, prematchResult.size());

    assertEquals("prematch-id", prematchResult.get(0).getPartida().getId());
    }

    @Test
    public void itShouldGetLiveValueBets() throws JsonMappingException, JsonProcessingException {

        Partida livePartida = _newPartida("live-id",Instant.now().minus(Duration.ofMinutes(20)).toEpochMilli()/1000);

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.3)
                .build();

        Odd pinnacleOdd = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.2)
                .build();

        PartidaOdds prelivePartidaOdd = PartidaOdds.builder()
                .partida(_prematchPartida)
                .odds(List.of(odd1, pinnacleOdd))
                .build();

        PartidaOdds livePartidaOdd = PartidaOdds.builder()
                .partida(livePartida)
                .odds(List.of(odd2, pinnacleOdd))
                .build();

        when(theOddsAPI.getUpcomingOdds()).thenReturn(List.of(prelivePartidaOdd, livePartidaOdd));

        List<PartidaOdds> liveResult = bettingService.getOdds(EVFilter.builder().live(true).build());
        List<PartidaOdds> allResult = bettingService.getOdds(EVFilter.builder().build());

    assertEquals(1, liveResult.size());
    assertEquals(2, allResult.size());

    assertEquals("live-id", liveResult.get(0).getPartida().getId());

    }

    @Test
    public void itShouldGetSurebets() {

        Odd odd1 = Odd.builder()
                .bookmaker("Bet365")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(1.22)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bet365")
                .market("h2h")
                .outcome(TEAM_AWAY)
                .odd(3.7)
                .build();

        Odd odd3 = Odd.builder()
                .bookmaker("Betano")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(1.44)
                .build();

        Odd odd4 = Odd.builder()
                .bookmaker("Betano")
                .market("h2h")
                .outcome(TEAM_AWAY)
                .odd(2.89)
                .build();

        PartidaOdds partidaOdd = PartidaOdds.builder().partida(_prematchPartida)
                .odds(List.of(odd1, odd2, odd3, odd4)).build();

        List<PartidaArbs> result = bettingService.getArbs(List.of(partidaOdd), EVFilter.builder().build());
        List<ArbBet> arbs = result.get(0).getArbs();
        System.out.println("creu "+arbs);

        assertEquals(1, result.size());
        assertEquals(2, arbs.size());
        assertEquals(0.035, arbs.get(0).getArb(), 0.001);
        assertEquals(0.383, arbs.get(1).getArb(),0.001);

    }

    private Partida _newPartida(String id, long timeUnix) {
        return Partida.builder()
                .awayTeam(TEAM_AWAY)
                .homeTeam(TEAM_HOME)
                .horarionUnix(timeUnix)
                .horario(convertToISODateTime(timeUnix))
                .id(id)
                .sportKey("torneio-key")
                .torneio("Torneio")
                .name("Home vs Away")
                .build();
    }

     private String convertToISODateTime(long unixTimestamp) {
        Instant instant = Instant.ofEpochSecond(unixTimestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return formatter.format(instant);
    }

    private List<PartidaOdds> _newPartidaOdds() {

        Partida partida = _newPartida("any-id",0);

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(3.2)
                .build();

        Odd odd4 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome("Nautico")
                .odd(1.5)
                .build();

        Odd pinnacleOdd1 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(TEAM_HOME)
                .odd(1.4)
                .build();

        Odd pinnacleOdd2 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome("Nautico")
                .odd(2.1)
                .build();

        PartidaOdds partidaOdd = PartidaOdds.builder().partida(partida)
                .odds(List.of(odd1, odd4, pinnacleOdd1, pinnacleOdd2)).build();

        return List.of(partidaOdd);
    }

}
