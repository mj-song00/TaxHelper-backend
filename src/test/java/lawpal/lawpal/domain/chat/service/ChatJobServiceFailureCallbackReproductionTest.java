package lawpal.lawpal.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.domain.chat.dto.request.ChatJobCreateRequest;
import lawpal.lawpal.domain.chat.dto.request.CompletedChatJobRequest;
import lawpal.lawpal.domain.chat.dto.request.PreparedChatJobRequest;
import lawpal.lawpal.domain.chat.entity.ChatJob;
import lawpal.lawpal.domain.chat.enums.ChatJobStatus;
import lawpal.lawpal.domain.chat.repository.ChatJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static lawpal.lawpal.common.exception.ExceptionEnum.*;

@ExtendWith(MockitoExtension.class)
class ChatJobServiceFailureCallbackReproductionTest {
    @Mock private ChatJobRepository repository;

    @ParameterizedTest
    @EnumSource(value = ChatJobStatus.class, names = {"COMPLETED", "FAILED"})
    void lateFailurePreservesTerminalResult(ChatJobStatus status) {
        UUID id = UUID.randomUUID();
        ChatJob job = ChatJob.builder().jobId(id).question("question").topK(5)
                .status(status).context("context").sources("[]").answer("first answer")
                .errorMessage(status == ChatJobStatus.FAILED ? "first error" : null)
                .processingStartedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .completedAt(LocalDateTime.of(2026, 1, 1, 0, 1)).build();
        when(repository.findByJobId(id)).thenReturn(Optional.of(job));
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        var before = mapper.valueToTree(job);
        ChatJobService service = new ChatJobService(repository, mapper, "http://unused.test");
        for (int i = 0; i < 2; i++) {
            service.markFailed(id, "late error");
            service.markCompleted(id, new CompletedChatJobRequest("late answer"));
            service.savePrepared(id, new PreparedChatJobRequest("late context", "[]"));
            service.markWaiting(id);
        }
        assertEquals(before, mapper.valueToTree(job));
        verify(repository, times(2)).failIfStatusIn(eq(id),
                eq(Set.of(ChatJobStatus.PREPARING, ChatJobStatus.WAITING, ChatJobStatus.PROCESSING)),
                eq("late error"), any(LocalDateTime.class));
        verify(repository, never()).save(any());
    }

    @Test
    void processingJobTransitionsToFailed() {
        UUID id = UUID.randomUUID();
        when(repository.failIfStatusIn(eq(id), anyCollection(), eq("error"), any()))
                .thenReturn(1);
        ChatJobService service = new ChatJobService(repository, new ObjectMapper(), "http://unused.test");
        service.markFailed(id, "error");
        verify(repository).failIfStatusIn(eq(id),
                eq(Set.of(ChatJobStatus.PREPARING, ChatJobStatus.WAITING, ChatJobStatus.PROCESSING)),
                eq("error"), any(LocalDateTime.class));
        // Actual persistence for each starting state is covered by the PostgreSQL tests.
        verify(repository, never()).findByJobId(any());
        verify(repository, never()).save(any());
    }

    @Test
    void missingJobsRemainNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByJobId(id)).thenReturn(Optional.empty());
        ChatJobService service = new ChatJobService(repository, new ObjectMapper(), "http://unused.test");
        List<Consumer<UUID>> calls = List.of(service::markWaiting, service::markProcessing,
                key -> service.markCompleted(key, new CompletedChatJobRequest("answer")),
                key -> service.markFailed(key, "error"),
                key -> service.savePrepared(key, new PreparedChatJobRequest("context", "[]")));
        for (Consumer<UUID> call : calls) {
            BaseException error = assertThrows(BaseException.class, () -> call.accept(id));
            assertEquals(CHAT_JOB_NOT_FOUND, error.getExceptionEnum());
        }
    }

    @ParameterizedTest
    @EnumSource(ChatJobStatus.class)
    void zeroRowClaimIsConflictAndOtherTransitionsFollowStateRules(ChatJobStatus status) {
        UUID id = UUID.randomUUID();
        when(repository.findByJobId(id)).thenReturn(Optional.of(ChatJob.builder().jobId(id).status(status).build()));
        ChatJobService service = new ChatJobService(repository, new ObjectMapper(), "http://unused.test");
        assertEquals(INVALID_CHAT_JOB_STATUS,
                assertThrows(BaseException.class, () -> service.markProcessing(id)).getExceptionEnum());
        if (status == ChatJobStatus.WAITING || status == ChatJobStatus.PROCESSING) {
            assertThrows(BaseException.class, () -> service.savePrepared(id, new PreparedChatJobRequest("c", "[]")));
        }
        if (status == ChatJobStatus.PREPARING || status == ChatJobStatus.WAITING) {
            assertThrows(BaseException.class, () -> service.markCompleted(id, new CompletedChatJobRequest("a")));
        }
        assertDoesNotThrow(() -> service.markWaiting(id));
    }

    @Test
    void successfulClaimAndCompletionUseConditionalUpdates() {
        UUID id = UUID.randomUUID();
        when(repository.processingIfWaiting(eq(id), any())).thenReturn(1);
        when(repository.completeIfProcessing(eq(id), eq("answer"), any())).thenReturn(1);
        ChatJobService service = new ChatJobService(repository, new ObjectMapper(), "http://unused.test");
        service.markProcessing(id);
        service.markCompleted(id, new CompletedChatJobRequest("answer"));
        verify(repository, never()).findByJobId(any());
    }

    @Test
    void registrationErrorOnlyFailsPreparingWithoutSavingOldEntity() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://unused.test/api/v1/chat/jobs")).andRespond(withServerError());
        ChatJobService service = new ChatJobService(repository, new ObjectMapper(), "http://unused.test");
        ReflectionTestUtils.setField(service, "restClient", builder.build());
        assertThrows(ResponseStatusException.class, () -> service.create(new ChatJobCreateRequest("question", 5)));
        verify(repository).failIfStatusIn(any(UUID.class), eq(Set.of(ChatJobStatus.PREPARING)),
                anyString(), any(LocalDateTime.class));
        verify(repository, never()).save(any());
        server.verify();
    }
}
