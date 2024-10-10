package hello.domain;

import java.util.List;

import org.springframework.stereotype.Service;

public interface BettingService {

    List<PartidaEVs> calculateEVs(List<PartidaOdds> partidasOdds);

    List<PartidaOdds> getOdds();

    
}
