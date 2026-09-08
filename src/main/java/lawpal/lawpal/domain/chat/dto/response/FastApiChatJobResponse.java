package lawpal.lawpal.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lawpal.lawpal.domain.chat.enums.ChatJobStatus;

import java.util.UUID;

public record FastApiChatJobResponse(
        @JsonProperty("job_id") UUID jobId,
        ChatJobStatus status
) {
}
