package hello.unit;

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
import hello.repository.TheOddsAPI;
import hello.service.ValueBetService;

@ExtendWith(MockitoExtension.class)
public class ValueBetServiceTest {

    @Mock
    private TheOddsAPI theOddsAPI;

    @InjectMocks
    private ValueBetService valueBetService;

    // @Test
    public void testGetValueBets() {
        Partida partida =  _newPartida(LocalDateTime.now().toString());
        Partida partida2 =  _newPartida(LocalDateTime.now().plusHours(2).toString());
        List<Partida> partidas = List.of(partida);

        // PartidaOdds partidaOdds1 = PartidaOdds.builder().

        when(theOddsAPI.getOdds(eq(partida), anyString())).thenReturn(any(PartidaOdds.class));

        double bankroll = 100;
        Map<Partida, List<hello.dto.ValueBet>> result = valueBetService.getValueBets(bankroll);

        verify(theOddsAPI).getUpcomingPartidas();
        // verify(theOddsAPI).getPartidaOdds();
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
