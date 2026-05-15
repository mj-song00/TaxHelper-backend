package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lawpal.lawpal.domain.law.StringOrArrayDeserializer;
import lawpal.lawpal.domain.law.entity.LawItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LawItemRequest {
    @JsonProperty("목번호")
    private String subNo;

    @JsonProperty("목내용")
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private List<String> subContent;

    public LawItem toEntity() {
        return LawItem.builder()
                .itemNumber(subNo)
                .content(subContent != null
                        ? String.join("\n", subContent)   // 엔티티가 String이면 join
                        : null)
                .build();
    }
}
