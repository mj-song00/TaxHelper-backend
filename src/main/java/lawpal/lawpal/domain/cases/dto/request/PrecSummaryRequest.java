package lawpal.lawpal.domain.cases.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrecSummaryRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("사건번호")
    private String caseNumber;

    @JsonProperty("데이터출처명")
    private String dataSourceName;

    @JsonProperty("사건종류코드")
    private String caseCode;

    @JsonProperty("사건종류명")
    private String caseTypeName;

    @JsonProperty("선고")
    private String sentence;

    @JsonProperty("선고일자")
    private String sentencingDate;

    @JsonProperty("판례일련번호")
    private String precedentSerialNumber;

    @JsonProperty("판결유형")
    private String judgmentType;

    @JsonProperty("법원종류코드")
    private String courtTypeCode;

    @JsonProperty("법원명")
    private String courtName;

    @JsonProperty("사건명")
    private String caseName;

    @JsonProperty("판례상세링크")
    private String link;
}
