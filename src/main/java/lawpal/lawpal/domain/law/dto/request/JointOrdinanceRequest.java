package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lawpal.lawpal.domain.ministry.entity.LawJointMinistry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JointOrdinanceRequest {

    @JsonProperty("content")
    private String ministryName;

    @JsonProperty("소관부처코드")
    private String ministryCode;

    public LawJointMinistry toEntity() {
        return LawJointMinistry.builder()
                .ministryName(ministryName)
                .ministryCode(ministryCode)
                .build();
    }
}