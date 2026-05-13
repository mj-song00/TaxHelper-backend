package lawpal.lawpal.domain.law.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 조문에 대한 table 입니다.
 * 각 조문에 대항 항이 포함될 수 있습니다.
 */

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "law_articles")
public class LawArticle extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 조문 키
     * ex) 0001001
     */
    @Column(nullable = false, unique = true)
    private String articleKey;

    /**
     * 조문 번호
     * ex) 1
     */
    @Column(nullable = false)
    private String articleNumber;

    /**
     * 조문 제목
     * ex) 목적
     */
    private String articleTitle;

    /**
     * 조문 내용
     */
    @Lob
    @Column(nullable = false)
    private String articleContent;

    /**
     * 조문 시행일자
     * ex) 19971231
     */
    @Column(length = 8)
    private String effectiveDate;

    /**
     * 조문 변경 여부
     * Y / N
     */
    @Column(length = 1)
    private String changedYn;

    /**
     * 제개정 유형
     * 일부개정 / 타법개정 ...
     */
    private String revisionType;

    /**
     * 조문 이동 이전 번호
     */
    private String movedPrevious;

    /**
     * 조문 이동 이후 번호
     */
    private String movedNext;

    /**
     * 조문 여부
     */
    private String articleYn;

    @OneToMany(mappedBy = "lawArticle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LawParagraph> paragraphs = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laws_id", nullable = false)
    private Law law;
}
