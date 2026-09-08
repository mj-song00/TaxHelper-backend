package lawpal.lawpal.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PreparedChatJobRequest(
        @NotBlank String context,
        @NotNull String sources
) {
}
