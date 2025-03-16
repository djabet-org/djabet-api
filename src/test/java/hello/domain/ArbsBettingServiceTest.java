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

import hello.domain.services.ArbService;
import hello.domain.services.OddsService;
import hello.domain.services.impl.ArbsBettingServiceImpl;
import hello.domain.services.impl.OddsServiceImpl;
import hello.infrastructure.TheOddsAPI;
import hello.model.EVFilter;

@ExtendWith(MockitoExtension.class)
public class ArbsBettingServiceTest {

        private static final String TEAM_HOME = "Sport";
        private static final String TEAM_AWAY = "Nautico";

        @Mock
        private TheOddsAPI theOddsAPI;

        @InjectMocks
        private ArbService bettingService = new ArbsBettingServiceImpl();

        @InjectMocks
        private OddsService oddsService = new OddsServiceImpl();

        private Partida _prematchPartida;

        @BeforeEach
        public void setup() {
                _prematchPartida = _newPartida("prematch-id",
                                Instant.now().plus(Duration.ofHours(2)).toEpochMilli() / 1000);
        }

        @Test
        public void itShouldGetArbBetsFilteredByMarkets() {
                Outcome outcomeHome = Outcome.builder().name(TEAM_HOME).build();
                Outcome outcomeAway = Outcome.builder().name(TEAM_AWAY).build();

                Odd odd1 = Odd.builder()
                                .bookmaker("Bookmaker A")
                                .market("h2h")
                                .outcome(outcomeHome)
                                .odd(3.4)
                                .build();

                Odd odd2 = Odd.builder()
                                .bookmaker("Bookmaker B")
                                .market("h2o")
                                .outcome(outcomeAway)
                                .odd(3.4)
                                .build();

                Odd odd4 = Odd.builder()
                                .bookmaker("pinnacle")
                                .market("h2h")
                                .outcome(outcomeAway)
                                .odd(3.2)
                                .build();

                Odd odd5 = Odd.builder()
                                .bookmaker("pinnacle")
                                .market("h2o")
                                .outcome(outcomeHome)
                                .odd(3.2)
                                .build();

                PartidaOdds partidaOdd = PartidaOdds.builder().partida(_prematchPartida)
                                .odds(List.of(odd1, odd2, odd4, odd5)).build();

                EVFilter evFilter = EVFilter.builder().markets("h2h").build();
                List<ArbBet> arbs = bettingService.getArbs(List.of(partidaOdd), evFilter);
                ArbBet arbBet = arbs.get(0);

                assertEquals(1, arbs.size());
                assertEquals(1, arbBet.getPartialArbs().size());
        }

        @Test
        public void itShouldGetSurebets() {
                Outcome outcomeAway = Outcome.builder().name(TEAM_AWAY).build();
                Outcome outcomeHome = Outcome.builder().name(TEAM_HOME).build();

                Odd odd1 = Odd.builder()
                                .bookmaker("Bet365")
                                .market("h2h")
                                .outcome(outcomeHome)
                                .odd(1.22)
                                .build();

                Odd odd2 = Odd.builder()
                                .bookmaker("Bet365")
                                .market("h2h")
                                .outcome(outcomeAway)
                                .odd(3.7)
                                .build();

                Odd odd3 = Odd.builder()
                                .bookmaker("Betano")
                                .market("h2h")
                                .outcome(outcomeHome)
                                .odd(1.44)
                                .build();

                Odd odd4 = Odd.builder()
                                .bookmaker("Betano")
                                .market("h2h")
                                .outcome(outcomeAway)
                                .odd(2.89)
                                .build();

                PartidaOdds partidaOdd = PartidaOdds.builder().partida(_prematchPartida)
                                .odds(List.of(odd1, odd2, odd3, odd4)).build();

                List<ArbBet> arbs = bettingService.getArbs(List.of(partidaOdd), EVFilter.builder().build());
                ArbBet arbBet = arbs.get(0);

                assertEquals(1, arbs.size());
                assertEquals(1, arbBet.getPartialArbs().size());
                assertEquals("3.53%", arbBet.getPartialArbs().get(0).getRoi());

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
