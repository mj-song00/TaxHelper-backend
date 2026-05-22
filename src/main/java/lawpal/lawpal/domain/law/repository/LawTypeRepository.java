package lawpal.lawpal.domain.law.repository;

import lawpal.lawpal.domain.law.entity.LawType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LawTypeRepository extends JpaRepository<LawType, Long> {
    Optional<LawType> findByTypeName(String lawType);
}
