package hello.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Torneio {

    private String key;
    private String group;
    private String title;
    private String description;
    private boolean active;
    private boolean hasOutrights;

}
