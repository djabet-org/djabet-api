package hello.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import hello.dto.Bookmaker;
import hello.dto.Market;
import hello.dto.Odd;
import hello.dto.Outcome;
import hello.dto.Partida;
import hello.dto.PartidaOdds;
import hello.dto.SureBet;
import hello.dto.Torneio;
import hello.infra.TheOddsAPI;

public class SureBetsService {

    public void goUpcoming(double bankroll, List<PartidaOdds> upcomingOdds) throws IOException {
        upcomingOdds.stream()
                // .filter(
                // torneio -> torneio.getGroup().equalsIgnoreCase(Constants.CRICKET_GROUP) ||
                // torneio.getGroup().equalsIgnoreCase(Constants.TENNIS_GROUP) ||
                // torneio.getGroup().equalsIgnoreCase(Constants.BASKETBALL_GROUP) ||
                // torneio.getGroup().equalsIgnoreCase(Constants.BASEBALL_GROUP))
                // .limit(1)
                // .filter(torneio -> !torneio.getTitle().equalsIgnoreCase("test matches"))
                // .limit(1)
                // .peek(torneio -> System.out.println(torneio.getTitle()+"\n"))
                // .flatMap(torneio -> oddsAPI.getPartidas(torneio.getKey()).stream())
                // .map(partida -> oddsAPI.getOdds(partida, "h2h,spreads"))
                // .peek(System.out::println)
                .map(partidaOdd -> _getOdds(partidaOdd))
                // .peek( System.out::println)
                .flatMap(odds -> getArbs(odds, bankroll).stream())
                .filter( arb -> arb.getArbs() > 0)
                // .peek( System.out::println)
                .sorted(Comparator.comparing(SureBet::getArbs).reversed())
                .forEach(System.out::println);
        // .limit(15)
    }

    public void go() throws IOException {
        TheOddsAPI oddsAPI = new TheOddsAPI();
        // String url = "http://localhost:8081/response.json";

        String markets = "h2h,spreads,totals";
        oddsAPI.getSoccerTorneios().stream()
                // .filter(
                // torneio -> torneio.getGroup().equalsIgnoreCase(Constants.CRICKET_GROUP) ||
                // torneio.getGroup().equalsIgnoreCase(Constants.TENNIS_GROUP) ||
                // torneio.getGroup().equalsIgnoreCase(Constants.BASKETBALL_GROUP) ||
                // torneio.getGroup().equalsIgnoreCase(Constants.BASEBALL_GROUP))
                // .limit(1)
                // filter(torneio -> !torneio.getTitle().equalsIgnoreCase("test matches"))
                .limit(1)
                // .peek(torneio -> System.out.println(torneio.getTitle()+"\n"))
                .flatMap(torneio -> oddsAPI.getPartidas(torneio.getKey()).stream())
                .limit(5)
                .map(partida -> oddsAPI.getOdds(partida, markets))
                // .peek(System.out::println)
                .map(this::_getOdds)
                // .peek( System.out::println)
                .flatMap(odds -> getArbs(odds, 100).stream())
                .filter( arb -> arb.getArbs() > 0)
                // .peek( System.out::println)
                .sorted(Comparator.comparing(SureBet::getArbs).reversed())
                .forEach(System.out::println);
        // .limit(15)
    }
    public List<SureBet> getArbs(List<Odd> odds, double bankroll) {
        Map<Partida, Map<String, Map<String, List<Odd>>>> oddsMap = odds.stream()
                .collect(
                        Collectors.groupingBy(Odd::getPartida,
                                Collectors.groupingBy(Odd::getMarket, Collectors.groupingBy(Odd::getName))));

        return oddsMap.entrySet().stream()
                .flatMap(partidaMap -> partidaMap.getValue().entrySet().stream())
                .filter(marketMap -> marketMap.getValue().size() <= 2)
                .map(marketMap -> marketMap.getValue())
                .flatMap(nameMap -> {
                    // System.out.println("creu " + nameMap.keySet());
                    List<String> keys = new ArrayList(nameMap.keySet());
                    List<SureBet> sureBets = new ArrayList<>();
                    List<Odd> odds1 = nameMap.get(keys.get(0));
                    List<Odd> odds2 = nameMap.get(keys.get(1));
                    for (Odd odd1 : odds1) {
                        for (Odd odd2 : odds2) {
                            if (odd1.getBookmaker().equalsIgnoreCase(odd2.getBookmaker()))
                                continue;
                            sureBets.add(_toSurebetH2H(bankroll*0.05,
                                    odd1.getPartida(), odd1.getBookmaker(), odd2.getBookmaker(), odd1.getOdd(),
                                    odd2.getOdd(),
                                    odd1.getMarket()));
                        }
                    }

                    return sureBets.stream();
                })
                .filter(surebet -> surebet.getArbs() > 0)
                .filter(surebet -> !Helper.getExcludedBookmakers().stream()
                        .anyMatch(bookmaker -> bookmaker.equals(surebet.getHomeBookmaker()) ||
                                bookmaker.equals(surebet.getAwayBookmaker())))
                // .filter(surebet -> !Helper.didStarted(surebet.getHorario()))
                .filter(surebet -> Helper.happensInTwoDays(surebet.getHorario()))
                .collect(Collectors.toList());
    }

    private List<Odd> _getOdds(PartidaOdds partida) {
        List<Odd> odds = new ArrayList<>();
        for (Bookmaker bookmaker : partida.getBookmakers()) {
            for (Market market : bookmaker.getMarkets()) {
                for (Outcome outcome : market.getOutcomes()) {
                    odds.add(_toOdd(bookmaker.getKey(), market.getKey(), outcome.getName(), outcome.getOdd(), partida));
                }
            }

        }

        return odds;
    }

    private Odd _toOdd(String bookmaker, String market, String name, double odd, PartidaOdds partida) {
        return Odd.builder().bookmaker(bookmaker).market(market).name(name).odd(odd).partida(partida.getPartida())
                .build();

    }

    private SureBet _toSurebetH2H(double defaultBetAmmount, Partida partida,
            String homeBookmaker,
            String awayBookmaker,
            double homeOdd,
            double awayOdd,
            String market) {

        double implicitProbabilityOdd1 = 100 / homeOdd;
        double implicitProbabilityOdd2 = 100 / awayOdd;

        double betAmmount1 = defaultBetAmmount * implicitProbabilityOdd1 / (implicitProbabilityOdd1 + implicitProbabilityOdd2);
        double betAmmount2 = defaultBetAmmount * implicitProbabilityOdd2 / (implicitProbabilityOdd1 + implicitProbabilityOdd2);

        return SureBet.builder()
                .partidaId(partida.getId())
                .partida(partida.getName())
                .torneio(partida.getTorneio())
                .awayTeam(partida.getAwayTeam())
                .homeTeam(partida.getHomeTeam())
                .horario(partida.getHorario())
                .awayBookmaker(awayBookmaker)
                .homeBookmaker(homeBookmaker)
                .awayOdd(awayOdd)
                .homeOdd(homeOdd)
                .market(market)
                .arbs((1 - ((1 / homeOdd) + (1 / awayOdd))) * 100)
                .betAmmount1(betAmmount1)
                .betAmmount2(betAmmount2)
                .build();
    }

}