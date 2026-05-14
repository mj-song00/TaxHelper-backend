package lawpal.lawpal.domain.law.repository;

import lawpal.lawpal.domain.law.entity.LawParagraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LawParagraphRepository extends JpaRepository<LawParagraph, Long> {
}
