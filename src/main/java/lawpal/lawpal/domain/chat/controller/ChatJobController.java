package lawpal.lawpal.domain.chat.controller;

import jakarta.validation.Valid;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.domain.chat.dto.request.ChatJobCreateRequest;
import lawpal.lawpal.domain.chat.dto.response.ChatJobCreateResponse;
import lawpal.lawpal.domain.chat.dto.response.ChatJobPollResponse;
import lawpal.lawpal.domain.chat.service.ChatJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static lawpal.lawpal.common.exception.ExceptionEnum.INVALID_INPUT_VALUE;

import java.util.UUID;

@RestController
@RequestMapping("/api/ui/chat/jobs")
public class ChatJobController {

    private final ChatJobService chatJobService;

    public ChatJobController(ChatJobService chatJobService) {
        this.chatJobService = chatJobService;
    }

    @PostMapping
    public ResponseEntity<ChatJobCreateResponse> create(
            @Valid @RequestBody ChatJobCreateRequest request
    ) {
        return ResponseEntity.status(ACCEPTED).body(chatJobService.create(request));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ChatJobPollResponse> getJob(@PathVariable String jobId) {
        try {
            return ResponseEntity.ok(chatJobService.getPublicJob(UUID.fromString(jobId)));
        } catch (IllegalArgumentException exception) {
            throw new BaseException(INVALID_INPUT_VALUE);
        }
    }
}
