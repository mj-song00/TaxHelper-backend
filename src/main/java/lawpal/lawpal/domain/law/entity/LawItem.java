package lawpal.lawpal.domain.law.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * 목에 대한 table입니다.
 * 필요시 저장됩니다.
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "law_items")
public class LawItem extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 목 번호 ex) 가.
    @Column(nullable = false)
    private String itemNumber;

    // 목 내용
    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_subparagraphs_id", nullable = false)
    private LawSubparagraph lawSubparagraph;
}