package lawpal.lawpal.domain.precedent.repository;

import lawpal.lawpal.domain.precedent.entity.Precedent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrecedentRepository extends JpaRepository<Precedent, Long> {
    boolean existsByPrecedentSerialNumber(String precedentSerialNumber);
}
