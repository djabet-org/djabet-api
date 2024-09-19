package hello.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Market {
    private String key;
    private List<Outcome> outcomes;
}
