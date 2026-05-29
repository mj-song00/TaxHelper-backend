package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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
    private JsonNode content;

    @JsonProperty("호")
    private List<SubParagraphRequest> item = new ArrayList<>();

    public String getContent() {
        if (content == null || content.isNull()) {
            return null;
        }

        if (content.isTextual()) {
            return content.asText();
        }

        if (content.isArray()) {
            List<String> result = new ArrayList<>();

            content.forEach(node -> {
                if (node.isArray()) {
                    node.forEach(child -> result.add(child.asText()));
                } else {
                    result.add(node.asText());
                }
            });

            return String.join("\n", result);
        }

        return content.asText();
    }

}