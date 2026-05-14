package lawpal.lawpal.domain.law.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "law_chunks")
public class LawChunk extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 청크 순서
     */
    @Column(nullable = false)
    private Integer chunkIndex;

    /**
     * 임베딩 대상 텍스트
     */
    @Lob
    @Column(nullable = false)
    private String content;

    /**
     * 조문 번호
     * ex) 제3조
     */
    private String articleNumber;

    /**
     * 항 번호
     * ex) 제1항
     */
    private String paragraphNumber;

    /**
     * 호 번호
     * ex) 제2호
     */
    private String subparagraphNumber;

    /**
     * 법령명
     */
    private String lawName;

    /**
     * 소득세법 제3조 제1항 같은 검색용 path
     */
    @Column(length = 500)
    private String referencePath;

    /**
     * 임베딩 여부
     */
    private Boolean embedded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laws_id", nullable = false)
    private Law law;
}
