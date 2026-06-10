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

    public PrecChunkResponseItem(PrecChunk precChunk) {
        this.id = precChunk.getId();
        this.title = precChunk.getTitle();
        this.content = precChunk.getContent();
        this.chunkType = precChunk.getChunkType();
        this.precedentId = precChunk.getPrecedent().getId();
    }
}
