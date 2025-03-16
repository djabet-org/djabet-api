package hello.domain.services.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hello.domain.PartidaOdds;
import hello.domain.services.OddsService;
import hello.infrastructure.TheOddsAPI;
import hello.model.EVFilter;

@Service
public class OddsServiceImpl implements OddsService {

        @Autowired
        private TheOddsAPI theOddsAPI;

        @Override
        public List<PartidaOdds> getOdds(EVFilter evFilter) throws Throwable {
                if (evFilter.getLive()) {
                return theOddsAPI.getUpcomingOdds(evFilter).stream()
                                .filter( odd -> _filterSports(odd.getPartida().getSportKey(), evFilter.getSports()))
                                .collect(Collectors.toList());
                } else {
                        return theOddsAPI.getSportOdds(evFilter).stream()
                                .collect(Collectors.toList());

                }
        }

        private boolean _filterSports(String sport, String sportsList) {
                return sportsList.isBlank() ? true : Stream.of(sportsList.split(",")).anyMatch(sportInFilter -> sport.toLowerCase().contains(sportInFilter.toLowerCase()));
        }
}
