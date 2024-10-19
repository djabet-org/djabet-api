package hello.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EVFilter {
    private double minEv;
    private double maxEv;
    @Builder.Default private String markets = "h2h";
}
