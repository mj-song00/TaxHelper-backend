package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LawTypeRequest {

    @JsonProperty("content")
    private String typeName;


    @JsonProperty("법종구분코드")
    private String typeCode;
}