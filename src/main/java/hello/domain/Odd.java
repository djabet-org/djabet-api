package hello.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Odd {
    private String bookmaker;
    private String market;
    private double odd;
    private String outcome;
}
