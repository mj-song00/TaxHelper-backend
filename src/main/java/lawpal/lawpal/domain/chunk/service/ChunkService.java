package lawpal.lawpal.domain.chunk.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lawpal.lawpal.domain.chunk.dto.request.VectorChunkSearchRequest;
import lawpal.lawpal.domain.cases.entity.Case;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    private final OpenAiEmbeddingService openAiEmbeddingService;


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
        chunkRepository.flush();
        fillChunkEmbeddings(chunks);
    }



    private void fillChunkEmbeddings(List<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            String embedding = openAiEmbeddingService.embedAsVectorLiteral(
                    chunk.getTitle() + "\n" + chunk.getContent()
            );
            if (embedding != null) {
                chunkRepository.updateEmbedding(chunk.getId(), embedding);
            }
        }
    }

    private void fillPrecChunkEmbeddings(List<PrecChunk> chunks) {
        for (PrecChunk chunk : chunks) {
            String embedding = openAiEmbeddingService.embedAsVectorLiteral(
                    chunk.getTitle() + "\n" + chunk.getContent()
            );
            if (embedding != null) {
                precChunkRepository.updateEmbedding(chunk.getId(), embedding);
            }
        }
    }

    public ChunkResponse getChunks(
            Pageable pageable,
            List<String> keywords,
            String query,
            List<String> lawNames
    ) {
        List<String> searchTerms = buildSearchTerms(query, keywords);

        Specification<Chunk> spec = buildChunkSearchSpec(searchTerms, lawNames);

        if (searchTerms.isEmpty()) {
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

        List<Chunk> candidates = chunkRepository.findAll(spec);
        String searchText = buildSearchText(query, keywords);
        boolean supplementIntent = hasSupplementIntent(searchText);
        boolean amendmentIntent = hasAmendmentIntent(searchText);
        boolean housingLoanIntent = hasHousingLoanIntent(searchText, searchTerms);
        boolean badDebtAllowanceIntent = hasBadDebtAllowanceIntent(searchText, searchTerms);

        List<ScoredChunk> ranked = candidates.stream()
                .map(chunk -> new ScoredChunk(
                        chunk,
                        calculateRelevanceScore(
                                chunk,
                                searchTerms,
                                supplementIntent,
                                amendmentIntent,
                                housingLoanIntent,
                                badDebtAllowanceIntent
                        )
                ))
                .filter(item -> item.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .toList();

        int totalElements = ranked.size();
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber() + 1;
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        int fromIndex = Math.min(pageable.getPageNumber() * pageSize, totalElements);
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<ChunkResponseItem> list = ranked.subList(fromIndex, toIndex).stream()
                .map(item -> new ChunkResponseItem(item.chunk(), roundScore(item.score())))
                .toList();

        return ChunkResponse.builder()
                .list(list)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }


    public ChunkResponse getChunksByVectorSearch(
            Pageable pageable,
            VectorChunkSearchRequest request
    ) {
        String queryEmbedding = toVectorLiteral(request.getQueryEmbedding());
        List<String> lawNames = cleanFilterValues(request.getLawNames());
        int limit = pageable.getPageSize();
        int offset = Math.toIntExact(pageable.getOffset());

        List<Chunk> chunks = lawNames.isEmpty()
                ? chunkRepository.findNearestByEmbedding(queryEmbedding, limit, offset)
                : chunkRepository.findNearestByEmbeddingAndLawNames(queryEmbedding, lawNames, limit, offset);

        List<ChunkResponseItem> list = chunks.stream()
                .map(ChunkResponseItem::new)
                .toList();

        return ChunkResponse.builder()
                .list(list)
                .currentPage(pageable.getPageNumber() + 1)
                .totalPages(estimateVectorTotalPages(chunks.size(), limit, pageable.getPageNumber()))
                .totalElements((long) chunks.size())
                .build();
    }


    private String toVectorLiteral(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException("queryEmbedding is required for vector search.");
        }

        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < embedding.size(); index++) {
            Double value = embedding.get(index);
            if (value == null) {
                throw new IllegalArgumentException("queryEmbedding must not contain null values.");
            }
            if (index > 0) {
                builder.append(',');
            }
            builder.append(value);
        }
        return builder.append(']').toString();
    }

    private List<String> cleanFilterValues(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private int estimateVectorTotalPages(int currentResultSize, int pageSize, int pageNumber) {
        if (currentResultSize == 0 || pageSize <= 0) {
            return pageNumber == 0 ? 0 : pageNumber + 1;
        }

        return currentResultSize == pageSize ? pageNumber + 2 : pageNumber + 1;
    }

    private Specification<Chunk> buildChunkSearchSpec(
            List<String> searchTerms,
            List<String> lawNames
    ) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> andPredicates = new ArrayList<>();

            if (!searchTerms.isEmpty()) {
                List<Predicate> termPredicates = new ArrayList<>();

                for (String term : searchTerms) {
                    if (term == null || term.isBlank()) {
                        continue;
                    }

                    String likeTerm = "%" + term.trim() + "%";

                    termPredicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.like(root.get("title"), likeTerm),
                                    criteriaBuilder.like(root.get("content"), likeTerm)
                            )
                    );
                }

                if (!termPredicates.isEmpty()) {
                    andPredicates.add(
                            criteriaBuilder.or(termPredicates.toArray(new Predicate[0]))
                    );
                }
            }

            if (lawNames != null && !lawNames.isEmpty()) {
                Join<Chunk, Law> law = root.join("law");
                andPredicates.add(law.get("name").in(lawNames));
            }

            return criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));
        };
    }

    private List<String> buildSearchTerms(String query, List<String> keywords) {
        Set<String> terms = new LinkedHashSet<>();

        addTerms(terms, query);

        if (keywords != null) {
            for (String keyword : keywords) {
                addTerms(terms, keyword);
            }
        }

        expandSearchTerms(terms);

        return terms.stream()
                .filter(term -> term.length() >= 2)
                .toList();
    }

    private void addTerms(Set<String> terms, String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        for (String term : text.trim().split("\\s+")) {
            String normalized = normalizeTerm(term);

            if (!normalized.isBlank()) {
                terms.add(normalized);
            }
        }
    }

    private String normalizeTerm(String term) {
        return term == null
                ? ""
                : term.replaceAll("^[^0-9A-Za-z가-힣.%]+|[^0-9A-Za-z가-힣.%]+$", "").trim();
    }

    private void expandSearchTerms(Set<String> terms) {
        String joined = String.join(" ", terms);
        boolean housingLoanIntent = hasHousingLoanIntent(joined, new ArrayList<>(terms));
        boolean badDebtAllowanceIntent = hasBadDebtAllowanceIntent(joined, new ArrayList<>(terms));

        if (joined.contains("장기저당차입금")) {
            terms.add("장기주택저당차입금");
        }

        if (joined.contains("장기주택저당차입금")) {
            terms.add("장기저당차입금");
        }

        if (joined.contains("한도")) {
            terms.add("한도액");
            terms.add("공제한도");
        }

        if (joined.contains("이자상환액")) {
            terms.add("이자");
            terms.add("상환액");
        }

        if (housingLoanIntent) {
            terms.add("제52조");
            terms.add("제112조");
            terms.add("주택자금공제");
            terms.add("특별소득공제");
            terms.add("상환기간");
            terms.add("고정금리");
            terms.add("비거치식");
            terms.add("분할상환");
        }

        if (badDebtAllowanceIntent) {
            terms.add("제34조");
            terms.add("제19조의2");
            terms.add("제61조");
            terms.add("대손충당금");
            terms.add("대손금");
            terms.add("손금산입");
            terms.add("설정대상채권");
            terms.add("채권잔액");
            terms.add("채무보증");
            terms.add("구상채권");
            terms.add("특수관계인");
            terms.add("업무와 관련 없이");
            terms.add("업무무관");
            terms.add("가지급금");
            terms.add("제외");
        }
    }

    private String buildSearchText(String query, List<String> keywords) {
        StringBuilder builder = new StringBuilder();

        if (query != null) {
            builder.append(query).append(' ');
        }

        if (keywords != null) {
            keywords.forEach(keyword -> builder.append(keyword).append(' '));
        }

        return builder.toString();
    }

    private boolean hasSupplementIntent(String searchText) {
        return containsAny(searchText, "부칙", "시행일", "적용례", "경과조치", "개정규정", "시행 후", "시행 전");
    }

    private boolean hasAmendmentIntent(String searchText) {
        return containsAny(searchText, "개정문", "개정 내용", "개정이유", "신구", "일부개정", "전부개정");
    }

    private boolean hasHousingLoanIntent(String searchText, List<String> searchTerms) {
        String joinedTerms = String.join(" ", searchTerms);
        String target = searchText + " " + joinedTerms;

        return containsAny(
                target,
                "장기주택저당차입금",
                "장기저당차입금",
                "주택저당차입금",
                "주택담보대출",
                "주담대",
                "이자상환액",
                "주택자금공제"
        );
    }

    private boolean hasBadDebtAllowanceIntent(String searchText, List<String> searchTerms) {
        String joinedTerms = String.join(" ", searchTerms);
        String target = searchText + " " + joinedTerms;

        return containsAny(
                target,
                "대손충당금",
                "대손금",
                "대손",
                "구상채권",
                "가지급금",
                "채무보증",
                "특수관계인",
                "업무와 관련 없이",
                "업무무관",
                "설정대상채권"
        );
    }

    private boolean containsAny(String text, String... needles) {
        if (text == null || text.isBlank()) {
            return false;
        }

        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }

        return false;
    }

    private double calculateRelevanceScore(
            Chunk chunk,
            List<String> searchTerms,
            boolean supplementIntent,
            boolean amendmentIntent,
            boolean housingLoanIntent,
            boolean badDebtAllowanceIntent
    ) {
        String lawName = normalizeText(chunk.getLaw().getName());
        String title = normalizeText(chunk.getTitle());
        String content = normalizeText(chunk.getContent());
        String combined = title + " " + content;

        if (isDeletedChunk(title, content)) {
            return 0.0;
        }

        double score = 0.0;

        for (String term : searchTerms) {
            String normalizedTerm = normalizeText(term);

            if (normalizedTerm.isBlank()) {
                continue;
            }

            if (lawName.contains(normalizedTerm)) {
                score += 2.0;
            }

            if (title.contains(normalizedTerm)) {
                score += 8.0;
            }

            int contentHits = countOccurrences(content, normalizedTerm);

            if (contentHits > 0) {
                score += 2.0 + Math.min(contentHits - 1, 3) * 0.5;
            }
        }

        score += switch (chunk.getChunkType()) {
            case ARTICLE -> supplementIntent ? 5.0 : 25.0;
            case SUPPLEMENT -> supplementIntent ? 20.0 : -45.0;
            case AMENDMENT -> amendmentIntent ? 20.0 : -35.0;
        };

        if (title.startsWith("부칙") && !supplementIntent) {
            score -= 25.0;
        }

        if (badDebtAllowanceIntent) {
            boolean targetArticle = title.contains("제34조")
                    || title.contains("제61조")
                    || title.contains("제19조의2")
                    || content.contains("제19조의2");
            boolean directBadDebtTerm = containsAny(
                    combined,
                    "대손충당금",
                    "대손금",
                    "구상채권",
                    "가지급금",
                    "채무보증",
                    "특수관계인",
                    "업무와 관련 없이",
                    "업무무관",
                    "설정대상채권",
                    "채권잔액"
            );

            if (!targetArticle && !directBadDebtTerm) {
                return 0.0;
            }

            if (containsAny(combined, "외국납부세액", "외국법인세액", "국외원천소득")) {
                return 0.0;
            }

            if (targetArticle) {
                score += 70.0;
            }

            if (directBadDebtTerm) {
                score += 60.0;
            }

            if (containsAny(combined, "구상채권", "가지급금", "채무보증")) {
                score += 55.0;
            }

            if (containsAny(combined, "특수관계인", "업무와 관련 없이", "업무무관")) {
                score += 45.0;
            }

            if (containsAny(combined, "손금산입", "설정대상채권", "채권잔액", "제외")) {
                score += 25.0;
            }
        }

        if (housingLoanIntent) {
            boolean targetArticle = title.contains("제52조") || title.contains("제112조");
            boolean directHousingTerm = containsAny(
                    combined,
                    "장기주택저당차입금",
                    "장기저당차입금",
                    "주택저당차입금",
                    "이자상환액",
                    "주택자금공제"
            );

            if (!targetArticle && !directHousingTerm) {
                return 0.0;
            }

            if (targetArticle) {
                score += 60.0;
            }

            if (directHousingTerm) {
                score += 45.0;
            }

            if (containsAny(combined, "한도액", "공제한도", "한도")) {
                score += 30.0;
            }

            if (containsAny(combined, "만원", "천만원", "800", "600", "1,800", "1800", "2,000", "2000")) {
                score += 15.0;
            }

            if (containsAny(combined, "상환기간", "고정금리", "비거치식", "분할상환")) {
                score += 15.0;
            }

            if (chunk.getChunkType() == LawChunkType.SUPPLEMENT && !supplementIntent) {
                score -= 80.0;
            }
        }

        return score;
    }

    private boolean isDeletedChunk(String title, String content) {
        String compactTitle = title.replaceAll("\s+", "");
        String compactContent = content.replaceAll("\s+", "");

        return compactTitle.endsWith("삭제")
                || compactContent.equals("삭제")
                || compactContent.matches("제\\d+조.*삭제");
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private int countOccurrences(String text, String term) {
        if (text.isBlank() || term.isBlank()) {
            return 0;
        }

        int count = 0;
        int index = 0;

        while ((index = text.indexOf(term, index)) >= 0) {
            count++;
            index += term.length();
        }

        return count;
    }

    private double roundScore(double score) {
        return Math.round(score * 10000.0) / 10000.0;
    }

    private record ScoredChunk(Chunk chunk, double score) {
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
        precChunkRepository.flush();
        fillPrecChunkEmbeddings(chunks);
    }

    public PrecResponse getPrecs(
            Pageable pageable,
            List<String> keywords,
            String query,
            List<String> courtNames,
            List<String> caseNumbers
    ) {
        List<String> searchTerms = buildSearchTerms(query, keywords);

        Specification<PrecChunk> spec = buildPrecChunkSearchSpec(
                searchTerms,
                courtNames,
                caseNumbers
        );

        if (searchTerms.isEmpty() && isBlankList(courtNames) && isBlankList(caseNumbers)) {
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

        List<PrecChunk> candidates = precChunkRepository.findAll(spec);

        List<ScoredPrecChunk> ranked = candidates.stream()
                .map(chunk -> new ScoredPrecChunk(
                        chunk,
                        calculatePrecRelevanceScore(chunk, searchTerms, courtNames, caseNumbers)
                ))
                .filter(item -> item.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredPrecChunk::score).reversed())
                .toList();

        int totalElements = ranked.size();
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber() + 1;
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        int fromIndex = Math.min(pageable.getPageNumber() * pageSize, totalElements);
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<PrecChunkResponseItem> list = ranked.subList(fromIndex, toIndex).stream()
                .map(item -> new PrecChunkResponseItem(item.chunk(), roundScore(item.score())))
                .toList();

        return PrecResponse.builder()
                .list(list)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }


    public PrecResponse getPrecsByVectorSearch(
            Pageable pageable,
            VectorChunkSearchRequest request
    ) {
        String queryEmbedding = toVectorLiteral(request.getQueryEmbedding());
        List<String> courtNames = cleanFilterValues(request.getCourtNames());
        List<String> caseNumbers = cleanFilterValues(request.getCaseNumbers());
        int limit = pageable.getPageSize();
        int offset = Math.toIntExact(pageable.getOffset());

        List<PrecChunk> chunks;
        if (!caseNumbers.isEmpty()) {
            chunks = precChunkRepository.findNearestByEmbeddingAndCaseNumbers(queryEmbedding, caseNumbers, limit, offset);
        } else if (!courtNames.isEmpty()) {
            chunks = precChunkRepository.findNearestByEmbeddingAndCourtNames(queryEmbedding, courtNames, limit, offset);
        } else {
            chunks = precChunkRepository.findNearestByEmbedding(queryEmbedding, limit, offset);
        }

        List<PrecChunkResponseItem> list = chunks.stream()
                .map(PrecChunkResponseItem::new)
                .toList();

        return PrecResponse.builder()
                .list(list)
                .currentPage(pageable.getPageNumber() + 1)
                .totalPages(estimateVectorTotalPages(chunks.size(), limit, pageable.getPageNumber()))
                .totalElements((long) chunks.size())
                .build();
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

    private Specification<PrecChunk> buildPrecChunkSearchSpec(
            List<String> searchTerms,
            List<String> courtNames,
            List<String> caseNumbers
    ) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> andPredicates = new ArrayList<>();
            Join<PrecChunk, Precedent> precedent = root.join("precedent");
            Join<Precedent, Case> cases = precedent.join("cases");

            if (!searchTerms.isEmpty()) {
                List<Predicate> termPredicates = new ArrayList<>();

                for (String term : searchTerms) {
                    if (term == null || term.isBlank()) {
                        continue;
                    }

                    String likeTerm = "%" + term.trim().toLowerCase(Locale.ROOT) + "%";

                    termPredicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeTerm),
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), likeTerm),
                                    criteriaBuilder.like(criteriaBuilder.lower(precedent.get("issue")), likeTerm),
                                    criteriaBuilder.like(criteriaBuilder.lower(precedent.get("judgmentSummary")), likeTerm),
                                    criteriaBuilder.like(criteriaBuilder.lower(precedent.get("referenceArticles")), likeTerm),
                                    criteriaBuilder.like(criteriaBuilder.lower(precedent.get("referenceCases")), likeTerm),
                                    criteriaBuilder.like(criteriaBuilder.lower(cases.get("caseNumber")), likeTerm),
                                    criteriaBuilder.like(criteriaBuilder.lower(cases.get("caseName")), likeTerm),
                                    criteriaBuilder.like(criteriaBuilder.lower(cases.get("courtName")), likeTerm)
                            )
                    );
                }

                if (!termPredicates.isEmpty()) {
                    andPredicates.add(
                            criteriaBuilder.or(termPredicates.toArray(new Predicate[0]))
                    );
                }
            }

            if (!isBlankList(courtNames)) {
                List<Predicate> courtPredicates = new ArrayList<>();

                for (String courtName : courtNames) {
                    if (courtName == null || courtName.isBlank()) {
                        continue;
                    }

                    courtPredicates.add(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(cases.get("courtName")),
                                    "%" + courtName.trim().toLowerCase(Locale.ROOT) + "%"
                            )
                    );
                }

                if (!courtPredicates.isEmpty()) {
                    andPredicates.add(
                            criteriaBuilder.or(courtPredicates.toArray(new Predicate[0]))
                    );
                }
            }

            if (!isBlankList(caseNumbers)) {
                List<Predicate> caseNumberPredicates = new ArrayList<>();

                for (String caseNumber : caseNumbers) {
                    if (caseNumber == null || caseNumber.isBlank()) {
                        continue;
                    }

                    caseNumberPredicates.add(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(cases.get("caseNumber")),
                                    "%" + caseNumber.trim().toLowerCase(Locale.ROOT) + "%"
                            )
                    );
                }

                if (!caseNumberPredicates.isEmpty()) {
                    andPredicates.add(
                            criteriaBuilder.or(caseNumberPredicates.toArray(new Predicate[0]))
                    );
                }
            }

            return criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));
        };
    }

    private double calculatePrecRelevanceScore(
            PrecChunk chunk,
            List<String> searchTerms,
            List<String> courtNames,
            List<String> caseNumbers
    ) {
        Case cases = chunk.getPrecedent().getCases();
        String title = normalizeText(chunk.getTitle());
        String content = normalizeText(chunk.getContent());
        String caseName = normalizeText(cases.getCaseName());
        String caseNumber = normalizeText(cases.getCaseNumber());
        String courtName = normalizeText(cases.getCourtName());

        double score = 0.0;

        for (String term : searchTerms) {
            String normalizedTerm = normalizeText(term);

            if (normalizedTerm.isBlank()) {
                continue;
            }

            if (title.contains(normalizedTerm)) {
                score += 8.0;
            }

            if (caseName.contains(normalizedTerm)) {
                score += 6.0;
            }

            if (caseNumber.contains(normalizedTerm)) {
                score += 10.0;
            }

            if (courtName.contains(normalizedTerm)) {
                score += 4.0;
            }

            int contentHits = countOccurrences(content, normalizedTerm);

            if (contentHits > 0) {
                score += 2.0 + Math.min(contentHits - 1, 4) * 0.5;
            }
        }

        for (String court : safeList(courtNames)) {
            if (court == null || court.isBlank()) {
                continue;
            }

            if (courtName.contains(normalizeText(court))) {
                score += 10.0;
            }
        }

        for (String number : safeList(caseNumbers)) {
            if (number == null || number.isBlank()) {
                continue;
            }

            if (caseNumber.contains(normalizeText(number))) {
                score += 15.0;
            }
        }

        score += switch (chunk.getChunkType()) {
            case ISSUE -> 12.0;
            case SUMMARY -> 10.0;
            case FULL_TEXT -> 6.0;
            case REFERENCE_ARTICLE, REFERENCE_CASE -> 3.0;
        };

        return score;
    }

    private boolean isBlankList(List<String> values) {
        return values == null || values.stream().allMatch(value -> value == null || value.isBlank());
    }

    private List<String> safeList(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values;
    }

    private record ScoredPrecChunk(PrecChunk chunk, double score) {
    }
}
