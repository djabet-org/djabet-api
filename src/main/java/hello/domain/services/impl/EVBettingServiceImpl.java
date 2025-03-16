package hello.domain.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.util.StringUtil;
import org.paukov.combinatorics.CombinatoricsFactory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hello.domain.Odd;
import hello.domain.PartidaEVs;
import hello.domain.PartidaOdds;
import hello.domain.ValueBet;
import hello.domain.services.EVService;
import hello.infrastructure.TheOddsAPI;
import hello.model.EVFilter;
import hello.service.Helper;

@Service
public class EVBettingServiceImpl implements EVService {

        @Autowired
        private TheOddsAPI theOddsAPI;

        @Override
        public List<PartidaEVs> calculateEVs(List<PartidaOdds> partidaOdds, EVFilter evFilter) {
                return partidaOdds.stream()
                                .map(partidaOdd -> _getEVs(100.0, partidaOdd, evFilter))
                                .filter(partidaEVs -> partidaEVs.getEvs().size() > 0)
                                .collect(Collectors.toList());
        }

        private PartidaEVs _getEVs(double bankroll, PartidaOdds partidaOdds, EVFilter evFilter) {
                Map<String, Map<String, List<Odd>>> winProbabilityBasedOnPinnacle = partidaOdds.getOdds().stream()
                                .filter(odd -> odd.getBookmaker().equals("pinnacle"))
                                .collect(Collectors.groupingBy(Odd::getMarket, Collectors.groupingBy(Odd::getOutcome)));

                List<ValueBet> evs = partidaOdds.getOdds().stream()
                                .collect(Collectors.groupingBy(Odd::getMarket))
                                .entrySet().stream()
                                .flatMap(marketsMap -> marketsMap.getValue().stream())
                                .map(marketOdd -> _toEV(bankroll, partidaOdds, marketOdd,
                                                winProbabilityBasedOnPinnacle))
                                .filter(ev -> ev.getEv() > evFilter.getMinEv() && ev.getEv() < evFilter.getMaxEv())
                                .filter(ev -> ev.getOdd() > evFilter.getMinOdd() && ev.getOdd() < evFilter.getMaxOdd())
                                .filter(ev -> StringUtil.isBlank(evFilter.getMarkets()) ? true
                                                : evFilter.getMarkets().contains(ev.getMarket()))
                                .filter(ev -> !Helper.getExcludedBookmakers().stream()
                                                .anyMatch(bookmaker -> bookmaker.equals(ev.getBookmaker())))
                                .collect(Collectors.toList());

                return PartidaEVs.builder().partida(partidaOdds.getPartida()).evs(evs).build();
        }

        private ValueBet _toEV(double bankroll, PartidaOdds partidaOdds, Odd marketOdd,
                        Map<String, Map<String, List<Odd>>> baseProbabilityMap) {
                if (!baseProbabilityMap.containsKey(marketOdd.getMarket())
                                || !baseProbabilityMap.get(marketOdd.getMarket()).containsKey(marketOdd.getOutcome())) {
                        return ValueBet.builder().build();
                }

                double pinnacleOdd = baseProbabilityMap.get(marketOdd.getMarket()).get(marketOdd.getOutcome()).get(0)
                                .getOdd();

                double stake = 100;
                double pinnacleImpliedProb = 1 / pinnacleOdd;
                double yourImpliedProb = 1 / marketOdd.getOdd();
                double potentialProfit = stake * (marketOdd.getOdd() - 1);
                double probabilityOfLosing = 1 - pinnacleImpliedProb;

                if (pinnacleImpliedProb <= yourImpliedProb) {
                        return ValueBet.builder().build();
                }

                double ev = (pinnacleImpliedProb * potentialProfit) - (probabilityOfLosing * stake);

                return ValueBet.builder()
                                .evPercentage(String.format("%.2f%%", ev * 100))
                                .ev(ev)
                                .market(marketOdd.getMarket())
                                .bookmaker(marketOdd.getBookmaker())
                                .odd(marketOdd.getOdd())
                                .outcome(marketOdd.getOutcome())
                                .sharpOdd(pinnacleOdd)
                                .build();
        }
}
