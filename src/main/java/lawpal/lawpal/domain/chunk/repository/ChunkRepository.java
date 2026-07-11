package lawpal.lawpal.domain.chunk.repository;

import lawpal.lawpal.domain.chunk.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChunkRepository extends JpaRepository<Chunk, Long>, JpaSpecificationExecutor<Chunk> {

    @Query(value = """
            SELECT c.*
            FROM chunks c
            WHERE c.embedding IS NOT NULL
            ORDER BY c.embedding <-> CAST(:queryEmbedding AS vector)
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Chunk> findNearestByEmbedding(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
            SELECT c.*
            FROM chunks c
            JOIN laws l ON l.id = c.law_id
            WHERE c.embedding IS NOT NULL
              AND l.name IN (:lawNames)
            ORDER BY c.embedding <-> CAST(:queryEmbedding AS vector)
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Chunk> findNearestByEmbeddingAndLawNames(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("lawNames") List<String> lawNames,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Modifying
    @Query(value = """
            UPDATE chunks
            SET embedding = CAST(:embedding AS vector)
            WHERE id = :id
            """, nativeQuery = true)
    void updateEmbedding(
            @Param("id") Long id,
            @Param("embedding") String embedding
    );
}
