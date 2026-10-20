package lawpal.lawpal.domain.chat.repository;

import lawpal.lawpal.domain.chat.entity.ChatJob;
import lawpal.lawpal.domain.chat.enums.ChatJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ChatJobRepository extends JpaRepository<ChatJob, Long> {
    Optional<ChatJob> findByJobId(UUID jobId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            update ChatJob j set j.context = :context, j.sources = :sources, j.updatedAt = :now
            where j.jobId = :jobId and j.status = lawpal.lawpal.domain.chat.enums.ChatJobStatus.PREPARING
            """)
    int prepareIfPreparing(@Param("jobId") UUID jobId, @Param("context") String context,
                           @Param("sources") String sources, @Param("now") LocalDateTime now);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            update ChatJob j set j.status = lawpal.lawpal.domain.chat.enums.ChatJobStatus.WAITING,
                j.updatedAt = :now
            where j.jobId = :jobId and j.status = lawpal.lawpal.domain.chat.enums.ChatJobStatus.PREPARING
            """)
    int waitingIfPreparing(@Param("jobId") UUID jobId, @Param("now") LocalDateTime now);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            update ChatJob j set j.status = lawpal.lawpal.domain.chat.enums.ChatJobStatus.PROCESSING,
                j.processingStartedAt = :now, j.updatedAt = :now
            where j.jobId = :jobId and j.status = lawpal.lawpal.domain.chat.enums.ChatJobStatus.WAITING
            """)
    int processingIfWaiting(@Param("jobId") UUID jobId, @Param("now") LocalDateTime now);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            update ChatJob j set j.status = lawpal.lawpal.domain.chat.enums.ChatJobStatus.COMPLETED,
                j.answer = :answer, j.errorMessage = null, j.completedAt = :now, j.updatedAt = :now
            where j.jobId = :jobId and j.status = lawpal.lawpal.domain.chat.enums.ChatJobStatus.PROCESSING
            """)
    int completeIfProcessing(@Param("jobId") UUID jobId, @Param("answer") String answer,
                             @Param("now") LocalDateTime now);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            update ChatJob j set j.status = lawpal.lawpal.domain.chat.enums.ChatJobStatus.FAILED,
                j.errorMessage = :errorMessage, j.completedAt = :now, j.updatedAt = :now
            where j.jobId = :jobId and j.status in :expectedStatuses
                and j.status in (lawpal.lawpal.domain.chat.enums.ChatJobStatus.PREPARING,
                    lawpal.lawpal.domain.chat.enums.ChatJobStatus.WAITING,
                    lawpal.lawpal.domain.chat.enums.ChatJobStatus.PROCESSING)
            """)
    int failIfStatusIn(@Param("jobId") UUID jobId,
                       @Param("expectedStatuses") Collection<ChatJobStatus> expectedStatuses,
                       @Param("errorMessage") String errorMessage, @Param("now") LocalDateTime now);
}
