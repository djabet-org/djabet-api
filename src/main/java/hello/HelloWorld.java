// package hello;

// import java.io.IOException;
// import java.util.List;
// import java.util.Objects;

// import hello.dto.Partida;
// import hello.dto.PartidaOdds;
// import hello.dto.SureBet;
// import hello.dto.ValueBet;
// import hello.repository.TheOddsAPI;
// import hello.service.ValueBetService;

// public class HelloWorld {
//     private static final String SPORTMONKS_API_TOKEN = "TMypiINwbJmXL0rdyi4p5kiCkXf0v0CfUH5i6nhS1NGkWREs9CQuOqKAXmCV";

//     public static void main(String[] args) throws IOException {
//         // Document doc = Jsoup
//         // .connect("https://br.betano.com/sport/futebol/")
//         // .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"+
//         // "(KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36")
//         // .get();
//         // System.out.println(doc);
//         double bankroll = 100;
//         double evBankroll = 430;

//         List<PartidaOdds> upcomingOdds = new TheOddsAPI().getUpcomingOdds();
//         List<Partida> upcomingPartidas = new TheOddsAPI().getUpcomingPartidas();
//         System.out.println("\n=======================\n");
//         goUpcomingEV(evBankroll, upcomingOdds);
//         System.out.println("\n=======================\n");
//         goUpcomingArbs(bankroll, upcomingOdds);
//     }

//     public void goUpcomingEV(double bankroll, List<PartidaOdds> upcomingOdds) throws IOException {
//         ValueBetService valueBetsService = new ValueBetService();

//         upcomingOdds.stream()
//                 .map(partidaOdd -> _getOdds(partidaOdd))
//                 .filter(Objects::nonNull)
//                 .flatMap(odds -> valueBetsService.calculateEVs(bankroll, odds).entrySet().stream())
//                 .flatMap(torneioMap -> torneioMap.getValue().entrySet().stream())
//                 .flatMap(partidaMap -> partidaMap.getValue().entrySet().stream())
//                 .flatMap(marketMap -> marketMap.getValue().stream())
//                 .sorted(Comparator.comparing(ValueBet::getEv).reversed())
//                 .filter(ev -> ev.getEv() * 100 >= 2)
//                 // .limit(10)
//                 .forEach(
//                         valueBet -> System.out.println(String.format(
//                                 " +EV: %.2f%% | $ Bet: R$ %.2f | %s | %s | (%s) %s (%s @ %.2f) | Real Prob %%: %.2f | %s",
//                                 valueBet.getEv() * 100, valueBet.getBetAmmount(), valueBet.getTorneio(),
//                                 valueBet.getPartida().getName(), valueBet.getForWhat(),
//                                 valueBet.getMarket(), valueBet.getBookmaker(), valueBet.getOdd(),
//                                 valueBet.getSharpOdd(), valueBet.getPartida().getHorario())));
//     }

//         public void goUpcoming(double bankroll, List<PartidaOdds> upcomingOdds) throws IOException {
//         upcomingOdds.stream()
//                 // .filter(
//                 // torneio -> torneio.getGroup().equalsIgnoreCase(Constants.CRICKET_GROUP) ||
//                 // torneio.getGroup().equalsIgnoreCase(Constants.TENNIS_GROUP) ||
//                 // torneio.getGroup().equalsIgnoreCase(Constants.BASKETBALL_GROUP) ||
//                 // torneio.getGroup().equalsIgnoreCase(Constants.BASEBALL_GROUP))
//                 // .limit(1)
//                 // .filter(torneio -> !torneio.getTitle().equalsIgnoreCase("test matches"))
//                 // .limit(1)
//                 // .peek(torneio -> System.out.println(torneio.getTitle()+"\n"))
//                 // .flatMap(torneio -> oddsAPI.getPartidas(torneio.getKey()).stream())
//                 // .map(partida -> oddsAPI.getOdds(partida, "h2h,spreads"))
//                 // .peek(System.out::println)
//                 .map(partidaOdd -> _getOdds(partidaOdd))
//                 // .peek( System.out::println)
//                 .flatMap(odds -> getArbs(odds, bankroll).stream())
//                 .filter( arb -> arb.getArbs() > 0)
//                 // .peek( System.out::println)
//                 .sorted(Comparator.comparing(SureBet::getArbs).reversed())
//                 .forEach(System.out::println);
//         // .limit(15)
//     }
// }
