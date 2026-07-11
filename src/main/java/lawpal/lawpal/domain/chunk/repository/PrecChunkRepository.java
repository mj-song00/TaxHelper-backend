package lawpal.lawpal.domain.chunk.repository;

import lawpal.lawpal.domain.chunk.entity.PrecChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrecChunkRepository extends JpaRepository<PrecChunk, Long>, JpaSpecificationExecutor<PrecChunk> {

    @Query(value = """
            SELECT pc.*
            FROM prec_chunks pc
            WHERE pc.embedding IS NOT NULL
            ORDER BY pc.embedding <-> CAST(:queryEmbedding AS vector)
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<PrecChunk> findNearestByEmbedding(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
            SELECT pc.*
            FROM prec_chunks pc
            JOIN precedent p ON p.id = pc.precedent_id
            JOIN cases c ON c.id = p.case_id
            WHERE pc.embedding IS NOT NULL
              AND c.court_name IN (:courtNames)
            ORDER BY pc.embedding <-> CAST(:queryEmbedding AS vector)
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<PrecChunk> findNearestByEmbeddingAndCourtNames(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("courtNames") List<String> courtNames,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
            SELECT pc.*
            FROM prec_chunks pc
            JOIN precedent p ON p.id = pc.precedent_id
            JOIN cases c ON c.id = p.case_id
            WHERE pc.embedding IS NOT NULL
              AND c.case_number IN (:caseNumbers)
            ORDER BY pc.embedding <-> CAST(:queryEmbedding AS vector)
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<PrecChunk> findNearestByEmbeddingAndCaseNumbers(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("caseNumbers") List<String> caseNumbers,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Modifying
    @Query(value = """
            UPDATE prec_chunks
            SET embedding = CAST(:embedding AS vector)
            WHERE id = :id
            """, nativeQuery = true)
    void updateEmbedding(
            @Param("id") Long id,
            @Param("embedding") String embedding
    );
}
