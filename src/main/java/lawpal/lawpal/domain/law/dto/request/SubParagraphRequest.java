package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lawpal.lawpal.domain.law.entity.LawSubparagraph;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubParagraphRequest {
    @JsonProperty("호번호")
    private String itemNo;

    @JsonProperty("호내용")
    private String content;

    @JsonProperty("호가지번호")
    private String branchNO;

    @JsonProperty("목")
    private List<LawItemRequest> subItem = new ArrayList<>();

    public LawSubparagraph toEntity() {
        return LawSubparagraph.builder()
                .subparagraphNumber(itemNo)
                .content(content)
                .itemList(subItem != null
                        ? subItem.stream()
                        .map(LawItemRequest::toEntity)
                        .toList()
                        : new ArrayList<>())
                .build();
    }
}
