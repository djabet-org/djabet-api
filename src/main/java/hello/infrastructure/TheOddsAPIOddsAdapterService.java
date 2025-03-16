package hello.infrastructure;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import hello.domain.PartidaOdds;
import hello.model.EVFilter;

public interface TheOddsAPIOddsAdapterService {
    List<PartidaOdds> adapt(JsonNode node, EVFilter filter);
}
