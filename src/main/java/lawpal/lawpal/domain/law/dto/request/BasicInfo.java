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
public class BasicInfo {

    /**
     * 법령ID
     * ex) 010719
     */
    @JsonProperty("법령ID")
    private String lawId;

    /**
     * 법령일련번호(MST)
     * ex) 253527
     */
    @JsonProperty("법령일련번호")
    private String lawSerialNumber;

    /**
     * 법령명 한글
     */
    @JsonProperty("법령명_한글")
    private String nameKor;

    /**
     * 법령명 한자
     */
    @JsonProperty("법령명_한자")
    private String nameHanja;

    /**
     * 법령명 약칭
     */
    @JsonProperty("법령명약칭")
    private String nameShort;

    /**
     * 의결구분
     */
    @JsonProperty("의결구분")
    private String decisionType;

    /**
     * 제안구분
     */
    @JsonProperty("제안구분")
    private String proposalType;

    /**
     * 공포번호
     */
    @JsonProperty("공포번호")
    private String proclamationNo;

    /**
     * 공포일자
     * ex) 2024-01-01
     */
    @JsonProperty("공포일자")
    private String proclamationDate;

    /**
     * 시행일자
     */
    @JsonProperty("시행일자")
    private String  enforcementDate;

    /**
     * 전화번호
     */
    @JsonProperty("전화번호")
    private String phoneNumber;

    /**
     * 언어
     */
    @JsonProperty("언어")
    private String language;

    /**
     * 제개정여부
     */
    @JsonProperty("제개정여부")
    private String revisionType;

    /**
     * 제개정구분
     */
    @JsonProperty("제개정구분")
    private String revisionClassification;

    /**
     * 공동부령정보
     */
    @JsonProperty("공동부령정보")
    private String joinMinistry;

    /**
     * 공포법령여부
     */
    @JsonProperty("공포법령여부")
    private String isProclaimed;

    /**
     * 한글법령여부
     */
    @JsonProperty("한글법령여부")
    private String isKorean;

    /**
     * 제명변경여부
     */
    @JsonProperty("제명변경여부")
    private String isTitleChange;

    /**
     * 별표편집여부
     */
    @JsonProperty("별표편집여부")
    private String hasAnnex;

    /**
     * 편장절관 구조코드
     */
    @JsonProperty("편장절관")
    private String structureCode;

    /**
     * 현행/연혁 코드
     */
    @JsonProperty("현행연혁코드")
    private String historyCode;

    /**
     * 법령상태
     */
    @JsonProperty("법령상태")
    private String status;

    /**
     * 법령상세링크
     */
    @JsonProperty("법령상세링크")
    private String detailLink;


    /**
     * 법종구분
     */
    @JsonProperty("법종구분")
    private LawTypeRequest lawType;

    /**
     * 소관부처
     */
    @JsonProperty("소관부처")
    private MinistryRequest ministry;

    /**
     * 연락부서
     */
    @JsonProperty("연락부서")
    private DepartmentRequest department;
}