package hello.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartialArb {

    @JsonIgnore
    private double roi;
    private String stake;
    @JsonIgnore
    private double profit;
    private double totalPayout; 
    @JsonProperty("books")
    private List<BookmakerArb> bookmakerArbs; 

    @JsonProperty("profit")
    public String getProfit(){
        return String.format("R$ %.2f", profit);

    }

    @JsonProperty("roi")
    public String getRoi(){
        return String.format("%.2f%%", roi);

    }

    @JsonProperty("totalPayout")
    public String getTotalPayout(){
        return String.format("R$ %.2f", totalPayout);

    }
}
