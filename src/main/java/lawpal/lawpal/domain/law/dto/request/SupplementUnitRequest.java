package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lawpal.lawpal.domain.law.entity.LawSupplement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SupplementUnitRequest {

    @JsonProperty("부칙키")
    private String key;

    @JsonProperty("부칙공포번호")
    private String proclamationNo;

    @JsonProperty("부칙공포일자")
    private String proclamationDate;

    @JsonProperty("부칙내용")
    private  List<List<String>> content;



    public LawSupplement toEntity(){
        return LawSupplement.builder()
                .proclamationNumber(proclamationNo)
                .proclamationDate(proclamationDate)
                .content(content != null ? content.toString() : null)
                .build();
    }
}