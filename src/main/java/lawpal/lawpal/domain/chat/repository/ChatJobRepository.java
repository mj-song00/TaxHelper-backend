package lawpal.lawpal.domain.chat.repository;

import lawpal.lawpal.domain.chat.entity.ChatJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatJobRepository extends JpaRepository<ChatJob, Long> {
    Optional<ChatJob> findByJobId(UUID jobId);
}
