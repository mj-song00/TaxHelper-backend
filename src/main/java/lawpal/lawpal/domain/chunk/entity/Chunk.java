package lawpal.lawpal.domain.chunk.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lawpal.lawpal.domain.chunk.enums.LawChunkType;
import lawpal.lawpal.domain.law.entity.Law;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chunks")
public class Chunk extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 법령의 청크인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_id", nullable = false)
    private Law law;

    /**
     * 청크 타입
     * ARTICLE / SUPPLEMENT / AMENDMENT
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LawChunkType chunkType;

    /**
     * 원본 테이블의 id
     * LawArticle id / LawSupplement id / LawAmendment id
     */
    @Column(nullable = false)
    private Long sourceId;

    /**
     * 검색 결과에 보여줄 제목
     * ex) 제1조 목적, 부칙 제000호, 개정문
     */
    @Column(nullable = false)
    private String title;

    /**
     * 실제 검색에 사용할 내용
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
