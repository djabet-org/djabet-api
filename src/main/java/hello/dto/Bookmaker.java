package hello.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Bookmaker {
    private String name;
    private String key;
    private List<Market> markets; 
    
}