package hello.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.poi.util.StringUtil;
import org.paukov.combinatorics.CombinatoricsFactory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import hello.infrastructure.TheOddsAPI;
import hello.model.EVFilter;
import hello.service.Helper;

@Service
public class BettingServiceImpl implements BettingService {

        @Autowired
        private TheOddsAPI theOddsAPI;

        @Override
        public List<PartidaEVs> calculateEVs(List<PartidaOdds> partidaOdds, EVFilter evFilter) {
                return partidaOdds.stream()
                                .map(partidaOdd -> _getEVs(100.0, partidaOdd, evFilter))
                                .filter(partidaEVs -> partidaEVs.getEvs().size() > 0)
                                .collect(Collectors.toList());
        }

        @Override
        public List<PartidaArbs> getArbs(List<PartidaOdds> partidasOdds, EVFilter evFilter) {
                return partidasOdds.stream().map(partidaOdds -> _getArbs(partidaOdds, evFilter))
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
                                // .peek(System.out::println)
                                .filter(ev -> ev.getEv() > evFilter.getMinEv() && ev.getEv() < evFilter.getMaxEv())
                                .filter(ev -> ev.getOdd() > evFilter.getMinOdd() && ev.getOdd() < evFilter.getMaxOdd())
                                .filter(ev -> StringUtil.isBlank(evFilter.getMarkets()) ? true
                                                : evFilter.getMarkets().contains(ev.getMarket()))
                                // .filter(valueBet -> !valueBet.getBookmaker().equals("pinnacle"))
                                // .filter( valueBet -> !Helper.didStarted(valueBet.getPartida().getHorario()))
                                // .filter(valueBet ->
                                // Helper.happensInTwoDays(valueBet.getPartida().getHorario()))
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
                                // .betAmmount(Helper.calculateBetAmount(baseProbability, bankroll,
                                // marketOdd.getOdd()))
                                .build();
        }

        @Override
        public List<PartidaOdds> getOdds(EVFilter evFilter) throws JsonMappingException, JsonProcessingException {
                return theOddsAPI.getUpcomingOdds().stream()
                                .filter(partidaOdd -> Objects.isNull(evFilter.getLive()) ? true
                                                : partidaOdd.getPartida().isLive())
                                .filter(partidaOdd -> Objects.isNull(evFilter.getPrematch()) ? true
                                                : !partidaOdd.getPartida().isLive())
                                .collect(Collectors.toList());
        }

        private PartidaArbs _getArbs(PartidaOdds partidaOdds, EVFilter evFilter) {
                Map<String, List<Odd>> marketsOdds = partidaOdds.getOdds().stream()
                                .collect(Collectors.groupingBy(Odd::getMarket));

                List<ArbBet> allArbBets = new ArrayList<>();

                for (Entry<String, List<Odd>> marketEntry : marketsOdds.entrySet()) {
                        List<ArbBet> arbs = _calculateArbs(marketEntry);
                        allArbBets.addAll(arbs);
                }

                return PartidaArbs.builder().arbs(_filter(allArbBets, evFilter)).partida(partidaOdds.getPartida())
                                .build();

        }

        private List<ArbBet> _filter(List<ArbBet> allArbBets, EVFilter evFilter) {
                return allArbBets.stream()
                                .filter(arb -> arb.getProfit() > 0)
                                .filter(arb -> arb.getArb() > evFilter.getMinArb())
                                .filter(arb -> arb.getArb() < evFilter.getMaxArb())
                                .collect(Collectors.toList());
        }

        private List<ArbBet> _calculateArbs(Entry<String, List<Odd>> marketEntry) {
                List<ArbBet> arbs = new ArrayList<>();
                List<Odd> odds = marketEntry.getValue();
                Odd[] odds2 = odds.toArray(Odd[]::new);

                ICombinatoricsVector<Odd> vector = CombinatoricsFactory.createVector(odds2);
                Generator<Odd> gen = CombinatoricsFactory.createSimpleCombinationGenerator(vector, 2);

                for (ICombinatoricsVector<Odd> combination : gen) {
                        Odd oddA = combination.getValue(0);
                        Odd oddB = combination.getValue(1);
                        double impliedProbabilityA = 1 / oddA.getOdd() * 100;
                        double impliedProbabilityB = 1 / oddB.getOdd() * 100;
                        double totalProbability = impliedProbabilityA + impliedProbabilityB;
                        double stake = 100;
                        double stakeA = stake * (impliedProbabilityA/100) / (totalProbability/100);
                        double stakeB = stake * (impliedProbabilityB/100) / (totalProbability/100);
                        double profit = stake / (totalProbability/100) - stake;
                        ArbBet arbBet = ArbBet.builder().arb(100-totalProbability)
                                        .bookmakerA(oddA.getBookmaker())
                                        .bookmakerB(oddB.getBookmaker())
                                        .market(marketEntry.getKey())
                                        .oddA(oddA.getOdd())
                                        .oddB(oddB.getOdd())
                                        .stake(stake)
                                        .arbPercentage(String.format("%.2f%%", 100-totalProbability))
                                        .profit(profit)
                                        .profitString(String.format("R$ %.2f", profit))
                                        .stakeA(String.format("R$ %.2f", stakeA))
                                        .stakeB(String.format("R$ %.2f", stakeB))
                                        .build();

                        arbs.add(arbBet);

                }

                return arbs;
        }
}
