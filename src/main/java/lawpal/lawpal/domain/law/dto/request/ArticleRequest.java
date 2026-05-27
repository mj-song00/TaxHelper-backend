package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ArticleRequest {

    @JsonProperty("조문단위")
    private List<ArticleUnitRequest> units;

//    public List<LawArticle> toEntityList() {
//        if (units == null) return Collections.emptyList();
//        return units.stream()
//                .map(ArticleUnitRequest::toEntity)
//                .collect(Collectors.toList());
//    }
}