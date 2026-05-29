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
public class SubParagraphRequest {
    @JsonProperty("호번호")
    private String itemNo;

    @JsonProperty("호내용")
    private JsonNode content;

    @JsonProperty("호가지번호")
    private String branchNO;

    @JsonProperty("목")
    private List<LawItemRequest> subItem = new ArrayList<>();

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
