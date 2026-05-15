package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lawpal.lawpal.domain.law.LawReferenceDeserializer;
import lawpal.lawpal.domain.law.entity.LawArticle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleUnitRequest {

    @JsonProperty("조문번호")
    private String articleNo;

    @JsonProperty("조문제목")
    private String title;

    @JsonProperty("조문내용")
    private String content;

    @JsonProperty("조문시행일자")
    private String enforcementDate;

    @JsonProperty("조문변경여부")
    private String isChanged;

    @JsonProperty("조문이동이전")
    private String moveBefore;

    @JsonProperty("조문이동이후")
    private String moveAfter;

    @JsonProperty("조문참고자료")
    @JsonDeserialize(using = LawReferenceDeserializer.class)
    private List<List<String>> reference;

    @JsonProperty("조문여부")
    private String articleType;

    @JsonProperty("조문가지번호")
    private String branchNo;

    @JsonProperty("항")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<ParagraphRequest> paragraph =  new ArrayList<>();

    public LawArticle toEntity() {

        return LawArticle.builder()
                .articleNumber(articleNo)
                .articleTitle(title)
                .articleContent(content)
                .effectiveDate(enforcementDate)
                .changedYn(isChanged)
                .movedPrevious(moveBefore)
                .movedNext(moveAfter)
                .articleYn(articleType)
                .paragraphs(paragraph != null
                        ? paragraph.stream()
                        .map(ParagraphRequest::toEntity)
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }
}
