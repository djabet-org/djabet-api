package hello.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hello.dto.Bookmaker;
import hello.dto.Market;
import hello.dto.Odd;
import hello.dto.Outcome;
import hello.dto.Partida;
import hello.dto.PartidaOdds;
import hello.dto.ValueBet;
import hello.repository.TheOddsAPI;

@Service
public class ValueBetService {

    @Autowired
    private TheOddsAPI api;

    private Map<String, String> sportsMarketsMap = Map.of(
        "soccer", "spreads,totals"
    );

    private Logger _log = Logger.getLogger(getClass().getName());

    public Map<Partida, List<ValueBet>> getValueBets(double bankroll) {
        return api.getUpcomingPartidas().stream()
            .map(partida -> api.getOdds(partida, "spreads,totals"))
            .filter(Objects::nonNull)
            .flatMap(partidaOdd -> _calculateEVs(bankroll, _getOdds(partidaOdd)).stream())
            .collect(Collectors.groupingBy(ValueBet::getPartida));
    }


    private List<ValueBet> _calculateEVs(double bankroll, List<Odd> odds) {
        Map<String, Map<String, List<Odd>>> baseProbabilityMap = odds.stream()
                .filter(odd -> odd.getBookmaker().equals("pinnacle"))
                .collect(Collectors.groupingBy(Odd::getMarket, Collectors.groupingBy(Odd::getName)));

        return odds.stream()
                .collect(
                        Collectors.groupingBy(Odd::getTorneio,
                                Collectors.groupingBy(Odd::getPartida,
                                        Collectors.groupingBy(Odd::getMarket))))
                .entrySet().stream()
                .flatMap(torneiosMap -> torneiosMap.getValue().entrySet().stream())
                .flatMap(partidasMap -> partidasMap.getValue().entrySet().stream())
                .flatMap(marketsMap -> marketsMap.getValue().stream())
                .map(marketOdd -> _toEV(bankroll, marketOdd, baseProbabilityMap))
                .filter(Objects::nonNull)
                .filter(valueBet -> !valueBet.getBookmaker().equals("pinnacle"))
                .filter(valueBet -> valueBet.getEv() * 100 > 1)
                // .filter( valueBet -> !Helper.didStarted(valueBet.getPartida().getHorario()))
                .filter(valueBet -> Helper.happensInTwoDays(valueBet.getPartida().getHorario()))
                .filter(valuebet -> !Helper.getExcludedBookmakers().stream()
                        .anyMatch(bookmaker -> bookmaker.equals(valuebet.getBookmaker())))
                .collect(Collectors.toList());
    }

    private ValueBet _toEV(double bankroll, Odd marketOdd, Map<String, Map<String, List<Odd>>> baseProbabilityMap) {
        if (!baseProbabilityMap.containsKey(marketOdd.getMarket())
                || !baseProbabilityMap.get(marketOdd.getMarket()).containsKey(marketOdd.getName())) {
            return null;
        }

        double pinnacleOdd = baseProbabilityMap.get(marketOdd.getMarket()).get(marketOdd.getName()).get(0).getOdd();
        double baseProbability = 100.0 / (pinnacleOdd);
        double ev = (baseProbability / (100 / marketOdd.getOdd())) - 1;

        return ValueBet.builder()
                .ev(ev)
                .market(marketOdd.getMarket())
                .partida(marketOdd.getPartida())
                .torneio(marketOdd.getTorneio())
                .bookmaker(marketOdd.getBookmaker())
                .odd(marketOdd.getOdd())
                .forWhat(marketOdd.getName())
                .sharpOdd(baseProbability)
                .betAmmount(Helper.calculateBetAmount(baseProbability, bankroll, marketOdd.getOdd()))
                .build();
    }

    private List<Odd> _getOdds(PartidaOdds partida) {
        List<Odd> odds = new ArrayList<>();
        for (Bookmaker bookmaker : partida.getBookmakers()) {
            for (Market market : bookmaker.getMarkets()) {
                if (market.getOutcomes().size() >= 3)
                    continue;
                for (Outcome outcome : market.getOutcomes()) {
                    odds.add(_toOdd(bookmaker.getKey(), market.getKey(), outcome.getName(), outcome.getOdd(), partida));
                }
            }

        }

        return odds;
    }

     private Odd _toOdd(String bookmaker, String market, String name, double odd, PartidaOdds partida) {
        return Odd.builder()
                .bookmaker(bookmaker)
                .market(market)
                .name(name)
                .odd(odd)
                .partida(partida.getPartida())
                .torneio(partida.getPartida().getTorneio())
                .build();

    }
    
}
