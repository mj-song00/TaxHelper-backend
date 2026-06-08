package lawpal.lawpal.domain.cases.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PreSearchRequest {
    @JsonProperty("키워드")
    private String keyword;

    @JsonProperty("page")
    private String page;

    @JsonProperty("target")
    private String target;

    @JsonProperty("totalCnt")
    private String totalCount;

    @JsonProperty("section")
    private String section;

    @JsonProperty("prec")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<PrecSummaryRequest> prec;
}
