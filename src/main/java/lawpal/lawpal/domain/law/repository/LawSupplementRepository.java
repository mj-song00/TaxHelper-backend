package lawpal.lawpal.domain.law.repository;

import lawpal.lawpal.domain.law.entity.LawSupplement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LawSupplementRepository  extends JpaRepository<LawSupplement, Long> {
    boolean existsBySupplementKey(String SupplementKey);
}
