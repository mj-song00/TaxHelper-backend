package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LawSummaryRequest {

    @JsonProperty("법령일련번호")
    private String mst;

    @JsonProperty("법령키")
    private String lawKey;

    @JsonProperty("법령명한글")
    private String lawName;

    @JsonProperty("법령ID")
    private String lawId;

    @JsonProperty("법령구분명")
    private String lawType;

    @JsonProperty("공포번호")
    private String proclamationNumber;

    @JsonProperty("공포일자")
    private String proclamationDate;

    @JsonProperty("시행일자")
    private String effectiveDate;

    @JsonProperty("소관부처명")
    private String ministryName;

    @JsonProperty("소관부처코드")
    private String ministryCode;

    @JsonProperty("법령상세링크")
    private String detailLink;

    @JsonProperty("현행연혁코드")
    private String historyCode;

    @JsonProperty("자법타법여부")
    private String amendmentScope;

    @JsonProperty("제개정구분명")
    private String revisionType;

    @JsonProperty("id")
    private String apiListId;

    @JsonProperty("공동부령정보")
    private Object jointOrdinance;

    @JsonProperty("법령약칭명")
    private String lawNameAbbreviation;
}