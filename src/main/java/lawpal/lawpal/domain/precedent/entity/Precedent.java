package lawpal.lawpal.domain.precedent.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lawpal.lawpal.domain.cases.entity.Case;
import lawpal.lawpal.domain.precedent.enums.PrecedentStatus;
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
    @Column(columnDefinition = "TEXT")
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


    /**
     * 판결요지
     */
    @Column(columnDefinition = "TEXT")
    private String judgmentSummary;

    /**
     *  본문 저장 상태 확인
     */
    @Column
    private PrecedentStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case cases;

    public void updateDetail(String fullText, String issue, String referenceArticles,
                             String referenceCases, String judgmentSummary) {
        this.fullText = fullText;
        this.issue = issue;
        this.referenceArticles = referenceArticles;
        this.referenceCases = referenceCases;
        this.judgmentSummary = judgmentSummary;
    }

    public void updateStatus(PrecedentStatus status) {
        this.status = status;
    }
}