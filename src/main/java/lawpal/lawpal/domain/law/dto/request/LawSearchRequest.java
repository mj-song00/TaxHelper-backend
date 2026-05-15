package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LawSearchRequest {

    @JsonProperty("target")
    private String target;

    @JsonProperty("키워드")
    private String keyword;

    @JsonProperty("section")
    private String section;

    @JsonProperty("page")
    private String page;

    @JsonProperty("totalCnt")
    private String totalCount;

    @JsonProperty("resultMsg")
    private String resultMessage;

    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("law")
    private List<LawSummaryRequest> law;
}