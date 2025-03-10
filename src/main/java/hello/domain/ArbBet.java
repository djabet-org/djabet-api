package hello.domain;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArbBet {
    private String event;
    private String market;
    @JsonProperty("arbs")
    private List<PartialArb> partialArbs;

}
