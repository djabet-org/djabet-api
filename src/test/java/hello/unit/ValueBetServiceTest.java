package hello.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hello.dto.Bookmaker;
import hello.dto.Market;
import hello.dto.Outcome;
import hello.dto.Partida;
import hello.dto.PartidaOdds;
import hello.dto.ValueBet;
import hello.infra.TheOddsAPI;
import hello.model.EVFilter;
import hello.repository.BetRepository;
import hello.service.ValueBetService;

@ExtendWith(MockitoExtension.class)
public class ValueBetServiceTest {

    @Mock
    private TheOddsAPI theOddsAPI;

    @Mock
    private BetRepository _betRepository;

    @InjectMocks
    private ValueBetService valueBetService;

    @Test
    public void testGetValueBets() {
        // Partida partida =  _newPartida(LocalDateTime.now().toString());
        // PartidaOdds odd = mock(PartidaOdds.class);

        // when(theOddsAPI.getOdds(eq(partida), anyString())).thenReturn(odd);

        double bankroll = 100;

        EVFilter filter = EVFilter.builder().build();

        List<ValueBet> result = valueBetService.getValueBets(bankroll, filter);

        verify(theOddsAPI).getUpcomingPartidas();
        // verify(theOddsAPI).getOdds(eq(partida), anyString());
        // assertEquals(result.size(),2);
    }

    private Partida _newPartida(String horario) {
        return Partida.builder()
                .awayTeam("Away Team" + horario)
                .homeTeam("Home Team" + horario)
                .horario(horario)
                .id("any-id")
                .sportKey("torneio-key")
                .torneio("Torneio")
                .name("Home vs Away" + horario)
                .build();
    }

    // private PartidaOdds _newPartidaOdds() { List<Bookmaker> bookmakers = List.of(
    //         Bookmaker.builder()
    //         .key("bookmakerA")
    //         .name("Bookmaker A")
    //         .markets(List.of(
    //             Market.builder().key("market1")
    //                 .outcomes(List.of(
    //                     Outcome.builder().name("A").market("h2h").odd(2);
    //                 ))
    //         ));

    //     )
    //     return PartidaOdds.builder().bookmakers(null)
    // }

}
