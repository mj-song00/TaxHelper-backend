package lawpal.lawpal.domain.law.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lombok.*;

/**
 * 부칙에 대한 table입니다.
 * 존재하지 않을 수도 있습니다.
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "law_supplements")
public class LawSupplement extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 부칙키
    @Column(nullable = false, unique = true)
    private String supplementKey;

    // 부칙공포일자
    @Column(length = 8)
    private String proclamationDate;

    // 부칙공포번호
    private String proclamationNumber;

    // 부칙내용
    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_id", nullable = false)
    private Law law;

}
