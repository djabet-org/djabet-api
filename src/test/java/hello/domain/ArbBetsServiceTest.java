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
public class ArbBetsServiceTest {

    private static final String TEAM_HOME = "Sport";
    private static final String TEAM_AWAY = "Nautico";

    @Mock
    private TheOddsAPI theOddsAPI;

    @InjectMocks
    private BettingService bettingService = new BettingServiceImpl();

    @InjectMocks
    private ArbBetsService arbBetsService = new ArbBetsService();

    private Partida _prematchPartida;

    @BeforeEach
    public void setup() {
        _prematchPartida = _newPartida("prematch-id", Instant.now().plus(Duration.ofHours(2)).toEpochMilli()/1000);
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

        List<PartidaArbs> result = arbBetsService.getArbs(List.of(partidaOdd), EVFilter.builder().build());
        System.out.println("creu "+result);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getArbs().size());
        assertEquals(-0.089, result.get(0).getArbs().get(0).getArb(), 0.001);
        assertEquals(-0.040, result.get(0).getArbs().get(1).getArb(), 0.001);

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
