package hello.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartidaArbs {

    private Partida partida;
    private List<ArbBet> arbs;

}
