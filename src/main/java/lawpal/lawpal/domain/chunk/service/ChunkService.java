package lawpal.lawpal.domain.chunk.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lawpal.lawpal.domain.chunk.dto.response.ChunkResponse;
import lawpal.lawpal.domain.chunk.dto.response.ChunkResponseItem;
import lawpal.lawpal.domain.chunk.dto.response.PrecChunkResponseItem;
import lawpal.lawpal.domain.chunk.dto.response.PrecResponse;
import lawpal.lawpal.domain.chunk.entity.Chunk;
import lawpal.lawpal.domain.chunk.entity.PrecChunk;
import lawpal.lawpal.domain.chunk.enums.LawChunkType;
import lawpal.lawpal.domain.chunk.enums.PrecChunkType;
import lawpal.lawpal.domain.chunk.repository.ChunkRepository;
import lawpal.lawpal.domain.chunk.repository.PrecChunkRepository;
import lawpal.lawpal.domain.law.entity.*;
import lawpal.lawpal.domain.law.repository.LawAmendmentRepository;
import lawpal.lawpal.domain.law.repository.LawArticleRepository;
import lawpal.lawpal.domain.law.repository.LawSupplementRepository;
import lawpal.lawpal.domain.precedent.entity.Precedent;
import lawpal.lawpal.domain.precedent.repository.PrecedentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChunkService {

    private final LawArticleRepository lawArticleRepository;
    private final LawSupplementRepository lawSupplementRepository;
    private final LawAmendmentRepository lawAmendmentRepository;
    private final ChunkRepository chunkRepository;
    private final PrecedentRepository precedentRepository;
    private final PrecChunkRepository precChunkRepository;


    public void createChunks() {

        chunkRepository.deleteAllInBatch();

        List<Chunk> chunks = new ArrayList<>();

        List<LawArticle> articles = lawArticleRepository.findAll();

        for (LawArticle article : articles) {
            String title = "제" + article.getArticleNumber() + "조";

            if (article.getArticleTitle() != null && !article.getArticleTitle().isBlank()) {
                title += " " + article.getArticleTitle();
            }

            StringBuilder content = new StringBuilder();

            if (article.getArticleContent() != null && !article.getArticleContent().isBlank()) {
                content.append(article.getArticleContent()).append("\n");
            }

            for (LawParagraph paragraph : article.getParagraphs()) {

                if (paragraph.getContent() != null && !paragraph.getContent().isBlank()) {
                    content.append(paragraph.getContent()).append("\n");
                }

                for (LawSubparagraph subparagraph : paragraph.getSubparagraphs()) {

                    if (subparagraph.getContent() != null && !subparagraph.getContent().isBlank()) {
                        content.append(subparagraph.getContent()).append("\n");
                    }

                    for (LawItem item : subparagraph.getItemList()) {

                        if (item.getContent() != null && !item.getContent().isBlank()) {
                            content.append(item.getContent()).append("\n");
                        }
                    }
                }
            }

            if (content.isEmpty()) {
                continue;
            }

            Chunk chunk = Chunk.builder()
                    .law(article.getLaw())
                    .chunkType(LawChunkType.ARTICLE)
                    .sourceId(article.getId())
                    .title(title)
                    .content(content.toString().trim())
                    .build();

            chunks.add(chunk);
        }

        List<LawSupplement> supplements = lawSupplementRepository.findAll();

        for (LawSupplement supplement : supplements) {
            String title = "부칙";

            if (supplement.getProclamationNumber() != null && !supplement.getProclamationNumber().isBlank()) {
                title += " " + supplement.getProclamationNumber();
            }

            if (supplement.getContent() == null || supplement.getContent().isBlank()) {
                continue;
            }

            Chunk chunk = Chunk.builder()
                    .law(supplement.getLaw())
                    .chunkType(LawChunkType.SUPPLEMENT)
                    .sourceId(supplement.getId())
                    .title(title)
                    .content(supplement.getContent())
                    .build();

            chunks.add(chunk);
        }

        List<LawAmendment> amendments = lawAmendmentRepository.findAll();

        for (LawAmendment amendment : amendments) {
            if (amendment.getContent() == null || amendment.getContent().isBlank()) {
                continue;
            }

            Chunk chunk = Chunk.builder()
                    .law(amendment.getLaw())
                    .chunkType(LawChunkType.AMENDMENT)
                    .sourceId(amendment.getId())
                    .title("개정문")
                    .content(amendment.getContent())
                    .build();

            chunks.add(chunk);
        }

        chunkRepository.saveAll(chunks);
    }


    public ChunkResponse getChunks(
            Pageable pageable,
            List<String> keywords,
            String query,
            List<String> lawNames
    ) {
        Specification<Chunk> spec = (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> andPredicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                String[] queryTerms = query.trim().split("\\s+");

                List<Predicate> queryPredicates = new ArrayList<>();

                for (String term : queryTerms) {
                    if (term.isBlank()) {
                        continue;
                    }

                    String likeTerm = "%" + term.trim() + "%";

                    queryPredicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.like(root.get("title"), likeTerm),
                                    criteriaBuilder.like(root.get("content"), likeTerm)
                            )
                    );
                }

                if (!queryPredicates.isEmpty()) {
                    andPredicates.add(
                            criteriaBuilder.or(queryPredicates.toArray(new Predicate[0]))
                    );
                }
            }

            if (keywords != null && !keywords.isEmpty()) {
                List<Predicate> keywordPredicates = new ArrayList<>();

                for (String keyword : keywords) {
                    if (keyword == null || keyword.isBlank()) {
                        continue;
                    }

                    String likeKeyword = "%" + keyword.trim() + "%";

                    keywordPredicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.like(root.get("title"), likeKeyword),
                                    criteriaBuilder.like(root.get("content"), likeKeyword)
                            )
                    );
                }

                if (!keywordPredicates.isEmpty()) {
                    andPredicates.add(
                            criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0]))
                    );
                }
            }

            if (lawNames != null && !lawNames.isEmpty()) {
                Join<Chunk, Law> law = root.join("law");
                andPredicates.add(law.get("name").in(lawNames));
            }

            return criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));
        };

        Page<Chunk> chunks = chunkRepository.findAll(spec, pageable);

        List<ChunkResponseItem> list = chunks.getContent().stream()
                .map(ChunkResponseItem::new)
                .toList();

        return ChunkResponse.builder()
                .list(list)
                .currentPage(chunks.getNumber() + 1)
                .totalPages(chunks.getTotalPages())
                .totalElements(chunks.getTotalElements())
                .build();
    }

    public void createPrec() {

        precChunkRepository.deleteAllInBatch();

        List<PrecChunk> chunks = new ArrayList<>();

        List<Precedent> precedents = precedentRepository.findAll();

        for (Precedent precedent : precedents) {

            if (precedent.getIssue() != null && !precedent.getIssue().isBlank()) {
                PrecChunk chunk = PrecChunk.builder()
                        .precedent(precedent)
                        .chunkType(PrecChunkType.ISSUE)
                        .title("판시사항")
                        .content(precedent.getIssue())
                        .build();

                chunks.add(chunk);
            }

            if (precedent.getJudgmentSummary() != null && !precedent.getJudgmentSummary().isBlank()) {
                PrecChunk chunk = PrecChunk.builder()
                        .precedent(precedent)
                        .chunkType(PrecChunkType.SUMMARY)
                        .title("판결요지")
                        .content(precedent.getJudgmentSummary())
                        .build();

                chunks.add(chunk);
            }

            if (precedent.getReferenceArticles() != null && !precedent.getReferenceArticles().isBlank()) {
                PrecChunk chunk = PrecChunk.builder()
                        .precedent(precedent)
                        .chunkType(PrecChunkType.REFERENCE_ARTICLE)
                        .title("참조조문")
                        .content(precedent.getReferenceArticles())
                        .build();

                chunks.add(chunk);
            }

            if (precedent.getReferenceCases() != null && !precedent.getReferenceCases().isBlank()) {
                PrecChunk chunk = PrecChunk.builder()
                        .precedent(precedent)
                        .chunkType(PrecChunkType.REFERENCE_CASE)
                        .title("참조판례")
                        .content(precedent.getReferenceCases())
                        .build();

                chunks.add(chunk);
            }

            if (precedent.getFullText() != null && !precedent.getFullText().isBlank()) {
                PrecChunk chunk = PrecChunk.builder()
                        .precedent(precedent)
                        .chunkType(PrecChunkType.FULL_TEXT)
                        .title("판례내용")
                        .content(precedent.getFullText())
                        .build();

                chunks.add(chunk);
            }
        }

        precChunkRepository.saveAll(chunks);
    }

    public PrecResponse getPrecs(Pageable pageable) {
        Page<PrecChunk> precs = precChunkRepository.findAll(pageable);

        List<PrecChunkResponseItem> list = precs.getContent().stream()
                .map(PrecChunkResponseItem::new)
                .toList();

        return PrecResponse.builder()
                .list(list)
                .currentPage(precs.getNumber() + 1)
                .totalPages(precs.getTotalPages())
                .totalElements(precs.getTotalElements())
                .build();
    }
}
