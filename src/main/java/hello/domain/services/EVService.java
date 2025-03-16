package hello.domain.services;

import java.util.List;

import hello.domain.PartidaEVs;
import hello.domain.PartidaOdds;
import hello.model.EVFilter;

public interface EVService {

    List<PartidaEVs> calculateEVs(List<PartidaOdds> partidasOdds, EVFilter evFilter);
    
}
