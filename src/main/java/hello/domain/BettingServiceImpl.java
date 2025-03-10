package hello.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.paukov.combinatorics.CombinatoricsFactory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.parser.Part;
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
        public List<ArbBet> getArbs(List<PartidaOdds> partidasOdds, EVFilter evFilter) {
                return partidasOdds.stream()
                                .filter(partidaOdds -> _matchesSportsFilter(partidaOdds, evFilter))
                                .filter(partidaOdds -> _diffSports(partidaOdds, evFilter.getNotSports()))
                                .map(partidaOdds -> _getArbs(partidaOdds, evFilter))
                                .flatMap(List::stream)
                                // .peek(System.out::println)
                                .collect(Collectors.toList());
        }

        private boolean _diffSports(PartidaOdds partidaOdds, String blacklistedSports) {
                return StringUtils.isBlank(blacklistedSports) ? true
                                : Arrays.asList(blacklistedSports.split(",")).stream()
                                                .noneMatch(sport -> partidaOdds.getPartida().getSportKey()
                                                                .contains(sport.toLowerCase()));

        }

        private boolean _matchesSportsFilter(PartidaOdds partidaOdds, EVFilter evFilter) {
                return StringUtil.isBlank(evFilter.getSports()) ? true
                                : Arrays.asList(evFilter.getSports().split(",")).stream()
                                                .anyMatch(sport -> partidaOdds.getPartida().getSportKey()
                                                                .contains(sport.toLowerCase()));

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
                return theOddsAPI.getUpcomingOdds(evFilter).stream()
                                .filter(partidaOdd -> Objects.isNull(evFilter.getLive()) ? true
                                                : partidaOdd.getPartida().isLive())
                                .filter(partidaOdd -> Objects.isNull(evFilter.getPrematch()) ? true
                                                : !partidaOdd.getPartida().isLive())
                                .collect(Collectors.toList());
        }

        private List<ArbBet> _getArbs(PartidaOdds partidaOdds, EVFilter evFilter) {
                Map<String, List<Odd>> marketsOdds = partidaOdds.getOdds().stream()
                                .filter(odd -> _filterMarkets(odd.getMarket(), evFilter.getMarkets()))
                                .collect(Collectors.groupingBy(Odd::getMarket));

                List<ArbBet> allArbBets = new ArrayList<>();

                for (Entry<String, List<Odd>> marketEntry : marketsOdds.entrySet()) {
                        List<PartialArb> partialArbs = _calculateArbs(marketEntry);
                        allArbBets.add(_toArbBet(partidaOdds, marketEntry.getKey(), partialArbs));
                }

                return allArbBets;

        }

        private boolean _filterMarkets(String market, String marketsList) {
                return Stream.of(marketsList.split(",")).anyMatch(marketInFilter -> marketInFilter.equals(market));
        }

        private ArbBet _toArbBet(PartidaOdds partidaOdds, String market, List<PartialArb> partialArbs) {
                return ArbBet.builder()
                                .partialArbs(partialArbs)
                                .event(partidaOdds.getPartida().getName())
                                .market(market)
                                .build();

        }

        // private List<ArbBet> _filter(List<ArbBet> allArbBets, EVFilter evFilter) {
        //         return allArbBets.stream()
        //                         .filter(arb -> arb.getProfit() > 0)
        //                         .filter(arb -> arb.getArb() > evFilter.getMinArb())
        //                         .filter(arb -> arb.getArb() < evFilter.getMaxArb())
        //                         .filter(arb -> evFilter.getMarkets().contains(arb.getMarket()))
        //                         .collect(Collectors.toList());
        // }

        private List<PartialArb> _calculateArbs(Entry<String, List<Odd>> marketEntry) {
                List<Odd> odds = marketEntry.getValue();
                Odd[] odds2 = odds.toArray(Odd[]::new);

                Map<String, List<Odd>> outcomeBookmakers = odds.stream()
                                .collect(Collectors.groupingBy(Odd::getOutcome));

                ICombinatoricsVector<Odd> vector = CombinatoricsFactory.createVector(odds2);

                Generator<Odd> gen = CombinatoricsFactory.createSimpleCombinationGenerator(vector,
                                outcomeBookmakers.size());

                return _getPartialArbs(gen);

        }

        private List<PartialArb> _getPartialArbs(Generator<Odd> generatorCombinations) {
                return StreamSupport.stream(generatorCombinations.spliterator(), false)
                                .filter(this::_diffBookmakers)
                                .filter(this::_diffOutcomes)
                                .filter(this::_foundArb)
                                .map(this::_combinationToPartialArb)
                                .collect(Collectors.toList());
        }

        private PartialArb _combinationToPartialArb(ICombinatoricsVector<Odd> combination) {
                double totalProbability = _totalProbability(combination);
                double stake = 100;

                List<BookmakerArb> bookmakersArbs = combination.getVector().stream()
                                .map(odd -> _oddToBookmakerArb(odd, stake, totalProbability))
                                .collect(Collectors.toList());

                double roi = 1-bookmakersArbs.stream().mapToDouble( arb -> 1/arb.getOdd()).sum();

                return PartialArb.builder()
                        .stake("R$ 100")
                        .roi(roi)
                        .profit(bookmakersArbs.get(0).getPayoutValue()-stake)
                        .totalPayout(bookmakersArbs.get(0).getPayoutValue())
                        .bookmakerArbs(bookmakersArbs).build();

        }

        private boolean _foundArb(ICombinatoricsVector<Odd> combination) {
                return _totalProbability(combination) < 1;
        }

        private boolean _diffBookmakers(ICombinatoricsVector<Odd> combination) {
                return combination.getVector().stream().map(Odd::getBookmaker).distinct().count() > 1;
        }

        private boolean _diffOutcomes(ICombinatoricsVector<Odd> combination) {
                return combination.getVector().stream().map(Odd::getOutcome).distinct().count() > 1;
        }

        private double _totalProbability(ICombinatoricsVector<Odd> combination) {
                return StreamSupport.stream(combination.spliterator(), false).mapToDouble(odd -> 1 / odd.getOdd())
                                .sum();
        }

        private BookmakerArb _oddToBookmakerArb(Odd odd, double stake, double totalProbability) {
                double impliedProbability = 1 / odd.getOdd();
                double stakeOdd = stake * (impliedProbability / 100) / (totalProbability / 100);
                return BookmakerArb.builder()
                                .bookmaker(odd.getBookmaker())
                                .outcome(odd.getOutcome())
                                .odd(odd.getOdd())
                                .stake(String.format("R$ %.2f", stakeOdd))
                                .payoutValue(stakeOdd * odd.getOdd())
                                .build();

        }
}
