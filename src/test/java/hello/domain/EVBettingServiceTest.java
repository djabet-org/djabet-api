package hello.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hello.domain.services.EVService;
import hello.domain.services.OddsService;
import hello.domain.services.impl.ArbsBettingServiceImpl;
import hello.domain.services.impl.EVBettingServiceImpl;
import hello.domain.services.impl.OddsServiceImpl;
import hello.infrastructure.TheOddsAPI;
import hello.model.EVFilter;

@ExtendWith(MockitoExtension.class)
public class EVBettingServiceTest {

    private static final String TEAM_HOME = "Sport";
    private static final String TEAM_AWAY = "Nautico";

    @Mock
    private TheOddsAPI theOddsAPI;

    @InjectMocks
    private EVService bettingService = new EVBettingServiceImpl();

    @InjectMocks
    private OddsService oddsService = new OddsServiceImpl();

    private Partida _prematchPartida;

    @BeforeEach
    public void setup() {
        _prematchPartida = _newPartida("prematch-id", Instant.now().plus(Duration.ofHours(2)).toEpochMilli()/1000);
    }

    @Test
    public void testGetValueBetsIfPositive() {
    Outcome outcome = Outcome.builder().name(TEAM_HOME).build();

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(outcome)
                .odd(3.0)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(outcome)
                .odd(3.2)
                .build();

        PartidaOdds partidaOdd = PartidaOdds.builder().partida(_prematchPartida)
                .odds(List.of(odd1, odd2)).build();

        List<PartidaEVs> result = bettingService.calculateEVs(List.of(partidaOdd), EVFilter.builder().live(false).build());

        assertEquals(0, result.size());
    }

    @Test
    public void itShouldGetValueBetsFilteredByMarkets() {
    Outcome outcome = Outcome.builder().name(TEAM_HOME).build();

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(outcome)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2o")
                .outcome(outcome)
                .odd(3.4)
                .build();

        Odd odd4 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(outcome)
                .odd(3.2)
                .build();

        Odd odd5 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2o")
                .outcome(outcome)
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
    Outcome outcome = Outcome.builder().name(TEAM_HOME).build();

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(outcome)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome(outcome)
                .odd(3.3)
                .build();

        Odd pinnacleOdd = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(outcome)
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
    Outcome outcome = Outcome.builder().name(TEAM_HOME).build();

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(outcome)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome(outcome)
                .odd(3.3)
                .build();

        Odd odd3 = Odd.builder()
                .bookmaker("Bookmaker C")
                .market("h2h")
                .outcome(outcome)
                .odd(3.8)
                .build();

        Odd pinnacleOdd = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(outcome)
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
    public void itShouldGetPrematchValueBets() throws Throwable {
    Outcome outcome = Outcome.builder().name(TEAM_HOME).build();

        Partida livePartida = _newPartida("live-id",Instant.now().minus(Duration.ofMinutes(20)).toEpochMilli()/1000);

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(outcome)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome(outcome)
                .odd(3.3)
                .build();

        Odd pinnacleOdd = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(outcome)
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

        when(theOddsAPI.getSportOdds(any())).thenReturn(List.of(prelivePartidaOdd, livePartidaOdd));

        EVFilter evFilter = EVFilter.builder().prematch(true).live(false).build();

        List<PartidaOdds> prematchResult = oddsService.getOdds(evFilter);

    assertEquals(2, prematchResult.size());

    assertEquals("prematch-id", prematchResult.get(0).getPartida().getId());
    }

    @Test
    public void itShouldGetLiveValueBets() throws Throwable {
    Outcome outcome = Outcome.builder().name(TEAM_HOME).build();
    Outcome outcomeAway = Outcome.builder().name(TEAM_AWAY).build();

        Partida livePartida = _newPartida("live-id",Instant.now().minus(Duration.ofMinutes(20)).toEpochMilli()/1000);

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(outcome)
                .odd(3.4)
                .build();

        Odd odd2 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome(outcome)
                .odd(3.3)
                .build();

        Odd pinnacleOdd = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(outcomeAway)
                .odd(3.2)
                .build();

        PartidaOdds livePartidaOdd = PartidaOdds.builder()
                .partida(livePartida)
                .odds(List.of(odd2, pinnacleOdd))
                .build();

        when(theOddsAPI.getUpcomingOdds(any())).thenReturn(List.of(livePartidaOdd));

        EVFilter evFilter = EVFilter.builder().live(true).build();

        List<PartidaOdds> liveResult = oddsService.getOdds(evFilter);

    assertEquals(1, liveResult.size());

    assertEquals("live-id", liveResult.get(0).getPartida().getId());

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
}
