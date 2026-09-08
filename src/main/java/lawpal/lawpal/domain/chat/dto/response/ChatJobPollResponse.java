package lawpal.lawpal.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lawpal.lawpal.domain.chat.enums.ChatJobStatus;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatJobPollResponse(
        UUID jobId,
        ChatJobStatus status,
        String question,
        String answer,
        JsonNode sources,
        String message
) {
}
