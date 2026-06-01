package lawpal.lawpal.domain.chunk.repository;

import lawpal.lawpal.domain.chunk.entity.Chunk;
import lawpal.lawpal.domain.chunk.entity.LawChunkType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {
    boolean existsByChunkTypeAndSourceId(LawChunkType lawChunkType, Long id);
}
