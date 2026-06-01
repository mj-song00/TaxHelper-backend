package lawpal.lawpal.domain.law.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LawDetailRequest {

    @JsonProperty("기본정보")
    private BasicInfo 기본정보;

    @JsonProperty("법령키")
    private String lawKey;

    @JsonProperty("조문")
    private ArticleRequest 조문;

    @JsonProperty("부칙")
    private SupplementsRequest 부칙;

    @JsonProperty("개정문")
    private AmendmentsRequest 개정문;
}