package lawpal.lawpal.domain.law.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lawpal.lawpal.domain.ministry.entity.Ministry;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 법령의 기본정보에 대한 table 입니다.
 *  법령일련번호, 볍령명, 한글 법령명, 한자 법령명, 법 종류, 공포번호, 공포일자, 시행일자, 소관부처, 현행, 밥령상세 링크가 있습니다.
 */
@Entity
@Getter
@Table(name = "laws")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Law extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 법령일련번호
     * MST 값
     * ex) 253527
     */
    @Column(nullable = false, unique = true)
    private String lawSerialNumber;

    /**
     * 법령ID
     * ex) 010719
     */
    @Column
    private String lawId;

    /**
     * 법령키
     * ex) 0068701997123100673
     */
    @Column(nullable = false, unique = true)
    private String lawKey;

    /**
     * 법령명 한글
     */
    @Column(nullable = false)
    private String name;

    /**
     * 한글 법령명 약칭
     */
    private String shortName;

    /**
     * 법령명 한자
     */
    private String hanjaName;

//    /**
//     * 법 종류
//     * 법률 / 시행령 / 시행규칙 등
//     */
//    @Column(nullable = false)
//    private String lawType;

    /**
     * 공포번호
     */
    private String proclamationNumber;

    /**
     * 공포일자
     */
    private String proclamationDate;

    /**
     * 시행일자
     */
    private String effectiveDate;

    /**
     * 제개정구분
     * 일부개정 / 전부개정 / 타법개정 등
     */
    private String revisionType;

    /**
     * 제개정구분 분류
     */
    private String revisionClassification;

    /**
     * 의결종류
     */
    private String decisionType;

    /**
     * 제안종류
     */
    private String proposalType;


    /**
     * 공동부령 정보
     */
    private String jointMinistry;

    /**
     * 전화번호
     */
    private String phoneNumber;

    /**
     * 언어
     */
    private String language;

    /**
     * 현행 / 연혁 여부
     */
    private String historyCode;

    /**
     * 법령 상태
     */
    private String status;

    /**
     * 공포법령 여부
     */
    private String proclaimedYn;

    /**
     * 한글법령 여부
     */
    private String koreanYn;

    /**
     * 제명변경 여부
     */
    private String titleChangedYn;

    /**
     * 별표편집 여부
     */
    private String annexYn;

    /**
     * 편장절관 구조코드
     */
    private String structureCode;

    /**
     * 법령 상세 링크
     */
    @Column(length = 1000)
    private String detailLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ministries_id")
    private Ministry ministry;

    @OneToMany(mappedBy = "law", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LawArticle> articles = new ArrayList<>();

    @OneToMany(mappedBy = "law", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LawSupplement> supplements = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_type_id", nullable = false)
    private LawType lawType;

//    @OneToMany(mappedBy = "law", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<LawAmendment> amendments = new ArrayList<>();
}