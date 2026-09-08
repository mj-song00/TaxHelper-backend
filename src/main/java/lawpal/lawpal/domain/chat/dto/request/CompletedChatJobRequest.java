package lawpal.lawpal.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CompletedChatJobRequest(
        @NotBlank String answer
) {
}
