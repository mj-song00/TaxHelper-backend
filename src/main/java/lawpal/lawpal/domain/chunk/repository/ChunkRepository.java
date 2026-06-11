package lawpal.lawpal.domain.chunk.repository;

import lawpal.lawpal.domain.chunk.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChunkRepository extends JpaRepository<Chunk, Long>, JpaSpecificationExecutor<Chunk> {

}