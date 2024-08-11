package hello.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CoresPercentualDTO {
    @JsonProperty("qtdVermelha")
    private long red;
    @JsonProperty("qtdPreta")
    private long black;
    @JsonProperty("qtdBranca")
    private long white;
    @JsonProperty("percentagePreta")
    private String percentageBlack;
    @JsonProperty("percentageBranca")
    private String percentageWhite;
    @JsonProperty("percentageVermelha")
    private String percentageRed;
}
