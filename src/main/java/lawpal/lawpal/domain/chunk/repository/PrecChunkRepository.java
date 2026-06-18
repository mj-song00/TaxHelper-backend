package lawpal.lawpal.domain.chunk.repository;

import lawpal.lawpal.domain.chunk.entity.PrecChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrecChunkRepository extends JpaRepository<PrecChunk, Long>, JpaSpecificationExecutor<PrecChunk> {
}
