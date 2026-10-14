package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lawpal.lawpal.domain.law.NestedTextListDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmendmentsRequest {
    @JsonProperty("개정문내용")
    @JsonDeserialize(using = NestedTextListDeserializer.class)
    private List<List<String>> contents;
}
