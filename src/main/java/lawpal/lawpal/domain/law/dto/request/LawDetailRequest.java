package lawpal.lawpal.domain.law.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lawpal.lawpal.domain.law.entity.Law;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

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
//
//    @JsonProperty("개정문")
//    private AmendmentsRequest 개정문;

    public Law toEntity() {
        return Law.builder()
                .lawSerialNumber(기본정보 != null ? 기본정보.getLawSerialNumber() : null)
                .lawId(기본정보 != null ? 기본정보.getLawId() : null)
                .lawKey(lawKey)
                .name(기본정보 != null ? 기본정보.getNameKor() : null)
                .shortName(기본정보 != null ? 기본정보.getNameShort() : null)
                .hanjaName(기본정보 != null ? 기본정보.getNameHanja() : null)
                .lawType(기본정보 != null ? 기본정보.getLawType() : null)
                .proclamationNumber(기본정보 != null ? 기본정보.getProclamationNo() : null)
                .proclamationDate(기본정보 != null ? 기본정보.getProclamationDate() : null)
                .effectiveDate(기본정보 != null ? 기본정보.getEnforcementDate() : null)
                .revisionType(기본정보 != null ? 기본정보.getRevisionType() : null)
                .revisionClassification(기본정보 != null ? 기본정보.getRevisionClassification() : null)
                .decisionType(기본정보 != null ? 기본정보.getDecisionType() : null)
                .proposalType(기본정보 != null ? 기본정보.getProposalType() : null)
                .ministry(기본정보 != null ? 기본정보.getMinistry().toEntity() : null)
                .jointMinistry(기본정보 != null ? 기본정보.getJoinMinistry() : null)
                .phoneNumber(기본정보 != null ? 기본정보.getPhoneNumber() : null)
                .language(기본정보 != null ? 기본정보.getLanguage() : null)
                .historyCode(기본정보 != null ? 기본정보.getHistoryCode() : null)
                .status(기본정보 != null ? 기본정보.getStatus() : null)
                .proclaimedYn(기본정보 != null ? 기본정보.getIsProclaimed() : null)
                .koreanYn(기본정보 != null ? 기본정보.getIsKorean() : null)
                .titleChangedYn(기본정보 != null ? 기본정보.getIsTitleChange() : null)
                .annexYn(기본정보 != null ? 기본정보.getHasAnnex() : null)
                .structureCode(기본정보 != null ? 기본정보.getStructureCode() : null)
                .detailLink(기본정보 != null ? 기본정보.getDetailLink() : null)
                .articles(new ArrayList<>())
                .supplements(new ArrayList<>())
//                .amendments(new ArrayList<>())
                .build();
    }
}