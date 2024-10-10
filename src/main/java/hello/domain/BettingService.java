package hello.domain;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface BettingService {

    List<PartidaEVs> calculateEVs(List<PartidaOdds> partidasOdds);

    List<PartidaOdds> getOdds() throws JsonMappingException, JsonProcessingException;

    
}
