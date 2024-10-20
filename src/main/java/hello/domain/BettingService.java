package hello.domain;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import hello.model.EVFilter;

public interface BettingService {

    List<PartidaEVs> calculateEVs(List<PartidaOdds> partidasOdds, EVFilter evFilter);

    List<PartidaOdds> getOdds(EVFilter evFilter) throws JsonMappingException, JsonProcessingException;

    
}
