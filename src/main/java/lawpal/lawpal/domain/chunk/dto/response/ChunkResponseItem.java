package lawpal.lawpal.domain.chunk.dto.response;

import lawpal.lawpal.domain.chunk.entity.Chunk;
import lawpal.lawpal.domain.chunk.enums.LawChunkType;
import lombok.Getter;

@Getter
public class ChunkResponseItem {

    private final Long id;
    private final String lawName;
    private final LawChunkType chunkType;
    private final String title;
    private final String content;
    private final Long sourceId;
    private final Double score;

    public ChunkResponseItem(Chunk chunk){
        this(chunk, null);
    }

    public ChunkResponseItem(Chunk chunk, Double score){
        this.id= chunk.getId();
        this.lawName= chunk.getLaw().getName();
        this.chunkType= chunk.getChunkType();
        this.title = chunk.getTitle();
        this.content= chunk.getContent();
        this.sourceId = chunk.getSourceId();
        this.score = score;
    }
}
