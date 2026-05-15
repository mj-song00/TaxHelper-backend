package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lawpal.lawpal.domain.ministry.entity.Ministry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MinistryRequest {

    @JsonProperty("content")
    private String ministryName;

    @JsonProperty("소관부처코드")
    private String ministryCode;

    public Ministry toEntity() {
        return Ministry.builder()
                .ministryName(ministryName)
                .ministryCode(ministryCode)
                .build();
    }
}