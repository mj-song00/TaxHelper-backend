package lawpal.lawpal.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record FastApiChatJobRequest(
        @JsonProperty("job_id") UUID jobId,
        String question,
        @JsonProperty("top_k") Integer topK
) {
}
