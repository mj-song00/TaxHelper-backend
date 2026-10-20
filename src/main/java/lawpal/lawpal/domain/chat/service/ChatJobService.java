package lawpal.lawpal.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.domain.chat.dto.request.ChatJobCreateRequest;
import lawpal.lawpal.domain.chat.dto.request.CompletedChatJobRequest;
import lawpal.lawpal.domain.chat.dto.request.FastApiChatJobRequest;
import lawpal.lawpal.domain.chat.dto.request.PreparedChatJobRequest;
import lawpal.lawpal.domain.chat.dto.response.ChatJobCreateResponse;
import lawpal.lawpal.domain.chat.dto.response.ChatJobPollResponse;
import lawpal.lawpal.domain.chat.dto.response.FastApiChatJobResponse;
import lawpal.lawpal.domain.chat.dto.response.InternalChatJobResponse;
import lawpal.lawpal.domain.chat.entity.ChatJob;
import lawpal.lawpal.domain.chat.enums.ChatJobStatus;
import lawpal.lawpal.domain.chat.repository.ChatJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static lawpal.lawpal.common.exception.ExceptionEnum.CHAT_JOB_NOT_FOUND;
import static lawpal.lawpal.common.exception.ExceptionEnum.INVALID_CHAT_JOB_STATUS;

@Slf4j
@Service
public class ChatJobService {

    private static final String REGISTRATION_ERROR_MESSAGE = "AI 작업 등록에 실패했습니다.";

    private final ChatJobRepository chatJobRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String aiBaseUrl;

    public ChatJobService(
            ChatJobRepository chatJobRepository,
            ObjectMapper objectMapper,
            @Value("${taxhelper.ai.base-url:http://127.0.0.1:8000}") String aiBaseUrl
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(120_000);
        this.chatJobRepository = chatJobRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
        this.aiBaseUrl = aiBaseUrl;
    }

    public ChatJobCreateResponse create(ChatJobCreateRequest request) {
        UUID jobId = UUID.randomUUID();
        int topK = request.topK() == null ? 5 : request.topK();
        ChatJob chatJob = ChatJob.builder()
                .jobId(jobId)
                .question(request.question().trim())
                .topK(topK)
                .status(ChatJobStatus.PREPARING)
                .build();
        chatJobRepository.saveAndFlush(chatJob);
        log.info("[CHAT_JOB] job_id={} status=PREPARING", jobId);

        try {
            FastApiChatJobResponse response = restClient.post()
                    .uri(aiBaseUrl + "/api/v1/chat/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new FastApiChatJobRequest(jobId, chatJob.getQuestion(), topK))
                    .retrieve()
                    .body(FastApiChatJobResponse.class);

            if (response == null
                    || !jobId.equals(response.jobId())
                    || response.status() != ChatJobStatus.WAITING) {
                throw new IllegalStateException("Unexpected FastAPI chat job response");
            }
            return new ChatJobCreateResponse(jobId, response.status());
        } catch (RestClientException | IllegalStateException exception) {
            int changed = chatJobRepository.failIfStatusIn(jobId, Set.of(ChatJobStatus.PREPARING),
                    REGISTRATION_ERROR_MESSAGE, LocalDateTime.now());
            log.error("[CHAT_JOB] job_id={} phase=registration_failure changed={}", jobId, changed, exception);
            throw new ResponseStatusException(BAD_GATEWAY, REGISTRATION_ERROR_MESSAGE, exception);
        }
    }

    @Transactional
    public void savePrepared(UUID jobId, PreparedChatJobRequest request) {
        if (chatJobRepository.prepareIfPreparing(jobId, request.context(), request.sources(),
                LocalDateTime.now()) == 0) {
            ChatJobStatus status = getByJobId(jobId).getStatus();
            if (status != ChatJobStatus.COMPLETED && status != ChatJobStatus.FAILED) {
                throw new BaseException(INVALID_CHAT_JOB_STATUS);
            }
            return;
        }
        log.info("[CHAT_JOB] job_id={} phase=prepared", jobId);
    }

    @Transactional
    public void markWaiting(UUID jobId) {
        if (chatJobRepository.waitingIfPreparing(jobId, LocalDateTime.now()) == 0) {
            getByJobId(jobId); // Preserve 404; already advanced jobs are unchanged.
            return;
        }
        log.info("[CHAT_JOB] job_id={} status=WAITING", jobId);
    }

    @Transactional
    public void markProcessing(UUID jobId) {
        if (chatJobRepository.processingIfWaiting(jobId, LocalDateTime.now()) == 0) {
            getByJobId(jobId);
            throw new BaseException(INVALID_CHAT_JOB_STATUS);
        }
        log.info("[CHAT_JOB] job_id={} status=PROCESSING", jobId);
    }

    @Transactional
    public void markCompleted(UUID jobId, CompletedChatJobRequest request) {
        if (chatJobRepository.completeIfProcessing(jobId, request.answer(), LocalDateTime.now()) == 0) {
            ChatJobStatus status = getByJobId(jobId).getStatus();
            if (status != ChatJobStatus.COMPLETED && status != ChatJobStatus.FAILED) {
                throw new BaseException(INVALID_CHAT_JOB_STATUS);
            }
            return;
        }
        log.info("[CHAT_JOB] job_id={} status=COMPLETED", jobId);
    }

    @Transactional
    public void markFailed(UUID jobId, String errorMessage) {
        if (chatJobRepository.failIfStatusIn(jobId, Set.of(ChatJobStatus.PREPARING,
                ChatJobStatus.WAITING, ChatJobStatus.PROCESSING), errorMessage, LocalDateTime.now()) == 0) {
            getByJobId(jobId); // A terminal job keeps its original result and timestamps.
            return;
        }
        log.warn("[CHAT_JOB] job_id={} status=FAILED", jobId);
    }

    @Transactional(readOnly = true)
    public ChatJob getByJobId(UUID jobId) {
        return chatJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new BaseException(CHAT_JOB_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public InternalChatJobResponse getInternalJob(UUID jobId) {
        return InternalChatJobResponse.from(getByJobId(jobId));
    }

    @Transactional(readOnly = true)
    public ChatJobPollResponse getPublicJob(UUID jobId) {
        ChatJob chatJob = getByJobId(jobId);
        ChatJobPollResponse response;

        if (chatJob.getStatus() == ChatJobStatus.COMPLETED) {
            JsonNode sources;
            try {
                sources = objectMapper.readTree(chatJob.getSources());
            } catch (JsonProcessingException exception) {
                throw new IllegalStateException("Stored ChatJob sources are invalid", exception);
            }
            if (!sources.isArray()) {
                throw new IllegalStateException("Stored ChatJob sources must be an array");
            }
            response = new ChatJobPollResponse(
                    chatJob.getJobId(),
                    chatJob.getStatus(),
                    chatJob.getQuestion(),
                    chatJob.getAnswer(),
                    sources,
                    null
            );
        } else if (chatJob.getStatus() == ChatJobStatus.FAILED) {
            String message = chatJob.getErrorMessage();
            if (message == null || message.isBlank()) {
                message = "답변 생성에 실패했습니다.";
            }
            response = new ChatJobPollResponse(
                    chatJob.getJobId(),
                    chatJob.getStatus(),
                    null,
                    null,
                    null,
                    message
            );
        } else {
            response = new ChatJobPollResponse(
                    chatJob.getJobId(),
                    chatJob.getStatus(),
                    null,
                    null,
                    null,
                    null
            );
        }

        log.info("[CHAT_JOB] job_id={} phase=polled status={}", jobId, chatJob.getStatus());
        return response;
    }
}
