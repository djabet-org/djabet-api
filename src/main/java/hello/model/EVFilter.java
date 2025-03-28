package hello.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EVFilter {
    private double minArb;
    @Builder.Default private double maxArb = Double.MAX_VALUE;
    private double minEv;
    @Builder.Default private double maxEv = Double.MAX_VALUE;
    private double minOdd;
    @Builder.Default private double maxOdd = Double.MAX_VALUE;
    @Builder.Default private String markets = "h2h";
    @Builder.Default private String sports = "";
    private String notSports;
    private String bookmakers;
    private Boolean live;
    private Boolean upcoming;
    private Boolean prematch;
}
