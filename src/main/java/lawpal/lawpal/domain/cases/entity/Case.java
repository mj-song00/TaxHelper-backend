package lawpal.lawpal.domain.cases.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cases")
public class Case extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사건번호 : 의정부지방법원-2023-구합-14003
     */
    @Column(nullable= false)
    private String caseNumber;


    /**
     * 사건종류코드 : 400107
     */
    @Column(nullable= false)
    private String caseCode;

    /**
     * 사건종류명: 민사
     */
    @Column(nullable= false)
    private String caseTypeName;

    /**
     * 선고: 선고
     */
    @Column
    private String sentence;

    /**
     * 선고일자 : 2026.01.15
     */

    @Column(nullable= false)
    private String sentencingDate;

    /**
     * 법원종류코드
     */
    @Column
    private String courtTypeCode;

    /**
     * 법원명:서울행정법원
     */
    @Column
    private String courtName;

    /**
     * 사건명: 상자산 거래차익이 구 소득세법 규정에 따른 국내원천소득인지 여부
     */
    @Column
    private String caseName;
}

