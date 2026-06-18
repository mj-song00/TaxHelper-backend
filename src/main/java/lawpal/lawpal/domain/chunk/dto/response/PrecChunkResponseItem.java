package lawpal.lawpal.domain.chunk.dto.response;

import lawpal.lawpal.domain.chunk.entity.PrecChunk;
import lawpal.lawpal.domain.chunk.enums.PrecChunkType;
import lombok.Getter;

@Getter
public class PrecChunkResponseItem {

    private final Long id;
    private final String title;
    private final String content;
    private final PrecChunkType chunkType;
    private final Long precedentId;
    private final String caseNumber;
    private final String caseName;
    private final String courtName;
    private final String sentencingDate;
    private final Double score;

    public PrecChunkResponseItem(PrecChunk precChunk) {
        this(precChunk, null);
    }

    public PrecChunkResponseItem(PrecChunk precChunk, Double score) {
        this.id = precChunk.getId();
        this.title = precChunk.getTitle();
        this.content = precChunk.getContent();
        this.chunkType = precChunk.getChunkType();
        this.precedentId = precChunk.getPrecedent().getId();
        this.caseNumber = precChunk.getPrecedent().getCases().getCaseNumber();
        this.caseName = precChunk.getPrecedent().getCases().getCaseName();
        this.courtName = precChunk.getPrecedent().getCases().getCourtName();
        this.sentencingDate = precChunk.getPrecedent().getCases().getSentencingDate();
        this.score = score;
    }
}
