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

import hello.domain.services.OddsService;
import hello.domain.services.impl.OddsServiceImpl;
import hello.infrastructure.TheOddsAPI;
import hello.model.EVFilter;

@ExtendWith(MockitoExtension.class)
public class OddsServiceTest {

    private static final String TEAM_HOME = "Sport";
    private static final String TEAM_AWAY = "Nautico";

    @Mock
    private TheOddsAPI theOddsAPI;

    @InjectMocks
    private OddsService oddsService = new OddsServiceImpl();

    @BeforeEach
    public void setup() {
    }

    @Test
    public void itShouldGetOdds() throws Throwable {
        List<PartidaOdds> expecteOdds = _newPartidaOdds();
        EVFilter evFilter = EVFilter.builder().live(true).build();
        when(theOddsAPI.getUpcomingOdds(evFilter)).thenReturn(expecteOdds);
        List<PartidaOdds> odds = oddsService.getOdds(evFilter);

        verify(theOddsAPI).getUpcomingOdds(evFilter);
        assertEquals(expecteOdds, odds);
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

    Outcome outcomeHome= Outcome.builder().name(TEAM_HOME).build();
        Partida partida = _newPartida("any-id",0);

        Odd odd1 = Odd.builder()
                .bookmaker("Bookmaker A")
                .market("h2h")
                .outcome(outcomeHome)
                .odd(3.2)
                .build();

        Odd odd4 = Odd.builder()
                .bookmaker("Bookmaker B")
                .market("h2h")
                .outcome(outcomeHome)
                .odd(1.5)
                .build();

        Odd pinnacleOdd1 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(outcomeHome)
                .odd(1.4)
                .build();

        Odd pinnacleOdd2 = Odd.builder()
                .bookmaker("pinnacle")
                .market("h2h")
                .outcome(outcomeHome)
                .odd(2.1)
                .build();

        PartidaOdds partidaOdd = PartidaOdds.builder().partida(partida)
                .odds(List.of(odd1, odd4, pinnacleOdd1, pinnacleOdd2)).build();

        return List.of(partidaOdd);
    }

}
