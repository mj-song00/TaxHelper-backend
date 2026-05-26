package lawpal.lawpal.domain.law.repository;

import lawpal.lawpal.domain.law.entity.Law;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LawRepository extends JpaRepository<Law, Long> {
    boolean existsByLawSerialNumber(String mst);
}
