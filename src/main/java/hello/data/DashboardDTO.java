package hello.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DashboardDTO {
    @JsonProperty("contagem_cores")
    private CoresPercentualDTO coresPercentualDTO;

}
