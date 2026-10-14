package lawpal.lawpal.domain.law;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NestedTextListDeserializer extends JsonDeserializer<List<List<String>>> {
    @Override
    public List<List<String>> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        List<List<String>> result = new ArrayList<>();
        if (node.isTextual()) {
            result.add(List.of(node.textValue()));
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                if (child.isTextual()) {
                    result.add(List.of(child.textValue()));
                } else if (child.isArray()) {
                    List<String> lines = new ArrayList<>();
                    for (JsonNode line : child) {
                        if (!line.isTextual()) {
                            return context.reportInputMismatch(List.class,
                                    "Expected text inside a law content array, got %s", line.getNodeType());
                        }
                        lines.add(line.textValue());
                    }
                    result.add(lines);
                } else {
                    return context.reportInputMismatch(List.class,
                            "Expected text or a text array, got %s", child.getNodeType());
                }
            }
        } else {
            return context.reportInputMismatch(List.class,
                    "Expected text or a text array, got %s", node.getNodeType());
        }
        return result;
    }
}
