package lawpal.lawpal.domain.chunk.service;

import lawpal.lawpal.domain.chunk.entity.Chunk;
import lawpal.lawpal.domain.chunk.entity.LawChunkType;
import lawpal.lawpal.domain.chunk.repository.ChunkRepository;
import lawpal.lawpal.domain.law.entity.LawAmendment;
import lawpal.lawpal.domain.law.entity.LawArticle;
import lawpal.lawpal.domain.law.entity.LawSupplement;
import lawpal.lawpal.domain.law.repository.LawAmendmentRepository;
import lawpal.lawpal.domain.law.repository.LawArticleRepository;
import lawpal.lawpal.domain.law.repository.LawSupplementRepository;
import lombok.RequiredArgsConstructor;
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

    public void createChunks() {

        chunkRepository.deleteAllInBatch();

        List<Chunk> chunks = new ArrayList<>();

        List<LawArticle> articles = lawArticleRepository.findAll();

        for (LawArticle article : articles) {
            String title = "제" + article.getArticleNumber() + "조";

            if (article.getArticleTitle() != null && !article.getArticleTitle().isBlank()) {
                title += " " + article.getArticleTitle();
            }

            Chunk chunk = Chunk.builder()
                    .law(article.getLaw())
                    .chunkType(LawChunkType.ARTICLE)
                    .sourceId(article.getId())
                    .title(title)
                    .content(article.getArticleContent())
                    .build();

            chunks.add(chunk);
        }

        List<LawSupplement> supplements = lawSupplementRepository.findAll();

        for (LawSupplement supplement : supplements) {
            String title = "부칙";

            if (supplement.getProclamationNumber() != null && !supplement.getProclamationNumber().isBlank()) {
                title += " " + supplement.getProclamationNumber();
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
}
