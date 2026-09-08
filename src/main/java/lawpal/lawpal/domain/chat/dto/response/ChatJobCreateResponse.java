package lawpal.lawpal.domain.chat.dto.response;

import lawpal.lawpal.domain.chat.enums.ChatJobStatus;

import java.util.UUID;

public record ChatJobCreateResponse(
        UUID jobId,
        ChatJobStatus status
) {
}
