package hello.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.paukov.combinatorics.CombinatoricsFactory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import hello.model.EVFilter;

public class ArbBetsService {

    public List<PartidaArbs> getArbs(List<PartidaOdds> partidasOdds, EVFilter evFilter) {
        return partidasOdds.stream().map( partidaOdds -> _getArbs(partidaOdds)).collect(Collectors.toList());
    }

    private PartidaArbs _getArbs(PartidaOdds partidaOdds) {
        Map<String, List<Odd>> marketsOdds = partidaOdds.getOdds().stream()
                .collect(Collectors.groupingBy(Odd::getMarket));

        List<ArbBet> allArbBets = new ArrayList<>();

        for (Entry<String, List<Odd>> marketEntry : marketsOdds.entrySet()) {
            List<ArbBet> arbs = _calculateArbs(marketEntry);
            allArbBets.addAll(arbs);
        }

        return PartidaArbs.builder().arbs(allArbBets).partida(partidaOdds.getPartida()).build();

    }

    private List<ArbBet> _calculateArbs(Entry<String, List<Odd>> marketEntry) {
        List<ArbBet> arbs = new ArrayList<>();
        List<Odd> odds = marketEntry.getValue();
        Odd[] odds2 = odds.toArray(Odd[]::new);

        ICombinatoricsVector<Odd> vector = CombinatoricsFactory.createVector(odds2);
        Generator<Odd> gen = CombinatoricsFactory.createSimpleCombinationGenerator(vector, odds.size());

           for (ICombinatoricsVector<Odd> combination : gen) {
                double arb = 1 - (1 / oddA.getOdd() + 1 / oddB.getOdd());
                ArbBet arbBet = ArbBet.builder().arb(arb)
                        .bookmakerA(oddA.getBookmaker())
                        .bookmakerB(oddB.getBookmaker())
                        .market(marketEntry.getKey())
                        .oddA(oddA.getOdd())
                        .oddB(oddB.getOdd())
                        .build();


           }

        for (int i = 0; i < odds.size(); i++) {
            for (int i2 = i + 1; i2 < odds.size(); i2++) {
                Odd oddA = odds.get(i);
                Odd oddB = odds.get(i2);
                        arbs.add(arbBet);
            }

        }

            return arbs;
    }

}