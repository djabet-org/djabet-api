package hello.data;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class DashboardDTO {
    @JsonProperty("contagem_cores")
    private CoresPercentualDTO coresPercentualDTO;

    private Map<Integer, int[]> numerosProximaCorProbabilidade;
}
