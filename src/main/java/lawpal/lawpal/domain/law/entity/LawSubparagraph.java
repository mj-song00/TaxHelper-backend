package lawpal.lawpal.domain.law.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lombok.*;


/**
 * 호에 대한 table 입니다.
 * 호는 필요할때만 저장됩니다.
 */
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "law_subparagraphs")
public class LawSubparagraph extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 호 번호 ex) 1.
    @Column(nullable = false)
    private String subparagraphNumber;

    // 호 내용
    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_paragraphs_id", nullable = false)
    private LawParagraph lawParagraph;
}
