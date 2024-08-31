package hello.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NumerosCoresProbabilidadesDTO {

    private int roll;
    private ColorHitAndMissed blackHitAndMissed;
    private ColorHitAndMissed whiteHitAndMissed;
    private ColorHitAndMissed redHitAndMissed;

}
