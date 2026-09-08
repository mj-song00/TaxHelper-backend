package lawpal.lawpal.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FailedChatJobRequest(
        @NotBlank String errorMessage
) {
}
