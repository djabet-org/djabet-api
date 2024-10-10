// package hello.unit;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.Mockito.*;
// import org.mockito.Mockito.*;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Map;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import hello.domain.BettingService;
// import hello.domain.PartidaOdds;
// import hello.dto.Bookmaker;
// import hello.dto.Market;
// import hello.domain.Odd;
// import hello.dto.Outcome;
// import hello.domain.Partida;
// import hello.domain.ValueBet;
// import hello.infrastructure.TheOddsAPI;
// import hello.model.EVFilter;
// import hello.repository.BetRepository;
// import hello.service.ValueBetService;

// public class ValueBetServiceTest {

//     @Mock
//     private TheOddsAPI theOddsAPI;

//     @Mock
//     private BetRepository _betRepository;

//     @InjectMocks
//     private ValueBetService valueBetService;

//     @InjectMocks
//     private BettingService bettingService;

//     @Test
//     public void testGetValueBets() {
//         when(theOddsAPI.getOdds(anyString(), anyString(), anyString())).thenReturn(_newPartidasOdds());

//         double bankroll = 100;

//         EVFilter filter = EVFilter.builder().build();

//         List<ValueBet> result = bettingService.calculateEVs(_newPartidasOdds());

//         verify(theOddsAPI).getUpcomingPartidas();
//         // verify(theOddsAPI).getOdds(eq(partida), anyString());
//         assertEquals(result.size(),2);
//     }

//     private Partida _newPartida(String horario) {
//         return Partida.builder()
//                 .awayTeam("Away Team" + horario)
//                 .homeTeam("Home Team" + horario)
//                 .horario(horario)
//                 .id("any-id")
//                 .sportKey("torneio-key")
//                 .torneio("Torneio")
//                 .name("Home vs Away" + horario)
//                 .build();
//     }

//     private List<PartidaOdds> _newPartidasOdds() { 

//         Odd odd1 = Odd.builder()
//             .bookmaker("Bookmaker A")
//             .market("h2h")
//             .odd(1.9)
//             .build();

//         Odd odd2 = Odd.builder()
//             .bookmaker("Bookmaker B")
//             .market("h2h")
//             .odd(2.1)
//             .build();

//             PartidaOdds partidaOdds = PartidaOdds.builder().partida(_newPartida(null)).odds(List.of(odd1, odd2)).build();

//             return List.of(partidaOdds);
//         // Outcome outcome1 = Outcome.builder().name("Sport").market("h2h").odd(2.1).build();
//         // Outcome outcome2 = Outcome.builder().name("Santa").market("h2h").odd(1.9).build();
//         // Market marketh2h = Market.builder().key("h2h").outcomes(List.of(outcome1, outcome2)).build();
//         // Bookmaker bookmakerA = Bookmaker.builder().key("bookmaker-a").name("Bookmaker A").markets(List.of(marketh2h)).build();
//         // Bookmaker bookmakerB = Bookmaker.builder().key("bookmaker-b").name("Bookmaker B").markets(List.of(marketh2h)).build();
//         // List<Bookmaker> bookmakers = List.of(
//         //     Bookmaker.builder()
//         //     .key("bookmakerA")
//         //     .name("Bookmaker A")
//         //     .markets(List.of(
//         //         Market.builder().key("market1")
//         //             .outcomes(List.of(
//         //                 Outcome.builder().name("A").market("h2h").odd(2);
//         //             ))
//         //     ));

//         // )
//         // return PartidaOdds.builder().bookmakers(null)
//     }

// }
