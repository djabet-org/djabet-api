package hello.domain;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArbBet {
    private String sportKey;
    private String event;
    private String market;
    private String homeTeam;
    private String awayTeam;
    private String horario;
    private String torneio;
    private boolean live;
    @JsonProperty("arbs")
    private List<PartialArb> partialArbs;

}
