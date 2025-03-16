package hello.domain.services;

import java.util.List;

import hello.domain.ArbBet;
import hello.domain.PartidaOdds;
import hello.model.EVFilter;

public interface ArbService {

    List<ArbBet> getArbs(List<PartidaOdds> partidasOdds, EVFilter evFilter);
    
}
