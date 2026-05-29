package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentUnitRequest {

    @JsonProperty("부서키")
    private String departmentKey;

    @JsonProperty("부서명")
    private String departmentName;

    @JsonProperty("부서연락처")
    private String phoneNumber;
}