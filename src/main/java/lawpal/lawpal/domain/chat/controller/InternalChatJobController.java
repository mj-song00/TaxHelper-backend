package lawpal.lawpal.domain.chat.controller;

import jakarta.validation.Valid;
import lawpal.lawpal.domain.chat.dto.request.FailedChatJobRequest;
import lawpal.lawpal.domain.chat.dto.request.CompletedChatJobRequest;
import lawpal.lawpal.domain.chat.dto.request.PreparedChatJobRequest;
import lawpal.lawpal.domain.chat.dto.response.InternalChatJobResponse;
import lawpal.lawpal.domain.chat.service.ChatJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/chat/jobs")
public class InternalChatJobController {

    private final ChatJobService chatJobService;

    public InternalChatJobController(ChatJobService chatJobService) {
        this.chatJobService = chatJobService;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<InternalChatJobResponse> getJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(chatJobService.getInternalJob(jobId));
    }

    @PutMapping("/{jobId}/prepared")
    public ResponseEntity<Void> savePrepared(
            @PathVariable UUID jobId,
            @Valid @RequestBody PreparedChatJobRequest request
    ) {
        chatJobService.savePrepared(jobId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{jobId}/waiting")
    public ResponseEntity<Void> markWaiting(@PathVariable UUID jobId) {
        chatJobService.markWaiting(jobId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{jobId}/processing")
    public ResponseEntity<Void> markProcessing(@PathVariable UUID jobId) {
        chatJobService.markProcessing(jobId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{jobId}/completed")
    public ResponseEntity<Void> markCompleted(
            @PathVariable UUID jobId,
            @Valid @RequestBody CompletedChatJobRequest request
    ) {
        chatJobService.markCompleted(jobId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{jobId}/failed")
    public ResponseEntity<Void> markFailed(
            @PathVariable UUID jobId,
            @Valid @RequestBody FailedChatJobRequest request
    ) {
        chatJobService.markFailed(jobId, request.errorMessage());
        return ResponseEntity.noContent().build();
    }
}
