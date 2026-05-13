package lawpal.lawpal.domain.law.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 조문에 속한 항에 대한 table 입니다.
 * 호가 포함될 수 있습니다.
 */
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "law_paragraphs")
public class LawParagraph extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 항 번호 ex) ①
    @Column(nullable = false)
    private String paragraphNumber;

    // 항 내용
    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_article_id", nullable = false)
    private LawArticle lawArticle;

    @OneToMany(mappedBy = "lawParagraph", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LawSubparagraph> subparagraphs = new ArrayList<>();

}
