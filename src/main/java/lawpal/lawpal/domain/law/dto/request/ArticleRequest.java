package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ArticleRequest {

    @JsonProperty("조문단위")
    private List<ArticleUnitRequest> units;
}