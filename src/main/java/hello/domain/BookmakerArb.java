package hello.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookmakerArb {

    private String stake;
    private String bookmaker;
    private String outcome;
    private double odd; 
    @JsonIgnore
    private double payoutValue;

    @JsonProperty("payout")
    public String getPayout(){
        return String.format("R$ %.2f", payoutValue);

    }

}
