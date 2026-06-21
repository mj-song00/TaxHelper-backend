package lawpal.lawpal.domain.law.repository;

import lawpal.lawpal.domain.law.entity.Law;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LawRepository extends JpaRepository<Law, Long> {
    boolean existsByLawSerialNumber(String mst);
    List<Law> findAllByName(String name);
}
