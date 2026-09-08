package lawpal.lawpal.domain.chat.dto.response;

import lawpal.lawpal.domain.chat.entity.ChatJob;
import lawpal.lawpal.domain.chat.enums.ChatJobStatus;

import java.util.UUID;

public record InternalChatJobResponse(
        UUID jobId,
        ChatJobStatus status,
        String question,
        String context
) {
    public static InternalChatJobResponse from(ChatJob chatJob) {
        return new InternalChatJobResponse(
                chatJob.getJobId(),
                chatJob.getStatus(),
                chatJob.getQuestion(),
                chatJob.getContext()
        );
    }
}
