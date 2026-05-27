package lawpal.lawpal.domain.ministry.repository;

import lawpal.lawpal.domain.ministry.entity.Ministry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MinistryRepository extends JpaRepository<Ministry, Long> {
    Optional<Ministry> findFirstByMinistryCode(String ministryCode);
}
