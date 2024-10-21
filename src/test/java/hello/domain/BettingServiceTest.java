package hello.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import hello.dto.Bookmaker;
import hello.dto.Market;
import hello.domain.Odd;
import hello.dto.Outcome;
import hello.domain.Partida;
import hello.domain.PartidaOdds;
import hello.domain.ValueBet;
import hello.infrastructure.TheOddsAPI;
import hello.model.EVFilter;
import hello.repository.BetRepository;
import hello.service.ValueBetService;

@ExtendWith(MockitoExtension.class)
public class BettingServiceTest {

    private static final String TEAM_HOME = "Sport";

    @Mock
    private TheOddsAPI theOddsAPI;

    @InjectMocks
    private BettingService bettingService = new BettingServiceImpl();

    private Partida _partida;

    @BeforeEach
    public void setup() {
        _partida = _newPartida(null);
    }

    @Test
    public void itShouldGetOdds() throws JsonMappingException, JsonProcessingException {
        List<PartidaOdds> expecteOdds = _newPartidaOdds();
        EVFilter evFilter = EVFilter.builder().build();
        when(theOddsAPI.getUpcomingOdds(any(EVFilter.class))).thenReturn(expecteOdds);
        List<PartidaOdds> odds = bettingService.getOdds(evFilter);

        verify(theOddsAPI).getUpcomingOdds(evFilter);
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

        PartidaOdds partidaOdd = PartidaOdds.builder().partida(_partida)
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

        PartidaOdds partidaOdd = PartidaOdds.builder().partida(_partida)
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
                .partida(_partida)
                .odds(List.of(odd1, odd2, pinnacleOdd))
                .build();

        EVFilter evFilter = EVFilter.builder().minEv(0.04).build();

        List<PartidaEVs> result = bettingService.calculateEVs(List.of(partidaOdd), evFilter);

        List<ValueBet> evs = result.get(0).getEvs();

        assertEquals(1, evs.size());
        assertTrue(evs.get(0).getEv() >= 0.04);
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
                .partida(_partida)
                .odds(List.of(odd1, odd2, odd3, pinnacleOdd))
                .build();

        EVFilter evFilter = EVFilter.builder().minOdd(3.32).maxOdd(3.7).build();

        List<PartidaEVs> result = bettingService.calculateEVs(List.of(partidaOdd), evFilter);

        List<ValueBet> evs = result.get(0).getEvs();

        assertEquals(1, evs.size());
        assertTrue(evs.get(0).getEv() >= 0.04);
    }
    private Partida _newPartida(String horario) {
        return Partida.builder()
                .awayTeam("Away Team")
                .homeTeam(TEAM_HOME)
                .horario(horario)
                .id("any-id")
                .sportKey("torneio-key")
                .torneio("Torneio")
                .name("Home vs Away")
                .build();
    }

    private List<PartidaOdds> _newPartidaOdds() {

        Partida partida = _newPartida(null);

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
