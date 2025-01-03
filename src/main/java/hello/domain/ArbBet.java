package hello.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArbBet {
    private double arb;
    private double stake;
    private String stakeA;
    private String stakeB;
    private String arbPercentage;
    private String market;
    private String bookmakerA;
    private String bookmakerB;
    private double oddA;
    private double oddB;
    @JsonIgnore
    private double profit;
    @JsonProperty("profit")
    private String profitString;

}
