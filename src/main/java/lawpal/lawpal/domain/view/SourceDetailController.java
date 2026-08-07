package lawpal.lawpal.domain.view;

import lawpal.lawpal.domain.cases.entity.Case;
import lawpal.lawpal.domain.chunk.entity.Chunk;
import lawpal.lawpal.domain.chunk.entity.PrecChunk;
import lawpal.lawpal.domain.chunk.repository.ChunkRepository;
import lawpal.lawpal.domain.chunk.repository.PrecChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ui/sources")
public class SourceDetailController {

    private final ChunkRepository chunkRepository;
    private final PrecChunkRepository precChunkRepository;

    @GetMapping("/{sourceType}/{chunkId}")
    @Transactional(readOnly = true)
    public SourceDetailResponse getSource(
            @PathVariable String sourceType,
            @PathVariable Long chunkId
    ) {
        return switch (sourceType) {
            case "law" -> getLawChunk(chunkId);
            case "precedent" -> getPrecedentChunk(chunkId);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "지원하지 않는 근거 유형입니다."
            );
        };
    }

    private SourceDetailResponse getLawChunk(Long chunkId) {
        Chunk chunk = chunkRepository.findById(chunkId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "법령 근거를 찾을 수 없습니다."
                ));

        return new SourceDetailResponse(
                "law",
                chunk.getId(),
                chunk.getLaw().getName(),
                chunk.getTitle(),
                chunk.getContent(),
                null,
                null,
                null
        );
    }

    private SourceDetailResponse getPrecedentChunk(Long chunkId) {
        PrecChunk chunk = precChunkRepository.findById(chunkId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "판례 근거를 찾을 수 없습니다."
                ));
        Case caseInfo = chunk.getPrecedent().getCases();

        return new SourceDetailResponse(
                "precedent",
                chunk.getId(),
                caseInfo.getCourtName(),
                chunk.getTitle(),
                chunk.getContent(),
                caseInfo.getCaseNumber(),
                caseInfo.getCaseName(),
                caseInfo.getSentencingDate()
        );
    }

    public record SourceDetailResponse(
            String sourceType,
            Long chunkId,
            String sourceName,
            String title,
            String content,
            String caseNumber,
            String caseName,
            String sentencingDate
    ) {
    }
}
