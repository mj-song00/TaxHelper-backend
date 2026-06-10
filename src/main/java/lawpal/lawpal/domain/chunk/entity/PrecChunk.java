package lawpal.lawpal.domain.chunk.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lawpal.lawpal.domain.chunk.enums.PrecChunkType;
import lawpal.lawpal.domain.precedent.entity.Precedent;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "precChunks")
public class PrecChunk extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 청크 유형
     * ISSUE : 판시사항
     * SUMMARY : 판결요지
     * FULL_TEXT : 판례내용
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrecChunkType chunkType;

    /**
     * 검색 결과 제목
     * ex) 판시사항, 판결요지, 판례내용
     */
    @Column(nullable = false)
    private String title;

    /**
     * 실제 검색 대상 내용
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "precedent_id", nullable = false)
    private Precedent precedent;
}
