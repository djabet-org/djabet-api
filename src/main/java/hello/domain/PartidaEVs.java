package hello.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartidaEVs {

    private Partida partida;
    private List<ValueBet> evs;

}
