package hello.unit;

import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hello.dto.Partida;
import hello.repository.TheOddsAPI;
import hello.service.ValueBet;
import hello.service.ValueBetService;

@ExtendWith(MockitoExtension.class)
public class ValueBetServiceTest {
    
    @Mock
    private TheOddsAPI theOddsAPI;

        @InjectMocks
        private ValueBetService valueBetService;

    @Test
    public void testGetValueBets() {
        Partida partida =  _newPartida(LocalDateTime.now().toString());
        List<Partida> partidas = List.of(partida);

        // List<ValueBet> result = valueBetService.getValueBets(partidas);

        verify(theOddsAPI).getUpcomingPartidas();
        // verify(theOddsAPI).getPartidaOdds();
    }

    private Partida _newPartida(String horario) {
        return Partida.builder()
                .awayTeam("Away Team")
                .homeTeam("Home Team")
                .horario(horario)
                .id("any-id")
                .sportKey("torneio-key")
                .torneio("Torneio")
                .name("Home vs Away")
                .build();
}

}
