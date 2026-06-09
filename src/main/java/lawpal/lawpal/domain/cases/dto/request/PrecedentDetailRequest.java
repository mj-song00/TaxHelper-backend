package lawpal.lawpal.domain.cases.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrecedentDetailRequest {

    @JsonProperty("판시사항")
    private String 판시사항;

    @JsonProperty("참조판례")
    private String 참조판례;

    @JsonProperty("판결요지")
    private String 판결요지;

    @JsonProperty("판례내용")
    private String 판례내용;

    @JsonProperty("참조조문")
    private String 참조조문;

}
