package hello.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Outcome {
    private String market;
    private String name;
    private double odd;
}
