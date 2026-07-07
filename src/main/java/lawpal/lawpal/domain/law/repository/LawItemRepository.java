package lawpal.lawpal.domain.law.repository;

import lawpal.lawpal.domain.law.entity.LawItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LawItemRepository extends JpaRepository<LawItem, Long> {
}
