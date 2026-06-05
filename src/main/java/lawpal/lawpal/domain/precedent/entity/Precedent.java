package lawpal.lawpal.domain.precedent.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lawpal.lawpal.domain.cases.entity.Case;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "precedent")
public class Precedent extends Timestamped  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 데이터출처명 : 국세법령정보시스템(본문검색시 html만 가능)
     * 데이터출처명 : 대법원(json 가능)
     */
    @Column(nullable = false)
    private String dataSourceName;

    /**
     * 판례일련번호: 613683
     */
    @Column(nullable = false, unique=true)
    private String precedentSerialNumber;


    /**
     * 판결유형: 판결
     */
    @Column
    private String judgmentType;

    /**
     * 판시사항
     */
    @Column(columnDefinition = "TEXT")
    private String issue;

    /**
     * 참조판례
     */
    @Column
    private String referenceCases;

    /**
     * 사건종류명: 세무
     */
    @Column
    private String caseType;

    /**
     * 참조조문
     */
    @Column(columnDefinition = "TEXT")
    private String referenceArticles;


    /**
     *판례내용
     */
    @Column(columnDefinition = "TEXT")
    private String fullText;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case cases;
}