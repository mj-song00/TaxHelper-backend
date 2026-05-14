package lawpal.lawpal.domain.law.repository;

import lawpal.lawpal.domain.law.entity.LawArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LawArticleRepository  extends JpaRepository<LawArticle, Long> {
    boolean existsByArticleKey(String articleKey);
}
