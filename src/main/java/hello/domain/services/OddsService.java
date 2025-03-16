package hello.domain.services;

import java.util.List;

import hello.domain.PartidaOdds;
import hello.model.EVFilter;

public interface OddsService {

    List<PartidaOdds> getOdds(EVFilter evFilter) throws Throwable;
    
}
