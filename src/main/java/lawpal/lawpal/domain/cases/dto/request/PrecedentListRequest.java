package lawpal.lawpal.domain.cases.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrecedentListRequest {
    @JsonProperty("PrecSearch")
    private PreSearchRequest preSearch;
}
