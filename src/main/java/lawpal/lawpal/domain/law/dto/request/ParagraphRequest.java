package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lawpal.lawpal.domain.law.entity.LawParagraph;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParagraphRequest {

    @JsonProperty("항번호")
    private String paragraphNo;

    @JsonProperty("항제개정유형")
    private String revisionType;

    @JsonProperty("항제개정일자문자열")
    private String revisionTypeStr;

    @JsonProperty("항내용")
    private String content;

    @JsonProperty("호")
    private List<SubParagraphRequest> item = new ArrayList<>();


    public LawParagraph toEntity() {
        return LawParagraph.builder()
                .paragraphNumber(paragraphNo)
                .content(content)
                .build();
    }
}