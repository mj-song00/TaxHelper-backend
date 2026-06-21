package lawpal.lawpal.domain.law.repository;

import lawpal.lawpal.domain.law.entity.LawAmendment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LawAmendmentRepository extends JpaRepository<LawAmendment, Long> {
    void deleteAllByLaw_Id(Long lawId);
}
