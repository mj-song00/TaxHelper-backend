package lawpal.lawpal.domain.chunk.repository;

import lawpal.lawpal.domain.chunk.entity.Chunk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    Page<Chunk> findAll(Pageable pageable);
}
