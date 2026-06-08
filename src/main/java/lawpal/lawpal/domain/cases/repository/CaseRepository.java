package lawpal.lawpal.domain.cases.repository;

import lawpal.lawpal.domain.cases.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseRepository extends JpaRepository<Case, Long> {
}
