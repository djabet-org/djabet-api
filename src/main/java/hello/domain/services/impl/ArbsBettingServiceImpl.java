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

import hello.domain.ArbBet;
import hello.domain.BookmakerArb;
import hello.domain.Odd;
import hello.domain.PartialArb;
import hello.domain.PartidaOdds;
import hello.domain.services.ArbService;
import hello.model.EVFilter;

@Service
public class ArbsBettingServiceImpl implements ArbService {

        @Override
        public List<ArbBet> getArbs(List<PartidaOdds> partidasOdds, EVFilter evFilter) {
                return partidasOdds.stream()
                                .map(partidaOdds -> _getArbs(partidaOdds, evFilter))
                                .flatMap(List::stream)
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

                return allArbBets.stream().filter(arb -> !arb.getPartialArbs().isEmpty()).collect(Collectors.toList());

        }

        private boolean _filterMarkets(String market, String marketsList) {
                return Stream.of(marketsList.split(",")).anyMatch(marketInFilter -> marketInFilter.equals(market));
        }

        private ArbBet _toArbBet(PartidaOdds partidaOdds, String market, List<PartialArb> partialArbs) {
                return ArbBet.builder()
                                .event(partidaOdds.getPartida().getName())
                                .sportKey(partidaOdds.getPartida().getSportKey())
                                .homeTeam(partidaOdds.getPartida().getHomeTeam())
                                .awayTeam(partidaOdds.getPartida().getAwayTeam())
                                .horario(partidaOdds.getPartida().getHorario())
                                .torneio(partidaOdds.getPartida().getTorneio())
                                .live(partidaOdds.getPartida().isLive())
                                .partialArbs(partialArbs)
                                .market(market)
                                .build();

        }

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

                double roi = 1 - bookmakersArbs.stream().mapToDouble(arb -> 1 / arb.getOdd()).sum();

                return PartialArb.builder()
                                .stake("R$ 100")
                                .roi(roi)
                                .profit(bookmakersArbs.get(0).getPayoutValue() - stake)
                                .totalPayout(bookmakersArbs.get(0).getPayoutValue())
                                .bookmakerArbs(bookmakersArbs).build();

        }

        private boolean _foundArb(ICombinatoricsVector<Odd> combination) {
                return _totalProbability(combination) < 1;
        }

        private boolean _diffBookmakers(ICombinatoricsVector<Odd> combination) {
                return combination.getVector().stream()
                                .collect(Collectors.groupingBy(Odd::getBookmaker, Collectors.counting()))
                                .values()
                                .stream()
                                .allMatch(k -> k == 1);
        }

        private boolean _diffOutcomes(ICombinatoricsVector<Odd> combination) {
                return combination.getVector().stream()
                                .collect(Collectors.groupingBy(Odd::getOutcome, Collectors.counting()))
                                .values()
                                .stream()
                                .allMatch(k -> k == 1);
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
                                .link(odd.getLink())
                                .outcome(odd.getOutcome())
                                .odd(odd.getOdd())
                                .stake(String.format("R$ %.2f", stakeOdd))
                                .payoutValue(stakeOdd * odd.getOdd())
                                .build();

        }
}
