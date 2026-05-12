package lawpal.lawpal.domain.law.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lawpal.lawpal.domain.ministry.entity.Ministry;
import lombok.*;

import java.time.LocalDate;

/**
 * 법령의 기본정보에 대한 table 입니다.
 *  id,
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

    // 법령일련번호
    @Column(nullable = false, unique = true)
    private Long lawSerialNumber;

    // 법령명
    @Column(nullable = false)
    private String name;

    // 한글 법령명 약칭
    private String shortName;

    // 법령명 한자
    private String hanjaName;

    // 법 종류 (법률 / 시행령 / 시행규칙 등)
    @Column(nullable = false)
    private String lawType;

    // 공포번호
    private String proclamationNumber;

    // 공포일자
    private LocalDate proclamationDate;

    // 시행일자
    private LocalDate effectiveDate;

    // 소관부처명
    private String ministryName;

    // 현행 / 연혁 여부
    private String historyCode;

    // 법령 상태
    private String status;

    // 법령 상세 링크
    @Column(length = 1000)
    private String detailLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ministries_id")
    private Ministry ministry;
}
