package hello.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import hello.domain.PartidaOdds;
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
            .map( partidaOdd -> _getEVs(100.0, partidaOdd, evFilter))
            .filter( partidaOdd -> partidaOdd.getEvs().size() > 0)
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
                .map(marketOdd -> _toEV(bankroll, partidaOdds, marketOdd, winProbabilityBasedOnPinnacle))
                .filter( ev -> ev.getEv() > evFilter.getMinEv() && ev.getEv() < evFilter.getMaxEv())
                // .peek(System.out::println)
                .filter( ev -> ev.getOdd() > evFilter.getMinOdd() && ev.getOdd() < evFilter.getMaxOdd())
                .filter( ev -> StringUtil.isBlank(evFilter.getMarkets()) ? true : evFilter.getMarkets().contains(ev.getMarket()))
                // .filter(valueBet -> !valueBet.getBookmaker().equals("pinnacle"))
                // .filter( valueBet -> !Helper.didStarted(valueBet.getPartida().getHorario()))
                // .filter(valueBet -> Helper.happensInTwoDays(valueBet.getPartida().getHorario()))
                // .filter(valuebet -> !Helper.getExcludedBookmakers().stream()
                //         .anyMatch(bookmaker -> bookmaker.equals(valuebet.getBookmaker())))
                .collect(Collectors.toList());

        return PartidaEVs.builder().partida(partidaOdds.getPartida()).evs(evs).build();
    }

    private ValueBet _toEV(double bankroll, PartidaOdds partidaOdds, Odd marketOdd, Map<String, Map<String, List<Odd>>> baseProbabilityMap) {
        if (!baseProbabilityMap.containsKey(marketOdd.getMarket())
                || !baseProbabilityMap.get(marketOdd.getMarket()).containsKey(marketOdd.getOutcome())) {
            return ValueBet.builder().build();
        }

        double pinnacleOdd = baseProbabilityMap.get(marketOdd.getMarket()).get(marketOdd.getOutcome()).get(0).getOdd();
        double baseProbability = 100.0 / (pinnacleOdd);
        double ev = (baseProbability / (100 / marketOdd.getOdd())) - 1;

        return ValueBet.builder()
                .evPercentage(String.format("%.2f%%", ev * 100))
                .ev(ev)
                .market(marketOdd.getMarket())
                .bookmaker(marketOdd.getBookmaker())
                .odd(marketOdd.getOdd())
                .outcome(marketOdd.getOutcome())
                .sharpOdd(pinnacleOdd)
                .betAmmount(Helper.calculateBetAmount(baseProbability, bankroll, marketOdd.getOdd()))
                .build();
    }

    @Override
    public List<PartidaOdds> getOdds(EVFilter evFilter) throws JsonMappingException, JsonProcessingException {
        return theOddsAPI.getUpcomingOdds(evFilter);
    }

}
