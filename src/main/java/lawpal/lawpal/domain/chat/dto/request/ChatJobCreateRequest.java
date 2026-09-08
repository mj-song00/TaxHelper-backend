package lawpal.lawpal.domain.chat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ChatJobCreateRequest(
        @NotBlank String question,
        @Min(1) @Max(20) Integer topK
) {
}
