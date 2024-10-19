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

    private List<PartidaOdds> _partidaOdds;

    private List<PartidaEVs> _result;

    private Partida _partida;

    @BeforeEach
    public void setup() {
        _partidaOdds = _newPartidaOdds();
        _result = bettingService.calculateEVs(_partidaOdds);
        _partida = _newPartida(null);
    }

    @Test
    public void itShouldGetOdds() throws JsonMappingException, JsonProcessingException {
        List<PartidaOdds> expecteOdds = _newPartidaOdds();
        when(theOddsAPI.getUpcomingOdds()).thenReturn(expecteOdds);
        List<PartidaOdds> odds = bettingService.getOdds();

        verify(theOddsAPI).getUpcomingOdds();
        assertEquals(expecteOdds, odds);
    }

    @Test
    public void testGetValueBets() {
        assertEquals(1, _result.size());
        assertTrue(_result.get(0).getEvs().size() > 0);
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

        List<PartidaEVs> result = bettingService.calculateEVs(List.of(partidaOdd));

        System.out.println(result);

        assertEquals(0, result.size());
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
